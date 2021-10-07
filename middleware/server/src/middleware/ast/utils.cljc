(ns middleware.ast.utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj]
            [clojure.walk :as w]
            [middleware.utils.core :refer [seek]]
            [middleware.ast.primitives :as prims]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn node-type [node]
  (:__class__ node))

(defn compile-time-constant? [node]
  (contains? #{"UziNumberLiteralNode" "UziPinLiteralNode"}
             (node-type node)))

(defn compile-time-value ^double [node not-constant]
  (case (node-type node)
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

(defn variable-declaration? [node]
  (= "UziVariableDeclarationNode" (node-type node)))

(defn task? [node]
  (= "UziTaskNode" (node-type node)))

(defn script? [node]
  (contains? #{"UziTaskNode"
               "UziFunctionNode"
               "UziProcedureNode"}
             (node-type node)))

(defn import? [node]
  (= "UziImportNode" (node-type node)))

(defn assignment? [node]
  (= "UziAssignmentNode" (node-type node)))

(defn primitive-declaration? [node]
  (= "UziPrimitiveDeclarationNode" (node-type node)))

(defn ticking-rate? [node]
  (= "UziTickingRateNode" (node-type node)))

(defn pin-literal? [node]
  (= "UziPinLiteralNode" (node-type node)))

(defn number-literal? [node]
  (= "UziNumberLiteralNode" (node-type node)))

(defn call? [node]
  (= "UziCallNode" (node-type node)))

(defn has-side-effects? [{:keys [primitive-name arguments] :as node}]
  (if (call? node)
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
  (case (node-type node)
    "UziScriptStartNode" "running"
    "UziScriptResumeNode" "running"
    "UziScriptStopNode" "stopped"
    "UziScriptPauseNode" "stopped"
    nil))

(defn scripts [path]
  ; NOTE(Richo): If we're inside an import's initialization block we have special
  ; scope rules. We need to look inside the imported program if the import has
  ; been resolved, and fail if it wasn't. The only scripts we can access are
  ; the imported program scripts.
  (if-let [imp (seek import? path)]
    (if-let [{program :program} (meta imp)]
      (-> program :scripts)
      (throw (ex-info "Unresolved import" imp)))
    (-> path last :scripts)))

(defn script-named [name path]
  (seek #(= name (:name %))
        (scripts path)))

(defn children-keys [node]
  (case (node-type node)
    "UziAssignmentNode" [:left :right]
    "UziBlockNode" [:statements]
    "UziCallNode" [:arguments]
    "UziConditionalNode" [:condition :trueBranch :falseBranch]
    "UziForNode" [:counter :start :stop :step :body]
    "UziForeverNode" [:body]
    "UziImportNode" [:initializationBlock]
    "UziLogicalAndNode" [:left :right]
    "UziLogicalOrNode" [:left :right]
    "UziLoopNode" [:pre :condition :post]
    "UziWhileNode" [:pre :condition :post]
    "UziUntilNode" [:pre :condition :post]
    "UziDoWhileNode" [:pre :condition :post]
    "UziDoUntilNode" [:pre :condition :post]
    "UziProgramNode" [:imports :globals :scripts :primitives]
    "UziRepeatNode" [:times :body]
    "UziReturnNode" [:value]
    "UziTaskNode" [:arguments :tickingRate :body]
    "UziProcedureNode" [:arguments :body]
    "UziFunctionNode" [:arguments :body]
    "UziVariableDeclarationNode" [:value]
    "Association" [:value]
    []))

(defn valid-keys [node]
  "This function returns the keys for this node which, when evaluated
   return a non-null value."
  (clj/filter #(some? (node %))
              (children-keys node)))

(defn children [ast]
  (reduce (fn [acc key]
            (if-let [node (key ast)]
              (if (sequential? node)
                (apply conj acc node)
                (conj acc node))
              acc))
          []
          (children-keys ast)))

(defn all-children [ast]
  (reduce (fn [acc next]
            (concat acc (all-children next)))
          [ast]
          (children ast)))

(defn filter [ast & types]
  (let [type-set (set types)]
    (clj/filter #(type-set (node-type %))
                (all-children ast))))

(defn- evaluate-pred-clauses [node path clauses]
  "Evaluates each clause in order. The clauses are pairs of pred-fn/expr-fn.
   Both functions accept the node and its path as arguments. If a pred-fn
   return true it will evaluate expr-fn and return its result.
   The optional :default clause will match any node, but only if no previous
   matching clause was found."
  (loop [[pred result-fn & rest] clauses]
    (if-not pred
      node
      (if (or (= :default pred)
              (pred node path))
        (result-fn node path)
        (recur rest)))))

(defn- replace-children [node keys expr-fn]
  "Updates node by replacing each child on the given keys with the
  result of evaluating expr-fn passing the child node as argument."
  (reduce (fn [result key]
            (let [old (result key)
                  new (expr-fn old)]
              (if (= old new)
                result
                (assoc result key new))))
          node
          keys))

(defn- transformp* [ast path clauses]
  "I made this function because clojure.walk doesn't traverse the tree
   in the order I need. Also, with this function I can keep track of the
   path of parent nodes, which is very useful.
   This function is private, you should call transformp or transform"
  (cond

    ; If we find a vector, we simply recursively transform each element
    ; and return a new vector.
    (vector? ast)
    (mapv #(transformp* % path clauses)
          ast)

    ; If we find a node, we evaluate each clause in order and if we find
    ; a match we replace the node with the result before recursively
    ; transforming its children.
    ; Here we take care of keeping track of the path so that we can pass
    ; it as argument for both the predicates and the transforming functions.
    (node? ast)
    (let [new-path (conj path ast)
          node (evaluate-pred-clauses ast
                                      new-path
                                      clauses)]
      (replace-children node
                        (valid-keys node)
                        (fn [child]
                          (transformp* child
                                       new-path
                                       clauses))))

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
  ; NOTE(Richo): If we're inside an import's initialization block we have special
  ; scope rules. We need to look inside the imported program if the import has
  ; been resolved, and fail if it wasn't. The only variables we can access are
  ; the imported program globals.
  (if-let [imp (seek import? path)]
    (if-let [{program :program} (meta imp)]
      (-> program :globals)
      (throw (ex-info "Unresolved import" imp)))
    (mapcat (fn [[first second]]
              (clj/filter #(= "UziVariableDeclarationNode" (node-type %))
                          (take-while #(not (= % first))
                                      (children second))))
            (partition 2 1 path))))

(defn locals-in-scope [path]
  ; NOTE(Richo): We take advantage of the fact that globals can only be defined
  ; in the root of the AST, which should always be the last item in the path.
  (variables-in-scope (drop-last path)))

(defn variable-named
  "Returns the variable declaration referenced by this name at this point in the ast"
  [name path]
  (seek #(= name (:name %))
        (variables-in-scope path)))

(defn global?
  "Works for both variable and variable-declaration nodes."
  [node path]
  (let [globals (-> path last :globals set)]
    (case (node-type node)
      "UziVariableNode" (let [variable (variable-named (:name node) path)]
                          (contains? globals variable))
      "UziVariableDeclarationNode" (contains? globals node))))

(def local? (complement global?))
