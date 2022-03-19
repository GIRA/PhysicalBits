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

; TODO(Richo): The following results are most likely outdated because I had to revert 
; the change that moved the AST check to the end. Now it's probable that the checker
; is again the bottleneck...
; TODO(Richo): Optimize the linker! Following are the results of running the tufte
; profiler on the compiler and then inside the linker. As you can see, the linker
; seems to be taking half the time of the entire process. I still haven't figured
; out a way of optimizing it without a major rewrite, so I leave this comment here
; to remind my future self of this.
; ___________________________________________________________________________________________________________________________________________________________________
; pId                                                     nCalls        Min      50% ≤      90% ≤      95% ≤      99% ≤        Max       Mean   MAD      Clock  Total
;
; :middleware.compilation.compiler/defn_resolve-imports          50       40ms       51ms       74ms       95ms      161ms      161ms     57.8ms  ±24%     2.89s     53%
; :middleware.parser.parser/parse                             50       14ms       23ms       31ms       34ms       52ms       52ms    23.74ms  ±19%     1.19s     22%
; :middleware.compilation.compiler/compile                       50        5ms       10ms       12ms       16ms       26ms       26ms     9.96ms  ±21%      498ms     9%
; :middleware.compilation.compiler/defn_remove-dead-code         50        5ms        9ms       14ms       16ms       17ms       17ms     9.36ms  ±21%      468ms     9%
; :middleware.compilation.compiler/defn_check                    50        3ms        5ms        8ms       10ms       20ms       20ms     5.44ms  ±34%      272ms     5%
; :middleware.compilation.compiler/defn_augment-ast              50        1ms        3ms        4ms        4ms        4ms        4ms     2.82ms  ±19%      141ms     3%
;
; Accounted                                                                                                                                             5.46s    100%
; Clock                                                                                                                                                 5.47s    100%
; ___________________________________________________________________________________________________________________________________________________________________________
; pId                                                             nCalls        Min      50% ≤      90% ≤      95% ≤      99% ≤        Max       Mean   MAD      Clock  Total
;
; :middleware.compilation.linker/defn_apply-alias                       350        0ns        0ns        5ms        6ms        9ms       18ms     1.98ms ±114%      693ms    30%
; :middleware.compilation.linker/defn_bind-primitives                   400        0ns        1ms        3ms        3ms        4ms        9ms      980μs  ±70%      392ms    17%
; :middleware.compilation.linker/defn_build-new-program                 400        0ns        0ns        0ns        1ms        1ms        1ms     72.5μs ±186%       29ms     1%
; :middleware.compilation.linker/defn_apply-initialization-block        350        0ns        0ns        0ns        1ms        1ms        2ms    62.86μs ±188%       22ms     1%
; :middleware.compilation.linker/defn_implicit-imports_1                350        0ns        0ns        0ns        0ns        0ns        1ms     5.71μs ±199%        2ms     0%
; :middleware.compilation.linker/defn_parse                             350        0ns        0ns        0ns        0ns        0ns        1ms     2.86μs ±199%        1ms     0%
; :middleware.compilation.linker/defn_implicit-imports_0                400        0ns        0ns        0ns        0ns        0ns        0ns        0ns ±NaN%        0ns     0%
;
; Accounted                                                                                                                                                     1.14s     50%
; Clock                                                                                                                                                         2.29s    100%
; ___________________________________________________________________________________________________________________________________________________________________________

(declare compile-node)

(defn ^:private with-node [result node]
  "Updates result's metadata with the node (if not present already)"
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
     :once? (= "once" state)
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
  (if (or (nil? value)
          (ast-utils/compile-time-constant? value))
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
   :instructions (compile body ctx)))

(defn compile-function [{:keys [name arguments body]} ctx]
  (register-constant! ctx 0) ; TODO(Richo): Script delay 0
  (emit/script
   :name name,
   :arguments (mapv (fn [{:keys [unique-name value]}]
                      (emit/variable unique-name)) ; TODO(Richo): Handle default value
                    arguments)
   :locals (collect-locals body)
   :instructions (compile body ctx)))

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

(defn ^:private compile-if-true-if-false
  [{condition :condition, true-branch :trueBranch, false-branch :falseBranch}
   ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)
        compiled-false-branch (compile false-branch ctx)]
    (vec (concat compiled-condition
                 [(emit/jz (inc (count compiled-true-branch)))]
                 compiled-true-branch
                 [(emit/jmp (count compiled-false-branch))]
                 compiled-false-branch))))

(defn ^:private compile-if-true
  [{condition :condition, true-branch :trueBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)]
    (vec (concat compiled-condition
                 [(emit/jz (count compiled-true-branch))]
                 compiled-true-branch))))

(defn ^:private compile-if-false
  [{condition :condition, false-branch :falseBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-false-branch (compile false-branch ctx)]
    (vec (concat compiled-condition
                 [(emit/jnz (count compiled-false-branch))]
                 compiled-false-branch))))

(defn compile-conditional [node ctx]
  (cond
    (empty? (-> node :falseBranch :statements)) (compile-if-true node ctx)
    (empty? (-> node :trueBranch :statements)) (compile-if-false node ctx)
    :else (compile-if-true-if-false node ctx)))

(defn compile-forever [{:keys [body]} ctx]
  (let [compiled-body (compile body ctx)]
    (conj compiled-body
          (emit/jmp (* -1 (inc (count compiled-body)))))))

(defn ^:private compile-loop
  [{negated? :negated, :keys [pre condition post]} ctx]
  (let [compiled-pre (compile pre ctx)
        compiled-condition (compile condition ctx)
        compiled-post (compile post ctx)]
    (vec (concat compiled-pre
                 compiled-condition
                 (if (empty? (:statements post))
                   (let [count (* -1 (+ 1
                                        (count compiled-pre)
                                        (count compiled-condition)))]
                     (if negated?
                       [(emit/jz count)]
                       [(emit/jnz count)]))
                   (let [count-start (* -1 (+ 2
                                              (count compiled-pre)
                                              (count compiled-condition)
                                              (count compiled-post)))
                         count-end (inc (count compiled-post))]
                     (concat [(if negated?
                                (emit/jnz count-end)
                                (emit/jz count-end))]
                             compiled-post
                             [(emit/jmp count-start)])))))))

(defn ^:private compile-for-loop-with-constant-step
  [{:keys [counter start stop step body]} ctx]
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        counter-name (:unique-name counter)]
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
             (emit/prim-call "greaterThanOrEquals"))

           ; If the condition succeeds, we jump to the end (break out of the loop)
           (emit/jz (+ 4
                       (count compiled-body)
                       (count compiled-step)))]

          ; We execute the body
          compiled-body

          ; But before jumping back to the comparison, we increment counter by step
          [(emit/read-local counter-name)]
          compiled-step
          [(emit/prim-call "add")
           (emit/write-local counter-name)

           ; And now we jump to the beginning
           (emit/jmp (* -1 (+ 7
                              (count compiled-step)
                              (count compiled-body)
                              (count compiled-stop))))]))))


(defn ^:private compile-for-loop
  [{:keys [counter start stop step body temp-name]} ctx]
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        counter-name (:unique-name counter)]
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
           (emit/prim-call "greaterThanOrEquals")
           (emit/jz (+ 5 (count compiled-body)))]

          ; While counter doesn't reach the stop we execute the body
          compiled-body

          ; Before jumping back to the comparison, we increment counter by step
          [(emit/read-local counter-name)
           (emit/read-local temp-name)
           (emit/prim-call "add")
           (emit/write-local counter-name)

           ; And now we jump to the beginning
           (emit/jmp (* -1 (+ 14
                              (count compiled-body)
                              (count compiled-step)
                              (count compiled-stop))))]))))

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
        compiled-times (compile times ctx)]
    (vec (concat
          ; First, we set temp = 0
          [(emit/push-value 0)
           (emit/write-local temp-name)]

          ; This is where the loop begins!

          ; Then, we compare temp with times
          [(emit/read-local temp-name)]
          compiled-times
          [(emit/prim-call "lessThan")
           (emit/jz (+ 5 (count compiled-body)))]

          ; While temp is less than the expected times we execute the body
          compiled-body

          ; Before jumping back to the comparison, we increment temp
          [(emit/read-local temp-name)
           (emit/push-value 1)
           (emit/prim-call "add")
           (emit/write-local temp-name)
           (emit/jmp (* -1 (+ 8
                              (count compiled-body)
                              (count compiled-times))))]))))

(defn compile-logical-and [{:keys [left right]} ctx]
  (let [compiled-left (compile left ctx)
        compiled-right (compile right ctx)]
    (if (ast-utils/has-side-effects? right)
      ; We need to short-circuit
      (do
        (register-constant! ctx 0)
        (vec (concat compiled-left
                     [(emit/jz (inc (count compiled-right)))]
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
                     [(emit/jnz (inc (count compiled-right)))]
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

(defn augment-ast [ast board]
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
  (let [local-counter (volatile! 0)
        temp-counter (volatile! 0)
        reset-counters! (fn []
                          (vreset! local-counter 0)
                          (vreset! temp-counter 0))]
    (ast-utils/transform
     ast

     "UziPinLiteralNode"
     (fn [{:keys [type number] :as pin} _]
       (assoc pin :value (boards/get-pin-number (str type number) board)))

     ; Temporary variables are local to their script
     "UziTaskNode" (fn [node _] (reset-counters!) node)
     "UziProcedureNode" (fn [node _] (reset-counters!) node)
     "UziFunctionNode" (fn [node _] (reset-counters!) node)

     "UziForNode" ; Some for-loops declare a temporary variable.
     (fn [{:keys [step] :as node} _]
       (if (ast-utils/compile-time-constant? step)
         node
         (assoc node :temp-name (str "@" (vswap! temp-counter inc)))))

     "UziRepeatNode" ; All repeat-loops declare a temporary variable
     (fn [node _]
       (assoc node :temp-name (str "@" (vswap! temp-counter inc))))

     "UziVariableDeclarationNode"
     (fn [var path] ; TODO(Richo): Avoid renaming a variable if its name is already unique
       (if (ast-utils/global? var path)
         var
         (assoc var :unique-name (str (:name var) "#" (vswap! local-counter inc))))))))

(defn remove-dead-code [ast & [remove-dead-code?]]
  (if remove-dead-code?
    (dcr/remove-dead-code ast)
    ast))

(defn check [ast check-external-code?]
  ; TODO(Richo): Use the src to improve the error messages
  (let [errors (checker/check-tree ast check-external-code?)]
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
  [original-ast & {:keys [board lib-dir remove-dead-code? check-external-code?]
                   :or {board boards/UNO,
                        lib-dir "../../uzi/libraries",
                        remove-dead-code? true
                        check-external-code? false}}]
  (let [ast (-> original-ast
                (resolve-imports lib-dir)
                (check check-external-code?) ; NOTE(Richo): We need to do the check *after* resolving the imports 
                                             ; and *before* removing the dead-code. Otherwise, we could write invalid 
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
