(ns plugin.compiler.core
  (:refer-clojure :exclude [compile])
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :as w]
            [plugin.device.boards :as boards]
            [plugin.compiler.ast-utils :as ast-utils]
            [plugin.compiler.emitter :as emit]
            [plugin.compiler.primitives :as prims]
            [plugin.compiler.linker :as linker]))

(defmulti compile-node :__class__)

(defn compile [node ctx]
  (compile-node node (update-in ctx [:path] conj node)))

(defn- rate->delay [{:keys [value scale] :as node}]
  (if-not node 0
    (if (= value 0)
      (double Double/MAX_VALUE)
      (/ (case scale
           "s" 1000
           "m" (* 1000 60)
           "h" (* 1000 60 60)
           "d" (* 1000 60 60 24))
         value))))

(defn variables-in-scope [path]
  (mapcat (fn [[first second]]
            (filter #(= "UziVariableDeclarationNode" (ast-utils/node-type %))
                    (take-while #(not (= % first))
                                (ast-utils/children second))))
          (partition 2 1 path)))

(defn- variable-name-and-scope [{:keys [name]} ctx]
  (let [variables-in-scope (variables-in-scope (ctx :path))
        globals (-> ctx :path last :globals set)
        variable (first (filter #(= name (:name %))
                                variables-in-scope))
        global? (contains? globals variable)
        var-name (variable (if global? :name :unique-name))]
    [var-name global?]))

(defn collect-globals [ast board]
  (set (concat

        ; Collect all number literals
        (map (fn [{:keys [value]}] (emit/constant value))
             (mapcat #(ast-utils/filter % "UziNumberLiteralNode")
                     (:scripts ast)))

        ; Collect all pin literals
        (map (fn [{:keys [type number]}] (emit/constant (boards/get-pin-number (str type number) board)))
             (ast-utils/filter ast "UziPinLiteralNode"))

        ; Collect repeat-loops (they use 0 to initialize temp and 1 to increment times)
        (mapcat (fn [_] [(emit/constant 0)
                         (emit/constant 1)])
                (ast-utils/filter ast "UziRepeatNode"))

        ; Collect for-loops (they use 0 to initialize temp)
        (map (fn [_] [(emit/constant 0)])
             (ast-utils/filter ast "UziForNode"))

        ; Collect logical-or (with short-circuit)
        (map (fn [_] (emit/constant 1))
             (filter (fn [{:keys [right]}] (ast-utils/has-side-effects? right))
                     (ast-utils/filter ast "UziLogicalOrNode")))


        ; Collect logical-and (with short-circuit)
        (map (fn [_] (emit/constant 0))
             (filter (fn [{:keys [right]}] (ast-utils/has-side-effects? right))
                     (ast-utils/filter ast "UziLogicalAndNode")))

        ; Collect all globals
        (map (fn [{:keys [name value]}] (emit/variable name (ast-utils/compile-time-value value 0)))
             (:globals ast))

        ; Collect all local values
        (map (fn [{:keys [value]}] (emit/constant (if (nil? value) 0
                                                    (ast-utils/compile-time-value value 0))))
             (mapcat (fn [{:keys [body]}] (ast-utils/filter body "UziVariableDeclarationNode"))
                     (:scripts ast)))

        ; Collect all ticking rates
        (map (fn [{:keys [tickingRate]}] (emit/constant (rate->delay tickingRate)))
             (:scripts ast)))))

(defmethod compile-node "UziProgramNode" [node ctx]
  (emit/program
   :globals (collect-globals node (ctx :board))
   :scripts (mapv #(compile % ctx)
                  (:scripts node))))

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

(defmethod compile-node "UziTaskNode"
  [{task-name :name, ticking-rate :tickingRate, state :state, body :body}
   ctx]
  (emit/script
   :name task-name,
   :delay (rate->delay ticking-rate),
   :running? (contains? #{"running" "once"} state)
   :locals (collect-locals body)
   :instructions (let [instructions (compile body ctx)]
                   (if (= "once" state)
                     (conj instructions (emit/stop task-name))
                     instructions))))

(defn- compile-stmt [node ctx]
  (let [instructions (compile node ctx)]
    (if (ast-utils/expression? node)
      (conj instructions (emit/prim-call "pop"))
      instructions)))

(defmethod compile-node "UziBlockNode" [{:keys [statements]} ctx]
  (vec (mapcat #(compile-stmt % ctx) statements)))

(defmethod compile-node "UziAssignmentNode" [{:keys [left right]} ctx]
  (let [[var-name global?] (variable-name-and-scope left ctx)]
    (conj (compile right ctx)
          (if global?
            (emit/write-global var-name)
            (emit/write-local var-name)))))

(defmethod compile-node "UziCallNode"
  [{:keys [selector arguments primitive-name] :as node} ctx]
  (let [positional-args? (or primitive-name
                             (every? #(nil? (:key %)) arguments))
        sorted-args (if positional-args?
                      arguments
                      (let [script (first (filter #(= selector (:name %))
                                                  (-> ctx :path last :scripts)))]
                        (map (fn [{:keys [name]}]
                               (first (filter #(= name (:key %))
                                              arguments)))
                             (:arguments script))))]
    (conj (vec (mapcat #(compile (:value %) ctx)
                       sorted-args))
          (if primitive-name
            (emit/prim-call primitive-name)
            (emit/script-call selector)))))

(defmethod compile-node "UziNumberLiteralNode" [node _]
  [(emit/push-value (node :value))])

(defmethod compile-node "UziVariableNode" [node ctx]
  (let [[var-name global?] (variable-name-and-scope node ctx)]
    [(if global?
       (emit/read-global var-name)
       (emit/read-local var-name))]))

(defmethod compile-node "UziVariableDeclarationNode"
  [{:keys [unique-name value]} ctx]
  (if (or (nil? value)
          (ast-utils/compile-time-constant? value))
    []
    (conj (compile value ctx)
          (emit/write-local unique-name))))

(defmethod compile-node "UziPinLiteralNode" [{:keys [type number]} ctx]
  [(emit/push-value (boards/get-pin-number (str type number) (ctx :board)))])

(defmethod compile-node "UziProcedureNode" [{:keys [name arguments body]} ctx]
  (emit/script
   :name name,
   :arguments (mapv (fn [{:keys [unique-name value]}]
                      (emit/variable unique-name value))
                    arguments)
   :instructions (compile body ctx)))

(defmethod compile-node "UziFunctionNode" [{:keys [name arguments body]} ctx]
  (emit/script
   :name name,
   :arguments (mapv (fn [{:keys [unique-name value]}]
                      (emit/variable unique-name value))
                    arguments)
   :instructions (compile body ctx)))

(defmethod compile-node "UziReturnNode" [{:keys [value]} ctx]
  (if value
    (conj (compile value ctx)
          (emit/prim-call "retv"))
    [(emit/prim-call "ret")]))

(defmethod compile-node "UziScriptStartNode" [{:keys [scripts]} ctx]
  (mapv #(emit/start %) scripts))

(defmethod compile-node "UziScriptStopNode" [{:keys [scripts]} ctx]
  (mapv #(emit/stop %) scripts))

(defmethod compile-node "UziScriptPauseNode" [{:keys [scripts]} ctx]
  (mapv #(emit/pause %) scripts))

(defmethod compile-node "UziScriptResumeNode" [{:keys [scripts]} ctx]
  (mapv #(emit/resume %) scripts))

(defmethod compile-node "UziYieldNode" [_ _]
  [(emit/prim-call "yield")])

(defn- compile-if-true-if-false
  [{condition :condition, true-branch :trueBranch, false-branch :falseBranch}
   ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)
        compiled-false-branch (compile false-branch ctx)]
    (concat compiled-condition
            [(emit/jz (inc (count compiled-true-branch)))]
            compiled-true-branch
            [(emit/jmp (count compiled-false-branch))]
            compiled-false-branch)))

(defn- compile-if-true
  [{condition :condition, true-branch :trueBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-true-branch (compile true-branch ctx)]
    (concat compiled-condition
            [(emit/jz (count compiled-true-branch))]
            compiled-true-branch)))

(defn- compile-if-false
  [{condition :condition, false-branch :falseBranch} ctx]
  (let [compiled-condition (compile condition ctx)
        compiled-false-branch (compile false-branch ctx)]
    (concat compiled-condition
            [(emit/jnz (count compiled-false-branch))]
            compiled-false-branch)))

(defmethod compile-node "UziConditionalNode" [node ctx]
  (cond
    (empty? (-> node :falseBranch :statements)) (compile-if-true node ctx)
    (empty? (-> node :trueBranch :statements)) (compile-if-false node ctx)
    :else (compile-if-true-if-false node ctx)))

(defmethod compile-node "UziForeverNode" [{:keys [body]} ctx]
  (let [compiled-body (compile body ctx)]
    (conj compiled-body
          (emit/jmp (* -1 (inc (count compiled-body)))))))

(defn- compile-loop
  [{negated? :negated, :keys [pre condition post]} ctx]
  (let [compiled-pre (compile pre ctx)
        compiled-condition (compile condition ctx)
        compiled-post (compile post ctx)]
    (concat compiled-pre
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
                        [(emit/jmp count-start)]))))))

; TODO(Richo): The following four node types could be merged into one
(defmethod compile-node "UziWhileNode" [node ctx]
  (compile-loop node ctx))
(defmethod compile-node "UziUntilNode" [node ctx]
  (compile-loop node ctx))
(defmethod compile-node "UziDoWhileNode" [node ctx]
  (compile-loop node ctx))
(defmethod compile-node "UziDoUntilNode" [node ctx]
  (compile-loop node ctx))

(defn- compile-for-loop-with-constant-step
  [{:keys [counter start stop step body]} ctx]
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        counter-name (:unique-name counter)]
    (concat
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
                         (count compiled-stop))))])))


(defn- compile-for-loop
  [{:keys [counter start stop step body temp-name]} ctx]
  (let [compiled-start (compile start ctx)
        compiled-stop (compile stop ctx)
        compiled-step (compile step ctx)
        compiled-body (compile body ctx)
        counter-name (:unique-name counter)]
    (concat
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
                         (count compiled-stop))))])))

(defmethod compile-node "UziForNode" [{:keys [step] :as node} ctx]
  (if (ast-utils/compile-time-constant? step)
    (compile-for-loop-with-constant-step node ctx)
    (compile-for-loop node ctx)))

(defmethod compile-node "UziRepeatNode"
  [{:keys [body times temp-name]} ctx]
  (let [compiled-body (compile body ctx)
        compiled-times (compile times ctx)]
    (concat
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
                         (count compiled-times))))])))

(defmethod compile-node "UziLogicalAndNode" [{:keys [left right]} ctx]
  (let [compiled-left (compile left ctx)
        compiled-right (compile right ctx)]
    (if (ast-utils/has-side-effects? right)
      ; We need to short-circuit
      (concat compiled-left
              [(emit/jz (inc (count compiled-right)))]
              compiled-right
              [(emit/jmp 1)
               (emit/push-value 0)])
      ; Primitive call is enough
      (concat compiled-left
              compiled-right
              [(emit/prim-call "logicalAnd")]))))

(defmethod compile-node "UziLogicalOrNode" [{:keys [left right]} ctx]
  (let [compiled-left (compile left ctx)
        compiled-right (compile right ctx)]
    (if (ast-utils/has-side-effects? right)
      ; We need to short-circuit
      (concat compiled-left
              [(emit/jnz (inc (count compiled-right)))]
              compiled-right
              [(emit/jmp 1)
               (emit/push-value 1)])
      ; Primitive call is enough
      (concat compiled-left
              compiled-right
              [(emit/prim-call "logicalOr")]))))

(defmethod compile-node :default [node _]
  (println "ERROR! Unknown node: " (ast-utils/node-type node))
  :oops)

(defn- create-context [board]
  {:path (list)
   :board board})

(defn- assign-unique-variable-names [ast]
  (let [counter (atom 0)
        temp-counter (atom 0)
        globals (-> ast :globals set)]
    (ast-utils/transform
     ast

     ; Temporary variables are local to their script
     "UziTaskNode" (fn [node] (reset! temp-counter 0) node)
     "UziProcedureNode" (fn [node] (reset! temp-counter 0) node)
     "UziFunctionNode" (fn [node] (reset! temp-counter 0) node)

     "UziForNode" ; Some for-loops declare a temporary variable.
     (fn [{:keys [step] :as node}]
       (if (ast-utils/compile-time-constant? step)
         node
         (assoc node :temp-name (str "@" (swap! temp-counter inc)))))

     "UziRepeatNode" ; All repeat-loops declare a temporary variable
     (fn [node]
       (assoc node :temp-name (str "@" (swap! temp-counter inc))))

     "UziVariableDeclarationNode"
     (fn [var]
       (if (contains? globals var)
         var
         (assoc var :unique-name (str (:name var) "#" (swap! counter inc))))))))

(defn- assign-internal-ids
  "This function is important because it will guarantee that all nodes are different
   when compared with =. Due to clojure's philosophy regarding values, identity, and
   equality I need to do this to be able to distinguish two otherwise equal nodes.
   This is particularly crucial for the variables-in-scope function because it relies
   on = to know when to stop looking for variables.
   An alternative could be to use identical? instead of = but I feel it would make
   the code more fragile than simply adding this artificial :internal-id"
  [ast]
  (ast-utils/transform ast :default #(assoc % :internal-id (.toString (java.util.UUID/randomUUID)))))

(defn compile-tree
  ([ast] (compile-tree ast boards/UNO))
  ([ast board]
   (-> ast
       linker/bind-primitives
       assign-unique-variable-names
       assign-internal-ids
       (compile (create-context board)))))

(defn compile-json-string [str]
  (compile-tree (parse-string str true)))
