; TODO(Richo): Most of the work here could probably be written using clojure.spec
(ns middleware.compiler.checker
  (:refer-clojure :exclude [assert])
  (:require [middleware.compiler.utils.ast :as ast-utils]
            [middleware.compiler.primitives :as prims]
            [petitparser.token :as t]
            [clojure.data :as data]))

(defn- register-error! [description node errors]
  (swap! errors conj {:node node
                      :src (if-let [token (get (meta node) :token)]
                             (t/input-value token))
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

(defn assert-import [node errors]
  (assert (ast-utils/import? node)
          "Literal expected"
          node errors))

(defn assert-primitive [node errors]
  (assert (ast-utils/primitive-declaration? node)
          "Primitive expected"
          node errors))

(defn assert-ticking-rate [node errors]
  (assert (ast-utils/ticking-rate? node)
          "Ticking rate expected"
          node errors))

(defn- assert-no-duplicates [coll key-fn msg errors]
  (let [set (atom #{})]
    (doseq [each coll]
      (assert (not (contains? @set (key-fn each)))
              msg each errors)
      (swap! set conj (key-fn each)))))

(defmulti check-node (fn [node errors path] (:__class__ node)))

(defn- check [node errors path]
  (let [new-path (conj path node)]
    (check-node node errors new-path)
    (doseq [child-node (ast-utils/children node)]
      (check child-node errors new-path))))

(defmethod check-node "UziProgramNode" [node errors path]
  (doseq [import (:imports node)]
    (assert-import import errors))
  (assert-no-duplicates (:imports node)
                        :alias
                        "Library already imported"
                        errors)
  (doseq [global (:globals node)]
    (assert-variable-declaration global errors)
    (when-let [value (:value global)]
      (assert-literal value errors)))
  (assert-no-duplicates (:globals node)
                        :name
                        "Global variable already declared"
                        errors)
  (doseq [script (:scripts node)]
    (assert-script script errors))
  (assert-no-duplicates (:scripts node)
                        :name
                        "Script name already in use"
                        errors)
  (doseq [prim (:primitives node)]
    (assert-primitive prim errors)))

(defn- check-script [node errors path]
  (assert-no-duplicates (:arguments node)
                        :name
                        "Argument name already specified"
                        errors)
  (when-let [ticking-rate (:tickingRate node)]
    (assert-ticking-rate ticking-rate errors)
    (assert (and (:state node)
                 (not= "once" (:state node)))
            "Ticking rate is not valid if task state is not specified"
            ticking-rate errors))
  (assert-block (:body node) errors))

(defmethod check-node "UziTaskNode" [node errors path]
  (check-script node errors path))

(defmethod check-node "UziProcedureNode" [node errors path]
  (check-script node errors path))

(defmethod check-node "UziFunctionNode" [node errors path]
  (check-script node errors path))

(defmethod check-node "UziTickingRateNode" [{:keys [^long value] :as node} errors path]
  (assert (> value 0)
          "Ticking rate must be a positive value"
          node errors))

(defn- check-primitive-call [node errors path]
  (let [prim (prims/primitive (:primitive-name node))]
    (assert (some? prim)
            (str "Invalid primitive '" (:selector node) "'")
            node errors)
    (when (some? prim)
      (let [nargs-expected (first (:stack-transition prim))
            nargs-provided (count (:arguments node))]
        (assert (= nargs-expected nargs-provided)
                (format "Calling '%s' with %d argument%s (expected: %d)"
                        (:selector node)
                        nargs-provided
                        (if (= 1 nargs-provided) "" "s")
                        nargs-expected)
                node errors)))))

(defn- contains-all? [a b]
  (nil? (first (data/diff (set b) (set a)))))

(defn- check-script-call [node errors path]
  (let [script (ast-utils/script-named (:selector node) path)]
    (assert script
            "Invalid script"
            node errors)
    (let [call-args (map :key (:arguments node))
          script-args (map :name (:arguments script))]
      ; NOTE(Richo): We can call the script with less arguments than required
      ; because the compiler will just use the default values (for now, zero)
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
  (assert (int? (:number node))
          "Integer expected"
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
              node errors))))
  (when-let [value (:value node)]
    (assert-expression value errors)))

(defmethod check-node "UziVariableNode" [node errors path]
  (assert (ast-utils/variable-named (:name node) path)
          "Undefined variable found"
          node errors))

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
      (when (ast-utils/assignment? stmt)
        (assert-literal (:right stmt) errors)))))

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

(defn- check-logical-operator [node errors path]
  (assert-expression (:left node) errors)
  (assert-expression (:right node) errors))

(defmethod check-node "UziLogicalAndNode" [node errors path]
  (check-logical-operator node errors path))

(defmethod check-node "UziLogicalOrNode" [node errors path]
  (check-logical-operator node errors path))

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

(defmethod check-node :default [_ _ _])

(defn check-tree [ast]
  (let [errors (atom [])
        path (list)]
    (check ast errors path)
    @errors))
