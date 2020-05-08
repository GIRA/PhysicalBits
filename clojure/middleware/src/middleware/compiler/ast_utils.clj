(ns middleware.compiler.ast-utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj-core]
            [clojure.walk :as w]
            [middleware.compiler.primitives :as prims]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn node-type [node]
  (get node :__class__))

(defn compile-time-constant? [node]
  (contains? #{"UziNumberLiteralNode" "UziPinLiteralNode"}
             (node-type node)))

(defn compile-time-value [node not-constant]
  (condp = (node-type node)
    "UziNumberLiteralNode" (:value node)
    "UziPinLiteralNode" (:value node)
    not-constant))

(defn expression? [{:keys [primitive-name] :as node}]
  (let [type (node-type node)]
    (if (= "UziCallNode" type) ; Special case for calls
      (if-let [{[_ after] :stack-transition} (prims/primitive primitive-name)]
        (= 1 after)
        true)
      (contains? #{"UziLogicalAndNode"
                   "UziLogicalOrNode"
                   "UziNumberLiteralNode"
                   "UziPinLiteralNode"
                   "UziVariableNode"}
                 type))))

(defn statement? [{:keys [primitive-name] :as node}]
  (let [type (node-type node)]
    (if (= "UziCallNode" type) ; Special case for calls
      (if-let [{[_ after] :stack-transition} (prims/primitive primitive-name)]
        (not= 1 after)
        true)
      (contains? #{"UziAssignmentNode"
                   "UziConditionalNode"
                   "UziForNode"
                   "UziForeverNode"
                   "UziWhileNode"
                   "UziDoWhileNode"
                   "UziUntilNode"
                   "UziDoUntilNode"
                   "UziRepeatNode"
                   "UziReturnNode"
                   "UziScriptStartNode"
                   "UziScriptStopNode"
                   "UziScriptResumeNode"
                   "UziScriptPauseNode"
                   "UziVariableDeclarationNode"
                   "UziYieldNode"}
                 type))))

(defn block? [node]
  (= "UziBlockNode" (node-type node)))

(defn variable? [node]
  (= "UziVariableNode" (node-type node)))

(defn task? [node]
  (= "UziTaskNode" (node-type node)))

(defn has-side-effects? [{:keys [primitive-name arguments] :as node}]
  (if (= "UziCallNode" (node-type node))
    (if-not primitive-name
      true ; Script calls could always have side-effects
      (if (some has-side-effects? (map :value arguments))
        true
        (if-let [{[_ after] :stack-transition} (prims/primitive primitive-name)]
          (not= 1 after)
          true)))
    (not (expression? node))))

(defn script-control? [node]
  (contains? #{"UziScriptStopNode" "UziScriptStartNode"
               "UziScriptPauseNode" "UziScriptResumeNode"}
             (node-type node)))

(defn script-control-state [node]
  (condp = (node-type node)
    "UziScriptStartNode" "running"
    "UziScriptResumeNode" "running"
    "UziScriptStopNode" "stopped"
    "UziScriptPauseNode" "stopped"
    nil))

(defn scripts [path]
  (-> path last :scripts))

(defn script-named [name path]
  (first (clj-core/filter #(= name (:name %))
                          (scripts path))))

(defmulti ^:private children-keys :__class__)
(defmethod children-keys "UziAssignmentNode" [_] [:left :right])
(defmethod children-keys "UziBlockNode" [_] [:statements])
(defmethod children-keys "UziCallNode" [_] [:arguments])
(defmethod children-keys "UziConditionalNode" [_] [:condition :trueBranch :falseBranch])
(defmethod children-keys "UziForNode" [_] [:counter :start :stop :step :body])
(defmethod children-keys "UziForeverNode" [_] [:body])
(defmethod children-keys "UziImportNode" [_] [:initializationBlock])
(defmethod children-keys "UziLogicalAndNode" [_] [:left :right])
(defmethod children-keys "UziLogicalOrNode" [_] [:left :right])
(defmethod children-keys "UziLoopNode" [_] [:pre :condition :post])
(defmethod children-keys "UziWhileNode" [_] [:pre :condition :post])
(defmethod children-keys "UziUntilNode" [_] [:pre :condition :post])
(defmethod children-keys "UziDoWhileNode" [_] [:pre :condition :post])
(defmethod children-keys "UziDoUntilNode" [_] [:pre :condition :post])
(defmethod children-keys "UziProgramNode" [_] [:imports :globals :scripts :primitives])
(defmethod children-keys "UziRepeatNode" [_] [:times :body])
(defmethod children-keys "UziReturnNode" [_] [:value])
(defmethod children-keys "UziTaskNode" [_] [:arguments :tickingRate :body])
(defmethod children-keys "UziProcedureNode" [_] [:arguments :body])
(defmethod children-keys "UziFunctionNode" [_] [:arguments :body])
(defmethod children-keys "UziVariableDeclarationNode" [_] [:value])
(defmethod children-keys "Association" [_] [:value])
(defmethod children-keys :default [_] [])

(defn valid-keys [node]
  "This function returns the keys for this node which, when evaluated
   return a non-null value."
  (clj-core/filter #(not (nil? (node %)))
                   (children-keys node)))

(defn children [ast]
  (filterv (complement nil?)
           (flatten (map (fn [key] (key ast))
                         (children-keys ast)))))

(defn all-children [ast]
  (concat [ast]
          (mapcat all-children
                  (children ast))))

(defn filter [ast & types]
  (let [type-set (set types)]
    (clj-core/filter #(type-set (node-type %))
                     (all-children ast))))

(defn- evaluate-pred-clauses [node path clauses]
  "Evaluates each clause in order. The clauses are pairs of pred-fn/expr-fn.
   Both functions accept the node and its path as arguments. If a pred-fn
   return true it will evaluate expr-fn and return its result.
   The optional :default clause will match any node, but only if no previous
   matching clause was found."
  (loop [[pred result-fn & rest] clauses]
    (if (or (= :default pred)
            (pred node path))
      (result-fn node path)
      (if (empty? rest)
        node
        (recur rest)))))

(defn- replace-children [node keys expr-fn]
  "Updates node by replacing each child on the given keys with the
  result of evaluating expr-fn passing the child node as argument."
  (loop [keys keys, result node]
    (let [[first & rest] keys]
      (if first
        (let [new-result (assoc result
                                first
                                (expr-fn (result first)))]
          (if (empty? rest)
            new-result
            (recur rest new-result)))
        result))))

(defn- transformp* [ast path clauses]
  "I made this function because clojure.walk doesn't traverse the tree
   in the order I need. Also, with this function I can keep track of the
   path of parent nodes, which is very useful.
   This function is private, you should call transformp or transform"
  (cond
    ; If we find a node, we evaluate each clause in order and if we find
    ; a match we replace the node with the result before recursively
    ; transforming its children.
    ; Here we take care of keeping track of the path so that we can pass
    ; it as argument for both the predicates and the transforming functions.
    (node? ast)
    (let [node (evaluate-pred-clauses ast
                                      (conj path ast)
                                      clauses)]
      (replace-children node
                        (valid-keys node)
                        (fn [child]
                          (transformp* child
                                       (conj path node)
                                       clauses))))

    ; If we find a vector, we simply recursively transform each element
    ; and return a new vector.
    (vector? ast)
    (mapv #(transformp* % path clauses)
          ast)

    ; Anything else is simply returned without any transformation
    :else ast))

(defn transformp [ast & clauses]
  "This function lets you traverse the tree and transform any of its nodes.
   The clauses are pairs of pred-fn/expr-fn. It will traverse the tree and
   evaluate each clause's predicate in order. If the predicate evaluates to
   true, it will evaluate expr-fn. Both pred-fn and expr-fn should accept
   two arguments: the node and its path as arguments. The path is a list
   containing all the parent nodes already traversed in the tree. If no
   clause is valid, it will optionally accept a :default clause.
   The original node will be replaced with the result of evaluating expr-fn
   before continuing the traversal."
  (transformp* ast (list) clauses))


(defn transform [ast & clauses]
  "This function is exactly like transformp but instead of predicates it matches
  node-types"
  (let [as-pred (fn [type]
                  (if (= :default type)
                    type
                    (fn [node _] (= type (node-type node)))))]
    (apply transformp
      ast
      (mapcat (fn [[type result-fn]]
                [(as-pred type) result-fn])
              (partition 2 clauses)))))

(defn variables-in-scope
  "Returns all the variable declarations up to this point in the ast"
  [path]
  (mapcat (fn [[first second]]
            (clj-core/filter #(= "UziVariableDeclarationNode" (node-type %))
                             (take-while #(not (= % first))
                                         (children second))))
          (partition 2 1 path)))

(defn variable-named
  "Returns the variable declaration referenced by this name at this point in the ast"
  [name path]
  (first (clj-core/filter #(= name (:name %))
                          (variables-in-scope path))))

(defn global?
  "Works for both variable and variable-declaration nodes."
  [node path]
  (let [globals (-> path last :globals set)]
    (condp = (node-type node)
      "UziVariableNode"
      :>> (fn [_] (let [variable (variable-named (:name node) path)]
                    (contains? globals variable)))
      "UziVariableDeclarationNode"
      :>> (fn [_] (contains? globals node)))))

(def local? (complement global?))

(defn assign-internal-ids
  "This function is important because it will guarantee that all nodes are different
   when compared with =. Due to clojure's philosophy regarding values, identity, and
   equality I need to do this to be able to distinguish two otherwise equal nodes.
   This is particularly crucial for the variables-in-scope function because it relies
   on = to know when to stop looking for variables.
   An alternative could be to use identical? instead of = but I feel it would make
   the code more fragile than simply adding this artificial :internal-id"
  [ast]
  (transform ast
             :default (fn [node _] (assoc node :internal-id (.toString (java.util.UUID/randomUUID))))))
