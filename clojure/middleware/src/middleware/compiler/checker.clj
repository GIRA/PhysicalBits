(ns middleware.compiler.checker
  (:require [middleware.compiler.ast-utils :as ast-utils]))

(defn- register-error! [description node errors]
  (swap! errors conj {:node node
                      :description description}))

(defn- assert [bool description node errors]
  (when (not bool)
    (register-error! description node errors))
  bool)

(defn assert-statement [node errors]
  (assert (ast-utils/statement? node)
          "Statement expected"
          node errors))

(defn assert-expression [node errors]
  (assert (ast-utils/expression? node)
          "Expression expected"
          node errors))

(defn assert-variable [node errors]
  (assert (ast-utils/variable? node)
          "Variable expected"
          node errors))

(defn assert-block [node errors]
  (assert (ast-utils/block? node)
          "Block expected"
          node errors))

(defmulti check-node (fn [node errors path] (:__class__ node)))

(defn- check [node errors path]
  (let [new-path (conj path node)]
    (check-node node errors new-path)
    (doseq [child-node (ast-utils/children node)]
      (check child-node errors new-path))))

(defmethod check-node "UziProgramNode" [node errors path]
  (let [imports (:imports node)]
    (doseq [import imports]
      ; TODO(Richo): ?
      )))

(defmethod check-node "UziTaskNode" [node errors path]
  )

(defn- check-primitive-call [node errors path]
  )

(defn- check-script-call [node errors path]
  (assert (ast-utils/script-named (:selector node) path)
          "Invalid script"
          node errors))

(defmethod check-node "UziCallNode" [node errors path]
  (doseq [arg (map :value (:arguments node))]
    (assert-expression arg errors))
  (if (get node :primitive-name)
    (check-primitive-call node errors path)
    (check-script-call node errors path)))

(defmethod check-node "UziBlockNode" [node errors path]
  (doseq [stmt (:statements node)]
    (assert-statement stmt errors)))


(defmethod check-node "UziNumberLiteralNode" [node errors path]
  (assert (number? (:value node))
          "Number expected"
          node
          errors))

(defmethod check-node "UziPinLiteralNode" [node errors path]
  (assert (contains? #{"D" "A"} (:type node))
          "Invalid pin type"
          node
          errors)
  (assert (pos-int? (:number node))
          "Positive integer expected"
          node
          errors))

(defmethod check-node "UziConditionalNode" [node errors path]
  (assert-expression (:condition node) errors)
  (assert-block (:trueBranch node) errors)
  (assert-block (:falseBranch node) errors))

(defmethod check-node "UziVariableDeclarationNode" [node errors path]
  )

(defmethod check-node "UziVariableNode" [node errors path]
  )

(defmethod check-node "UziImportNode" [node errors path]
  )

(defmethod check-node "UziPrimitiveDeclarationNode" [node errors path]
  )

(defmethod check-node "UziProcedureNode" [node errors path]
  )

(defmethod check-node "UziFunctionNode" [node errors path]
  )

(defmethod check-node "UziReturnNode" [node errors path]
  )

(defn- check-conditional-loop [node errors path]
  (assert-block (:pre node) errors)
  (assert-expression (:condition node) errors)
  (assert-block (:post node) errors))

(defmethod check-node "UziWhileNode" [node errors path]
  (check-conditional-loop node errors path))

(defmethod check-node "UziDoWhileNode" [node errors path]
  (check-conditional-loop node errors path))

(defmethod check-node "UziUntilNode" [node errors path]
  (check-conditional-loop node errors path))

(defmethod check-node "UziDoUntilNode" [node errors path]
  (check-conditional-loop node errors path))

(defmethod check-node "UziAssignmentNode" [node errors path]
  (assert-variable (:left node) errors)
  (assert-expression (:right node) errors))

(defn- check-script-control [node errors path]
  (let [valid-script-names (set (map :name (ast-utils/scripts path)))]
    (doseq [script-name (:scripts node)]
      (assert (contains? valid-script-names script-name)
              (str "Invalid script: " script-name)
              node errors)
      (assert (ast-utils/task? (ast-utils/script-named script-name path))
              "Task reference expected"
              node errors))))

(defmethod check-node "UziScriptStartNode" [node errors path]
  (check-script-control node errors path))

(defmethod check-node "UziScriptStopNode" [node errors path]
  (check-script-control node errors path))

(defmethod check-node "UziScriptResumeNode" [node errors path]
  (check-script-control node errors path))

(defmethod check-node "UziScriptPauseNode" [node errors path]
  (check-script-control node errors path))

(defmethod check-node "Association" [_ _ _]) ; TODO(Richo): Remove

(defmethod check-node :default [node _ _]
  (throw (ex-info "MISSING: " node)))

(defn check-tree [ast]
  (let [errors (atom [])
        path (list)]
    (check ast errors path)
    @errors))

#_(
   (do ; Definitions
     (def parse middleware.parser.parser/parse)
     (def pprint clojure.pprint/pprint)
     (def ast (parse "var a; task foo() stopped { if a = 3 { turnOff(D13); }}"))
     (def ast (middleware.compiler.linker/resolve-imports ast "../../uzi/tests")))
  (pprint (dissoc ast :primitives))
  (check-tree ast)

  )
