(ns middleware.compilation.compiler
  (:refer-clojure :exclude [compile])
  (:require #?(:clj [clojure.tools.logging :as log])
            [middleware.utils.core :refer [seek]]
            [middleware.device.boards :as boards]
            [middleware.ast.utils :as ast-utils]
            [middleware.program.emitter :as emit]
            [middleware.compilation.linker :as linker]
            [middleware.compilation.checker :as checker]
            [middleware.compilation.dead-code-remover :as dcr]))

; TODO(Richo): Optimize the linker! Following are the results of running the tufte
; profiler on the compiler and then inside the linker. As you can see, the linker
; seems to be taking half the time of the entire process. I still haven't figured
; out a way of optimizing it without a major rewrite, so I leave this comment here
; to remind my future self of this.
; TODO(Richo): After a little more in-depth profiling I think the biggest issue is
; the ast-utils/global? function, which seems to account for about 26% of the total
; time. Unless I'm reading this wrong, that is insane.
; __________________________________________________________________________________
; (compiler)
; pId                                 nCalls      50% ?       Mean      Clock  Total
;
; defn_resolve-imports                    50       18ms    19.78ms      989ms    29%  <---
; compile                                 50        9ms     10.2ms      510ms    15%
; defn_remove-dead-code                   50        8ms     8.16ms      408ms    12%
; defn_check                              50        4ms     4.28ms      214ms     6%
; defn_augment-ast                        50        1ms      560?s       28ms     1%
;
; Accounted                                                            2.15s     62%
; Clock                                                                3.46s    100%
; __________________________________________________________________________________
; (linker)
; pId                                 nCalls      50% ≤       Mean      Clock  Total
; 
; defn_resolve-import                    350        1ms     2.53ms      885ms    27%
; defn_apply-alias                       150        3ms      3.7ms      555ms    17%  <---
; defn_bind-primitives                   400        1ms    832.5μs      333ms    10%
; defn_build-new-program                 400        0ns       50μs       20ms     1%
; defn_apply-initialization-block        350        0ns    31.43μs       11ms     0%
; defn_implicit-imports_1                350        0ns     5.71μs        2ms     0%
; defn_parse                             350        0ns     2.86μs        1ms     0%
; defn_implicit-imports_0                400        0ns        0ns        0ns     0%
; 
; Accounted                                                            1.81s     55%
; Clock                                                                3.28s    100%
; __________________________________________________________________________________
; (linker/apply-alias)
; pId                                 nCalls      50% ≤       Mean      Clock  Total
;
; update-variable                      9,300        0ns    57.53μs      535ms    14%
; ast-utils/local?                     9,300        0ns    54.95μs      511ms    13%  <---
; update-alias                         9,800        0ns     2.86μs       28ms     1%
; ast-utils/node-type                 36,900        0ns    352.3ns       13ms     0%
; update-name                          1,900        0ns     4.21μs        8ms     0%
; update-selector                      3,200        0ns     2.19μs        7ms     0%
; untouched                           12,700        0ns   236.22ns        3ms     0%
;
; Accounted                                                            1.11s     28%
; Clock                                                                3.91s    100%
; __________________________________________________________________________________
; (ast-utils/global?)
; pId                                 nCalls      50% ≤       Mean      Clock  Total
;
; global-variable?                     9,550        0ns   105.65μs     1.01s     26%  <---
; global-var-decl?                     4,450        0ns     2.02μs        9ms     0%
;
; Accounted                                                            1.02s     26%
; Clock                                                                3.93s    100%
; __________________________________________________________________________________

(declare compile-node)

(defn ^:private with-node
  "Updates result's metadata with the node (if not present already)"
  [result node]
  (vary-meta result update :node #(or % node)))

(defn ^:private compile [node ctx]
  (let [result (compile-node node (update ctx :path conj node))]
    (when (and (sequential? result)
               (not (vector? result)))
      (throw (ex-info "Sequential and not vector!" {:node node :result result})))
    (if-not (sequential? result)
      (with-node result node)
      (mapv #(with-node % node) result))))

(defn ^:private rate->delay [{:keys [^double value scale] :as node}]
  (if-not node
    0
    (if (zero? value)
      (double #?(:clj Double/MAX_VALUE
                 :cljs (.-MAX_VALUE js/Number)))
      (/ (case scale
           "s" 1000
           "m" (* 1000 60)
           "h" (* 1000 60 60)
           "d" (* 1000 60 60 24))
         value))))

(defn ^:private register-constant! [{globals :globals} value]
  (vswap! globals conj (emit/constant value)))

(defn ^:private register-global!
  ([ctx name]
   (register-global! name 0))
  ([{globals :globals} name value]
   (vswap! globals conj (emit/variable name value))))


(defn compile-program [node ctx]
  (doseq [{:keys [name value]} (:globals node)]
    (register-global! ctx name (ast-utils/compile-time-value value 0)))
  (let [scripts (mapv #(compile % ctx)
                      (:scripts node))
        globals @(:globals ctx)]
    (emit/program
     :globals globals
     :scripts scripts)))

; TODO(Richo): Maybe collect the locals while traversing the tree...
(defn collect-locals [script-body]
  (vec (concat
        ; Collect all variable declarations
        (map (fn [{:keys [unique-name value]}]
               (emit/variable unique-name
                              (ast-utils/compile-time-value value 0)))
             (ast-utils/filter script-body "UziVariableDeclarationNode"))

        ; Special case #1: for-loop with variable step
        (map (fn [{:keys [temp-name]}] (emit/variable temp-name))
             (filter (fn [{:keys [temp-name]}] (not (nil? temp-name)))
                     (ast-utils/filter script-body "UziForNode")))

        ; Special case #2: repeat-loop
        (map (fn [{:keys [temp-name]}] (emit/variable temp-name))
             (ast-utils/filter script-body "UziRepeatNode")))))

(defn compile-task
  [{task-name :name, ticking-rate :tickingRate, state :state, body :body}
   ctx]
  (let [delay (rate->delay ticking-rate)]
    (register-constant! ctx delay)
    (emit/script
     :name task-name
     :delay delay
     :running? (contains? #{"running" "once"} state)
     :type (if (= "once" state)
             :task
             :timer)
     :locals (collect-locals body)
     :instructions (compile body ctx))))

(defn ^:private compile-stmt [node ctx]
  (let [instructions (compile node ctx)]
    ; NOTE(Richo): If the node is an expression we need to append a pop instruction to
    ; leave the stack in the correct state (remember: this functions compiles statements!)
    (if (ast-utils/expression? node)
      (conj instructions (with-node (emit/prim-call "pop") node))
      instructions)))

(defn compile-block [{:keys [statements]} ctx]
  (vec (mapcat #(compile-stmt % ctx) statements)))

(defn compile-assignment [{:keys [left right]} {:keys [path] :as ctx}]
  (let [global? (ast-utils/global? left path)
        variable (ast-utils/variable-named (:name left) path)
        var-name (if global?
                   (:name variable)
                   (:unique-name variable))]
    (conj (compile right ctx)
          (if global?
            (emit/write-global var-name)
            (emit/write-local var-name)))))

(defn compile-call
  [{:keys [selector arguments primitive-name] :as node} ctx]
  (let [script (ast-utils/script-named selector (:path ctx))
        positional-args? (or primitive-name
                             (every? #(nil? (:key %)) arguments))
        sorted-args (if positional-args?
                      (if script
                        (map-indexed (fn [idx {:keys [value]}]
                                       (get-in arguments [idx :value] value))
                                     (:arguments script))
                        (map :value arguments))
                      (map (fn [{:keys [name value]}]
                             (if-let [arg (seek #(= name (:key %)) arguments)]
                               (:value arg)
                               value))
                           (:arguments script)))]
    (conj (vec (mapcat #(compile % ctx) sorted-args))
          (if primitive-name
            (emit/prim-call primitive-name)
            (emit/script-call selector)))))

(defn compile-number-literal [{value :value} ctx]
  (register-constant! ctx value)
  [(emit/push-value value)])

(defn compile-variable [node {:keys [path]}]
  (let [global? (ast-utils/global? node path)
        variable (ast-utils/variable-named (:name node) path)
        var-name (if global?
                   (:name variable)
                   (:unique-name variable))]
    [(if global?
       (emit/read-global var-name)
       (emit/read-local var-name))]))

(defn compile-variable-declaration
  [{:keys [unique-name value]} ctx]
  (register-constant! ctx (ast-utils/compile-time-value value 0))
  (if (and (or (nil? value)
               (ast-utils/compile-time-constant? value))
           (empty? (filter ast-utils/loop? (:path ctx))))
    []
    (conj (compile value ctx)
          (emit/write-local unique-name))))

(defn compile-pin-literal [{:keys [value]} ctx]
  (register-constant! ctx value)
  [(emit/push-value value)])

(defn compile-procedure [{:keys [name arguments body]} ctx]
  (register-constant! ctx 0) ; TODO(Richo): Script delay 0
  (emit/script
   :name name,
   :arguments (mapv (fn [{:keys [unique-name value]}]
                      (emit/variable unique-name)) ; TODO(Richo): Handle default value
                    arguments)
   :locals (collect-locals body)
   :instructions (compile body ctx)
   :type :procedure))

(defn compile-function [{:keys [name arguments body]} ctx]
  (register-constant! ctx 0) ; TODO(Richo): Script delay 0
  (emit/script
   :name name,
   :arguments (mapv (fn [{:keys [unique-name value]}]
                      (emit/variable unique-name)) ; TODO(Richo): Handle default value
                    arguments)
   :locals (collect-locals body)
   :instructions (compile body ctx)
   :type :function))

(defn compile-return [{:keys [value]} ctx]
  (if value
    (conj (compile value ctx)
          (emit/prim-call "retv"))
    [(emit/prim-call "ret")]))

(defn compile-script-start [{:keys [scripts]} ctx]
  (mapv #(emit/start %) scripts))

(defn compile-script-stop [{:keys [scripts]} ctx]
  (mapv #(emit/stop %) scripts))

(defn compile-script-pause [{:keys [scripts]} ctx]
  (mapv #(emit/pause %) scripts))

(defn compile-script-resume [{:keys [scripts]} ctx]
  (mapv #(emit/resume %) scripts))

(defn compile-yield [_ _]
  [(emit/prim-call "yield")])

(defn- compile-branch-prim [emit-fn offset ctx]
  (let [prim-map {emit/jmp "jmp"
                  emit/jz "jz"
                  emit/jnz "jnz"
                  emit/jlte "jlte"}
        prim-name (prim-map emit-fn)
        ; HACK(Richo): If we jump backwards we need to decrement the offset to
        ; take into account the new push instruction (this sucks!)
        actual-offset (if (< offset 0) (dec offset) offset)]
    (register-constant! ctx actual-offset)
    [(emit/push-value actual-offset)
     (emit/prim-call prim-name)]))

; TODO(Richo): Think of a better name!
(defn- inside-7bits-range? [n]
  ; Using 2's complement in 7 bits we can encode in the range [-64, 63] 
  (and (>= n -64) (<= n 63)))

(defn- compile-branch [emit-fn offset ctx]
  ; TODO(Richo): Magic numbers!
  (if (inside-7bits-range? offset)
    [(emit-fn offset)]
    (compile-branch-prim emit-fn offset ctx)))

(defn ^:private compile-if-true-if-false
  [{condition :condition, true-branch :trueBranch, false-branch :falseBranch}
   ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)
        compiled-false-branch (compile false-branch ctx)
        count-true (count compiled-true-branch)
        count-false (count compiled-false-branch)]
    (vec (concat compiled-condition
                 (compile-branch emit/jz (+ (if (inside-7bits-range? count-false) 
                                              1 2)
                                            count-true) ctx)
                 compiled-true-branch
                 (compile-branch emit/jmp count-false ctx)
                 compiled-false-branch))))

(defn ^:private compile-if-true
  [{condition :condition, true-branch :trueBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)]
    (vec (concat compiled-condition
                 (compile-branch emit/jz (count compiled-true-branch) ctx)
                 compiled-true-branch))))

(defn ^:private compile-if-false
  [{condition :condition, false-branch :falseBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-false-branch (compile false-branch ctx)]
    (vec (concat compiled-condition
                 (compile-branch emit/jnz (count compiled-false-branch) ctx)
                 compiled-false-branch))))

(defn compile-conditional [node ctx]
  (cond
    (empty? (-> node :falseBranch :statements)) (compile-if-true node ctx)
    (empty? (-> node :trueBranch :statements)) (compile-if-false node ctx)
    :else (compile-if-true-if-false node ctx)))

(defn compile-forever [{:keys [body]} ctx]
  (let [compiled-body (compile body ctx)]
    (vec (concat compiled-body
                 (compile-branch emit/jmp (* -1 (inc (count compiled-body))) ctx)))))

(defn ^:private compile-loop
  [{negated? :negated, :keys [pre condition post]} ctx]
  (let [compiled-pre (compile pre ctx)
        compiled-condition (compile condition ctx)
        compiled-post (compile post ctx)]
    (vec (concat compiled-pre
                 compiled-condition
                 (if (empty? (:statements post))
                   (compile-branch (if negated? emit/jz emit/jnz)
                                   (* -1 (+ 1
                                            (count compiled-pre)
                                            (count compiled-condition)))
                                   ctx)
                   (let [count-start (* -1 (+ 2
                                              (count compiled-pre)
                                              (count compiled-condition)
                                              (count compiled-post)))
                         count-end (inc (count compiled-post))]
                     (if (and (inside-7bits-range? count-start)
                              (inside-7bits-range? count-end))
                       (concat (compile-branch (if negated? emit/jnz emit/jz)
                                               count-end ctx)
                               compiled-post
                               (compile-branch emit/jmp count-start ctx))
                       ; TODO(Richo): Make sure to test this!
                       (concat (compile-branch-prim (if negated? emit/jnz emit/jz)
                                                    (inc count-end) ctx)
                               compiled-post
                               (compile-branch-prim emit/jmp (dec count-start) ctx)))))))))

(defn ^:private compile-for-loop-with-constant-step
  [{:keys [counter start stop step body]} ctx] 
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        count-end (+ 4
                     (count compiled-body)
                     (count compiled-step))
        count-begin (* -1 (+ 7
                             (count compiled-step)
                             (count compiled-body)
                             (count compiled-stop)))
        counter-name (:unique-name counter)]
    ; HACK(Richo): This is such a disgusting hack...
    (if (and (inside-7bits-range? count-end)
             (inside-7bits-range? count-begin))
      (vec (concat
            ; First, we initialize counter
            compiled-start
            [(emit/write-local counter-name)

             ; Then, we compare counter with stop
             (emit/read-local counter-name)]
            compiled-stop

            ; We can do this statically because we know step is a compile-time constant
            [(if (> (ast-utils/compile-time-value step 0) 0)
               (emit/prim-call "lessThanOrEquals")
               (emit/prim-call "greaterThanOrEquals"))]

            ; If the condition succeeds, we jump to the end (break out of the loop)
            (compile-branch emit/jz count-end ctx)

            ; We execute the body
            compiled-body

            ; But before jumping back to the comparison, we increment counter by step
            [(emit/read-local counter-name)]
            compiled-step
            [(emit/prim-call "add")
             (emit/write-local counter-name)]

            ; And now we jump to the beginning
            (compile-branch emit/jmp count-begin ctx)))
      (vec (concat
            ; First, we initialize counter
            compiled-start
            [(emit/write-local counter-name)

             ; Then, we compare counter with stop
             (emit/read-local counter-name)]
            compiled-stop

            ; We can do this statically because we know step is a compile-time constant
            [(if (> (ast-utils/compile-time-value step 0) 0)
               (emit/prim-call "lessThanOrEquals")
               (emit/prim-call "greaterThanOrEquals"))]

            ; If the condition succeeds, we jump to the end (break out of the loop)
            (compile-branch-prim emit/jz (inc count-end) ctx)

            ; We execute the body
            compiled-body

            ; But before jumping back to the comparison, we increment counter by step
            [(emit/read-local counter-name)]
            compiled-step
            [(emit/prim-call "add")
             (emit/write-local counter-name)]

            ; And now we jump to the beginning
            (compile-branch-prim emit/jmp (dec count-begin) ctx))))))


(defn ^:private compile-for-loop
  [{:keys [counter start stop step body temp-name]} ctx]
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        count-end (+ 5 (count compiled-body))
        count-begin (* -1 (+ 14
                             (count compiled-body)
                             (count compiled-step)
                             (count compiled-stop)))
        counter-name (:unique-name counter)]
    (if (and (inside-7bits-range? count-end)
             (inside-7bits-range? count-begin))
      (vec (concat
            ; First, we initialize counter
            compiled-start
            [(emit/write-local counter-name)

            ; This is where the loop begins!

            ; Then, we compare counter with stop. The comparison can either be
            ; GTEQ or LTEQ depending on the sign of the step (which is stored on temp)
             (emit/read-local counter-name)]
            compiled-stop
            compiled-step
            [(emit/write-local temp-name)
             (emit/read-local temp-name)
             (emit/push-value 0)
             (emit/jlte 2)
             (emit/prim-call "lessThanOrEquals")
             (emit/jmp 1)
             (emit/prim-call "greaterThanOrEquals")]
            (compile-branch emit/jz count-end ctx)

          ; While counter doesn't reach the stop we execute the body
            compiled-body

          ; Before jumping back to the comparison, we increment counter by step
            [(emit/read-local counter-name)
             (emit/read-local temp-name)
             (emit/prim-call "add")
             (emit/write-local counter-name)]

          ; And now we jump to the beginning           
            (compile-branch emit/jmp count-begin ctx)))
      (vec (concat
            ; First, we initialize counter
            compiled-start
            [(emit/write-local counter-name)

            ; This is where the loop begins!

            ; Then, we compare counter with stop. The comparison can either be
            ; GTEQ or LTEQ depending on the sign of the step (which is stored on temp)
             (emit/read-local counter-name)]
            compiled-stop
            compiled-step
            [(emit/write-local temp-name)
             (emit/read-local temp-name)
             (emit/push-value 0)
             (emit/jlte 2)
             (emit/prim-call "lessThanOrEquals")
             (emit/jmp 1)
             (emit/prim-call "greaterThanOrEquals")]
            (compile-branch-prim emit/jz (inc count-end) ctx)

          ; While counter doesn't reach the stop we execute the body
            compiled-body

          ; Before jumping back to the comparison, we increment counter by step
            [(emit/read-local counter-name)
             (emit/read-local temp-name)
             (emit/prim-call "add")
             (emit/write-local counter-name)]

          ; And now we jump to the beginning           
            (compile-branch-prim emit/jmp (dec count-begin) ctx))))))

(defn compile-for [{:keys [step] :as node} ctx]
  (register-constant! ctx 0) ; TODO(Richo): This seems to be necessary only if the step is not constant
  (if (ast-utils/compile-time-constant? step)
    (compile-for-loop-with-constant-step node ctx)
    (compile-for-loop node ctx)))

(defn compile-repeat
  [{:keys [body times temp-name]} ctx]
  ; We need to register constants 0 and 1 because repeat-loops use 0 to
  ; initialize "temp" and 1 to increment "times"
  (register-constant! ctx 0)
  (register-constant! ctx 1)
  (let [compiled-body (compile body ctx)
        compiled-times (compile times ctx)
        count-end (+ 5 (count compiled-body))
        count-begin (* -1 (+ 8
                             (count compiled-body)
                             (count compiled-times)))]
    (if (and (inside-7bits-range? count-end)
             (inside-7bits-range? count-begin))
      (vec (concat
          ; First, we set temp = 0
            [(emit/push-value 0)
             (emit/write-local temp-name)]

          ; This is where the loop begins!

          ; Then, we compare temp with times
            [(emit/read-local temp-name)]
            compiled-times
            [(emit/prim-call "lessThan")]
            (compile-branch emit/jz count-end ctx)

          ; While temp is less than the expected times we execute the body
            compiled-body

          ; Before jumping back to the comparison, we increment temp
            [(emit/read-local temp-name)
             (emit/push-value 1)
             (emit/prim-call "add")
             (emit/write-local temp-name)]

            (compile-branch emit/jmp count-begin ctx)))
      (vec (concat
          ; First, we set temp = 0
            [(emit/push-value 0)
             (emit/write-local temp-name)]

          ; This is where the loop begins!

          ; Then, we compare temp with times
            [(emit/read-local temp-name)]
            compiled-times
            [(emit/prim-call "lessThan")]
            (compile-branch-prim emit/jz (inc count-end) ctx)

          ; While temp is less than the expected times we execute the body
            compiled-body

          ; Before jumping back to the comparison, we increment temp
            [(emit/read-local temp-name)
             (emit/push-value 1)
             (emit/prim-call "add")
             (emit/write-local temp-name)]

            (compile-branch-prim emit/jmp (dec count-begin) ctx))))))

(defn compile-logical-and [{:keys [left right]} ctx]
  (let [compiled-left (compile left ctx)
        compiled-right (compile right ctx)]
    (if (ast-utils/has-side-effects? right)
      ; We need to short-circuit
      (do
        (register-constant! ctx 0)
        (vec (concat compiled-left
                     (compile-branch emit/jz (inc (count compiled-right)) ctx)
                     compiled-right
                     [(emit/jmp 1)
                      (emit/push-value 0)])))
      ; Primitive call is enough
      (vec (concat compiled-left
                   compiled-right
                   [(emit/prim-call "logicalAnd")])))))

(defn compile-logical-or [{:keys [left right]} ctx]
  (let [compiled-left (compile left ctx)
        compiled-right (compile right ctx)]
    (if (ast-utils/has-side-effects? right)
      ; We need to short-circuit
      (do
        (register-constant! ctx 1)
        (vec (concat compiled-left
                     (compile-branch emit/jnz (inc (count compiled-right)) ctx)
                     compiled-right
                     [(emit/jmp 1)
                      (emit/push-value 1)])))
      ; Primitive call is enough
      (vec (concat compiled-left
                   compiled-right
                   [(emit/prim-call "logicalOr")])))))

(defn compile-node [node ctx]
  (case (ast-utils/node-type node)
    "UziProgramNode" (compile-program node ctx)
    "UziTaskNode" (compile-task node ctx)
    "UziBlockNode" (compile-block node ctx)
    "UziAssignmentNode" (compile-assignment node ctx)
    "UziCallNode" (compile-call node ctx)
    "UziNumberLiteralNode" (compile-number-literal node ctx)
    "UziVariableNode" (compile-variable node ctx)
    "UziVariableDeclarationNode" (compile-variable-declaration node ctx)
    "UziPinLiteralNode" (compile-pin-literal node ctx)
    "UziProcedureNode" (compile-procedure node ctx)
    "UziFunctionNode" (compile-function node ctx)
    "UziReturnNode" (compile-return node ctx)
    "UziScriptStartNode" (compile-script-start node ctx)
    "UziScriptStopNode" (compile-script-stop node ctx)
    "UziScriptPauseNode" (compile-script-pause node ctx)
    "UziScriptResumeNode" (compile-script-resume node ctx)
    "UziYieldNode" (compile-yield node ctx)
    "UziConditionalNode" (compile-conditional node ctx)
    "UziForeverNode" (compile-forever node ctx)
    "UziWhileNode" (compile-loop node ctx)
    "UziUntilNode" (compile-loop node ctx)
    "UziDoWhileNode" (compile-loop node ctx)
    "UziDoUntilNode" (compile-loop node ctx)
    "UziForNode" (compile-for node ctx)
    "UziRepeatNode" (compile-repeat node ctx)
    "UziLogicalAndNode" (compile-logical-and node ctx)
    "UziLogicalOrNode" (compile-logical-or node ctx)
    (throw (ex-info "Unknown node" {:node node, :ctx ctx}))))

(defn ^:private create-context []
  {:path (list)
   :globals (volatile! #{})})

(defn augment-ast
  "This function augments the AST with more information needed for the compiler.
   1) All pin literals are augmented with a :value that corresponds to their
      pin number for the given board. This is useful because it makes pin literals
      polymorphic with number literals. And also, we'll need this value later and it
      would be a pain in the ass to pass around the board every time.
   2) All nodes that could need to declare either a local or temporary variable
      are augmented with a unique name (based on a simple counter) that the compiler
      can later use to identify this variable.
      I'm using the following naming conventions:
      - Temporary variables are named with @ and then the counter
      - Local variables already have a name, so I just add the # suffix followed by the counter"
  [ast board]
  (let [local-counter (volatile! 0)
        temp-counter (volatile! 0)
        reset-counters! (fn []
                          (vreset! local-counter 0)
                          (vreset! temp-counter 0))]
    (ast-utils/transform-with-path
     ast
     (fn [node path]
       (case (ast-utils/node-type node)
         "UziPinLiteralNode"
         (let [{:keys [type number] :as pin} node]
           (assoc pin :value (boards/get-pin-number (str type number) board)))

         ; Temporary variables are local to their script
         "UziTaskNode" (do (reset-counters!) node)
         "UziProcedureNode" (do (reset-counters!) node)
         "UziFunctionNode" (do (reset-counters!) node)

         "UziForNode" ; Some for-loops declare a temporary variable.
         (if (ast-utils/compile-time-constant? (node :step))
           node
           (assoc node :temp-name (str "@" (vswap! temp-counter inc))))

         "UziRepeatNode" ; All repeat-loops declare a temporary variable
         (assoc node :temp-name (str "@" (vswap! temp-counter inc)))

         "UziVariableDeclarationNode"
         (if (ast-utils/global? node path) ; TODO(Richo): Avoid renaming a variable if its name is already unique
           node
           (assoc node :unique-name (str (:name node) "#" (vswap! local-counter inc))))
         
         node)))))

(defn remove-dead-code [ast & [remove-dead-code?]]
  (if remove-dead-code?
    (dcr/remove-dead-code ast)
    ast))

(defn check [ast check-external-code? concurrency-enabled?]
  ; TODO(Richo): Use the src to improve the error messages
  (let [errors (checker/check-tree ast 
                                   check-external-code?
                                   concurrency-enabled?)]
    (if-not (empty? errors)
      (throw (ex-info (str (count errors)
                           " error" (if (= 1 (count errors)) "" "s")
                           " found!")
                      {:src (-> ast meta :token :source)
                       :errors errors}))
      ast)))

(defn resolve-imports [ast lib-dir]
  (linker/resolve-imports ast lib-dir))

(defn compile-tree
  [original-ast & {:keys [board lib-dir remove-dead-code? 
                          check-external-code? concurrency-enabled?]
                   :or {board boards/UNO,
                        lib-dir "../../uzi/libraries",
                        remove-dead-code? true
                        check-external-code? false
                        concurrency-enabled? true}}]
  (let [ast (-> original-ast
                (resolve-imports lib-dir)
                (check check-external-code?  ; NOTE(Richo): We need to do the check *after* resolving the imports 
                       concurrency-enabled?) ; and *before* removing the dead-code. Otherwise, we could write invalid
                                             ; programs that would compile just fine because the invalid code gets 
                                             ; deleted before the check.
                (remove-dead-code remove-dead-code?)
                (augment-ast board))
        src (-> ast meta :token :source)]
    (vary-meta (compile ast (create-context))
               assoc
               :source src
               :original-ast original-ast
               :final-ast ast)))
