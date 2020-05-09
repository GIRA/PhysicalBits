(ns middleware.compiler.checker
  (:require [middleware.compiler.ast-utils :as ast-utils]
            [middleware.compiler.primitives :as prims]
            [clojure.data :as data]))

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

(defn assert-variable-declaration [node errors]
  (assert (ast-utils/variable-declaration? node)
          "Variable declaration expected"
          node errors))

(defn assert-block [node errors]
  (assert (ast-utils/block? node)
          "Block expected"
          node errors))

(defn assert-script [node errors]
  (assert (ast-utils/script? node)
          "Script expected"
          node errors))

(defn assert-literal [node errors]
  (assert (ast-utils/compile-time-constant? node)
          "Literal expected"
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
      ))

  (let [globals (atom #{})]
    (doseq [global (:globals node)]
      (assert-variable-declaration global errors)
      (when-let [value (:value global)]
        (assert-literal value errors))
      (assert (not (contains? @globals (:name global)))
              "Global variable already declared"
              global errors)
      (swap! globals conj (:name global))))

  (let [scripts (atom #{})]
    (doseq [script (:scripts node)]
      (assert-script script errors)
      (assert (not (contains? @scripts (:name script)))
              "Script name already in use"
              script errors)
      (swap! scripts conj (:name script)))))

(defn- check-script-args [node errors path]
  (let [args (atom #{})]
    (doseq [arg (:arguments node)]
      (assert (not (contains? @args (:name arg)))
              "Argument name already specified"
              arg errors)
      (swap! args conj (:name arg)))))


(defmethod check-node "UziTaskNode" [node errors path]
  (check-script-args node errors path)
  (when-let [ticking-rate (:tickingRate node)]
    (assert (and (:state node)
                 (not= "once" (:state node)))
            "Ticking rate is not valid if task state is not specified"
            ticking-rate errors)))

(defmethod check-node "UziProcedureNode" [node errors path]
  (check-script-args node errors path))

(defmethod check-node "UziFunctionNode" [node errors path]
  (check-script-args node errors path))

(defmethod check-node "UziTickingRateNode" [node errors path]
  (assert (> (:value node) 0)
          "Ticking rate must be a positive value"
          node errors))

(defn- check-primitive-call [node errors path]
  )

(defn- contains-all? [a b]
  (nil? (first (data/diff (set b) (set a)))))

(defn- check-script-call [node errors path]
  (let [script (ast-utils/script-named (:selector node) path)]
    (assert script
            "Invalid script"
            node errors)
    (let [call-args (map :key (:arguments node))
          script-args (map :name (:arguments script))]
      ; TODO(Richo): I don't understand why I check <=
      (assert (<= (count call-args)
                  (count script-args))
              "Invalid number of arguments"
              node errors)
      (assert (or (contains-all? script-args call-args)
                  (every? nil? call-args))
              "Explicit argument names expected"
              node errors))))

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
  (when-let [script (first (filter ast-utils/script? path))]
    (when (not (some #(= % node) (:arguments script)))
      (let [local-names (set (map :name (ast-utils/locals-in-scope path)))]
        (assert (not (contains? local-names (:name node)))
              "Variable already declared"
              node errors)))))

(defmethod check-node "UziVariableNode" [node errors path]
  (if-let [import (first (filter ast-utils/import? path))]
    ; TODO(Richo): Special case when we're inside an import init block. Maybe I
    ; should handle this in ast-utils/variable-named since the init blocks have
    ; special scoping rules. I'll think about it and refactor later, if necessary.
    (assert (contains? (set (map :name (-> import :program :globals)))
                       (:name node))
            "Undefined variable found"
            node errors)
    (assert (ast-utils/variable-named (:name node) path)
            "Undefined variable found"
            node errors)))

(defmethod check-node "UziRepeatNode" [node errors path]
  (assert-expression (:times node) errors)
  (assert-block (:body node) errors))

(defmethod check-node "UziForeverNode" [node errors path]
  (assert-block (:body node) errors))

(defmethod check-node "UziForNode" [node errors path]
  (assert-variable-declaration (:counter node) errors)
  (assert-expression (:start node) errors)
  (assert-expression (:step node) errors)
  (assert-expression (:stop node) errors)
  (assert-block (:body node) errors))



(defmethod check-node "UziImportNode" [node errors path]
  (when-let [init-block (:initializationBlock node)]
    (assert-block init-block errors)
    (doseq [stmt (:statements init-block)]
      (assert-statement stmt errors)
      (cond
        (ast-utils/assignment? stmt) (assert-literal (:right stmt) errors)
        ))))

(defmethod check-node "UziPrimitiveDeclarationNode" [node errors path]
  (assert (string? (:alias node))
          "Invalid alias"
          node errors)
  (assert (string? (:name node))
          "Invalid name"
          node errors)
  (assert (prims/primitive (:name node))
          "Primitive not found"
          node errors))

(defmethod check-node "UziReturnNode" [node errors path]
  (when-let [value (:value node)]
    (assert-expression value errors)))

(defmethod check-node "UziYieldNode" [node errors path]
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
  ; TODO(Richo): Special case when we're inside an import init block. Maybe I
  ; should handle this in ast-utils/scripts since the init blocks have special
  ; scoping rules. I'll think about it and refactor later, if necessary.
  (let [valid-script-names (if-let [import (first (filter ast-utils/import? path))]
                             (set (map :name (-> import :program :scripts)))
                             (set (map :name (ast-utils/scripts path))))
        ; TODO(Richo): Yeah, this sucks... I'll make it work first and then clean up.
        script-named (fn [script-name path]
                       (if-let [import (first (filter ast-utils/import? path))]
                         (first (filter #(= script-name (:name %)) (-> import :program :scripts)))
                         (ast-utils/script-named script-name path)))]
    (doseq [script-name (:scripts node)]
      (assert (contains? valid-script-names script-name)
              (str "Invalid script: " script-name)
              node errors)
      (assert (ast-utils/task? (script-named script-name path))
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
     (defn format [ast]
       (ast-utils/transform (dissoc ast :primitives)
                            :default (fn [n _] (dissoc n :internal-id))))
     (def ast (parse "import t from 'test_16.uzi' { a = 1; }
                      task main() { t.a = 12; }"))
     (def ast (ast-utils/assign-internal-ids ast))
     (def ast (middleware.compiler.linker/resolve-imports ast "../../uzi/tests")))
  (pprint (format ast))
  (map :description (check-tree ast))
  (pprint (middleware.compiler.compiler/compile-tree ast))

  )
