; TODO(Richo): Most of the work here could probably be written using clojure.spec
(ns middleware.compilation.checker
  (:refer-clojure :exclude [assert])
  (:require [middleware.utils.core :refer [seek]]
            [middleware.ast.utils :as ast-utils]
            [middleware.ast.primitives :as prims]
            [petitparser.token :as t]
            [clojure.set :as set]))

; TODO(Richo): Improve error descriptions so that they can be translated, maybe
; just storing the description as a format-string with the argument data?
(defn ^:private register-error! [description node errors]
  (vswap! errors conj {:node node
                       :src (when-let [token (get (meta node) :token)]
                              (t/input-value token))
                       :description description}))

(defn ^:private assert [bool description node errors]
  (when-not bool
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

(defn ^:private assert-no-duplicates [coll key-fn msg errors]
  (let [set (volatile! #{})]
    (doseq [each coll]
      (assert (not (contains? @set (key-fn each)))
              msg each errors)
      (vswap! set conj (key-fn each)))))

(defn assert-single-task [node errors]
  (let [tasks (filterv ast-utils/task? (:scripts node))]
    (assert (<= (count tasks) 1)
            "Only 1 task is supported"
            (second tasks)
            errors)))

(defn check-program [node errors path]
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

(defn ^:private check-script [node errors path]
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

(defn check-ticking-rate [{:keys [^double value] :as node} errors path]
  (assert (> value 0)
          "Ticking rate must be a positive value"
          node errors))

(defn ^:private check-primitive-call [node errors path]
  (let [prim (prims/primitive (:primitive-name node))]
    (assert (some? prim)
            (str "Invalid primitive '" (:selector node) "'")
            node errors)
    (when (some? prim)
      (let [nargs-expected (first (:stack-transition prim))
            nargs-provided (count (:arguments node))]
        (assert (= nargs-expected nargs-provided)
                (str "Calling '" (:selector node)
                     "' with " nargs-provided
                     " argument" (if (= 1 nargs-provided) "" "s")
                     " (expected: " nargs-expected ")")
                node errors)))))

(defn ^:private contains-all? [a b]
  (set/subset? (set b) (set a)))

(defn ^:private check-script-call [node errors path]
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

(defn check-call [node errors path]
  (doseq [arg (map :value (:arguments node))]
    (assert-expression arg errors))
  (if (get node :primitive-name)
    (check-primitive-call node errors path)
    (check-script-call node errors path)))

(defn check-block [node errors path]
  (doseq [stmt (:statements node)]
    (assert-statement stmt errors)))


(defn check-number-literal [node errors path]
  (assert (number? (:value node))
          "Number expected"
          node
          errors))

(defn check-pin-literal [node errors path]
  (assert (contains? #{"D" "A"} (:type node))
          "Invalid pin type"
          node
          errors)
  (assert (int? (:number node))
          "Integer expected"
          node
          errors))

(defn check-conditional [node errors path]
  (assert-expression (:condition node) errors)
  (assert-block (:trueBranch node) errors)
  (assert-block (:falseBranch node) errors))

(defn check-variable-declaration [node errors path]
  (when-let [script (seek ast-utils/script? path)]
    (let [local-names (set (map :name (ast-utils/locals-in-scope path)))]
      (assert (not (contains? local-names (:name node)))
              "Variable already declared"
              node errors)))
  (when-let [value (:value node)]
    (assert-expression value errors)))

(defn check-variable [node errors path]
  (assert (ast-utils/variable-named (:name node) path)
          "Undefined variable found"
          node errors))

(defn check-repeat [node errors path]
  (assert-expression (:times node) errors)
  (assert-block (:body node) errors))

(defn check-forever [node errors path]
  (assert-block (:body node) errors))

(defn check-for [node errors path]
  (assert-variable-declaration (:counter node) errors)
  (assert-expression (:start node) errors)
  (assert-expression (:step node) errors)
  (assert-expression (:stop node) errors)
  (assert-block (:body node) errors))

(defn check-import [node errors path]
  (when-let [init-block (:initializationBlock node)]
    (assert-block init-block errors)
    (doseq [stmt (:statements init-block)]
      (assert-statement stmt errors)
      (when (ast-utils/assignment? stmt)
        (assert-literal (:right stmt) errors)))))

(defn check-primitive-declaration [node errors path]
  (assert (string? (:alias node))
          "Invalid alias"
          node errors)
  (assert (string? (:name node))
          "Invalid name"
          node errors)
  (assert (prims/primitive (:name node))
          "Primitive not found"
          node errors))

(defn check-return [node errors path]
  (when-let [value (:value node)]
    (assert-expression value errors)))

(defn ^:private check-conditional-loop [node errors path]
  (assert-block (:pre node) errors)
  (assert-expression (:condition node) errors)
  (assert-block (:post node) errors))

(defn ^:private check-logical-operator [node errors path]
  (assert-expression (:left node) errors)
  (assert-expression (:right node) errors))

(defn check-assignment [node errors path]
  (assert-variable (:left node) errors)
  (assert-expression (:right node) errors))

(defn ^:private check-script-control [node errors path]
  (let [valid-script-names (set (map :name (ast-utils/scripts path)))]
    (doseq [script-name (:scripts node)]
      (assert (contains? valid-script-names script-name)
              (str "Invalid script: " script-name)
              node errors)
      (assert (ast-utils/task? (ast-utils/script-named script-name path))
              "Task reference expected"
              node errors))))

(defn check-node [node errors path]
  (case (ast-utils/node-type node)
    "UziProgramNode" (check-program node errors path)
    "UziTaskNode" (check-script node errors path)
    "UziProcedureNode" (check-script node errors path)
    "UziFunctionNode" (check-script node errors path)
    "UziTickingRateNode" (check-ticking-rate node errors path)
    "UziCallNode" (check-call node errors path)
    "UziBlockNode" (check-block node errors path)
    "UziNumberLiteralNode" (check-number-literal node errors path)
    "UziPinLiteralNode" (check-pin-literal node errors path)
    "UziConditionalNode" (check-conditional node errors path)
    "UziVariableDeclarationNode" (check-variable-declaration node errors path)
    "UziVariableNode" (check-variable node errors path)
    "UziRepeatNode" (check-repeat node errors path)
    "UziForeverNode" (check-forever node errors path)
    "UziForNode" (check-for node errors path)
    "UziImportNode" (check-import node errors path)
    "UziPrimitiveDeclarationNode" (check-primitive-declaration node errors path)
    "UziReturnNode" (check-return node errors path)
    "UziWhileNode" (check-conditional-loop node errors path)
    "UziDoWhileNode" (check-conditional-loop node errors path)
    "UziUntilNode" (check-conditional-loop node errors path)
    "UziDoUntilNode" (check-conditional-loop node errors path)
    "UziLogicalAndNode" (check-logical-operator node errors path)
    "UziLogicalOrNode" (check-logical-operator node errors path)
    "UziAssignmentNode" (check-assignment node errors path)
    "UziScriptStartNode" (check-script-control node errors path)
    "UziScriptStopNode" (check-script-control node errors path)
    "UziScriptResumeNode" (check-script-control node errors path)
    "UziScriptPauseNode" (check-script-control node errors path)
    nil))

(defn ^:private external-script? [node ast]
  (and (ast-utils/script? node)
       (not (identical? (-> ast meta :token :source)
                        (-> node meta :token :source)))))

(defn ^:private check [node errors ast path]
  (when-not (external-script? node ast)
    (let [new-path (conj path node)]
      (check-node node errors new-path)
      (doseq [child-node (ast-utils/children node)]
        (check child-node errors ast new-path)))))

(defn ^:private check-with-external [node errors ast path]
  (let [new-path (conj path node)]
    (check-node node errors new-path)
    (doseq [child-node (ast-utils/children node)]
      (check-with-external child-node errors ast new-path))))

(defn check-tree
  ([ast] (check-tree ast true true))
  ([ast check-external-code? concurrency-enabled?]
   (let [errors (volatile! [])
         path (list)]
     (if check-external-code?
       (check-with-external ast errors ast path)
       (check ast errors ast path))
     (when-not concurrency-enabled?
       (assert-single-task ast errors))
     @errors)))