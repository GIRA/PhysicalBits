(ns middleware.ast.utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj]
            [clojure.walk :as w]
            [petitparser.token :as t]
            [middleware.utils.core :refer [seek random-uuid] :as u]
            [middleware.ast.primitives :as prims]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn node-type [node]
  (:__class__ node))

(defn program? [node]
  (= "UziProgramNode" (node-type node)))

(defn compile-time-constant? [node]
  (contains? #{"UziNumberLiteralNode"
               "UziPinLiteralNode"
               "UziStringNode"}
             (node-type node)))

(defn compile-time-value ^double [node not-constant]
  (case (node-type node)
    "UziNumberLiteralNode" (:value node)
    "UziPinLiteralNode" (:value node)
    "UziStringNode" (:index node)
    not-constant))

(defn expression? [{:keys [primitive-name expression?] :as node}]
  (let [type (node-type node)]
    (if (= "UziCallNode" type) ; Special case for calls
      (if-let [{[_ after] :stack-transition} (prims/primitive primitive-name)]
        (= 1 after)
        expression?)
      (contains? #{"UziLogicalAndNode"
                   "UziLogicalOrNode"
                   "UziNumberLiteralNode"
                   "UziPinLiteralNode"
                   "UziVariableNode"
                   "UziStringNode"}
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

(defn procedure? [node]
  (= "UziProcedureNode" (node-type node)))

(defn function? [node]
  (= "UziFunctionNode" (node-type node)))

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

(defn control-structure? [node]
  (contains? #{"UziConditionalNode" "UziForNode"
               "UziForeverNode" "UziLoopNode"
               "UziWhileNode" "UziUntilNode"
               "UziDoWhileNode" "UziDoUntilNode"
               "UziRepeatNode"}
             (node-type node)))

(defn loop? [node]
  (contains? #{"UziLoopNode" ; TODO(Richo): Is UziLoopNode still necessary?
               "UziForNode" "UziForeverNode"
               "UziWhileNode" "UziUntilNode"
               "UziDoWhileNode" "UziDoUntilNode"
               "UziRepeatNode"}
             (node-type node)))

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
    (if-let [program (-> imp meta :program)]
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

(defn- replace-children [node keys expr-fn]
  "Updates node by replacing each child on the given keys with the
  result of evaluating expr-fn passing the child node as argument."
  (reduce (fn [result key]
            (let [old (result key)
                  new (expr-fn old)]
              (if (identical? old new)
                result
                (assoc result key new))))
          node
          keys))

(defn transform
  "This function lets you traverse the tree and transform any of its nodes.
   The action argument should be a function accepting 1 argument: the node 
   to be transformed. The original node will be replaced with the result of 
   evaluating action before continuing the traversal.
   I made this function because clojure.walk doesn't traverse the tree 
   in the order I need."
  [ast action]
  (cond
    ; If we find a vector, we simply recursively transform each element and 
    ; return a new vector.
    (vector? ast)
    (mapv #(transform % action)
          ast)

    ; If we find a node, we replace the node with the result before recursively
    ; transforming its children.
    (node? ast)
    (let [node (action ast)]
      (replace-children node
                        (valid-keys node)
                        #(transform % action)))

    ; Anything else is simply returned without any transformation
    :else ast))

(defn- transform-with-path* 
  "I made this function because clojure.walk doesn't traverse the tree 
   in the order I need. Also, with this function I can keep track of the
   path of parent nodes, which is very useful.
   This function is private, you should call transform-with-path"
  [ast path action]
  (cond
    ; If we find a vector, we simply recursively transform each element
    ; and return a new vector.
    (vector? ast)
    (mapv #(transform-with-path* % path action)
          ast)

    ; If we find a node, we replace the node with the result before recursively
    ; transforming its children.
    ; Here we take care of keeping track of the path so that we can pass
    ; it as argument for the transforming functions.
    (node? ast)
    (let [node (action ast path)
          new-path (conj path node)]
      (replace-children node
                        (valid-keys node)
                        #(transform-with-path* % new-path action)))

    ; Anything else is simply returned without any transformation
    :else ast))

(defn transform-with-path
  "This function lets you traverse the tree and transform any of its nodes.
   The action argument should be a function accepting two arguments: the node 
   and its path.The path is a list containing all the parent nodes already 
   traversed in the tree.
   The original node will be replaced with the result of evaluating action
   before continuing the traversal."
  [ast action]
  (transform-with-path* ast (list) action))

(defn globals-in-scope [path]
  (if-let [imp (seek import? path)]
    (if-let [program (-> imp meta :program)]
      (-> program :globals)
      (throw (ex-info "Unresolved import" imp)))
    (-> path last :globals)))

(defn- variables-in-scope
  "Returns all the variable declarations up to this point in the ast"
  [path]
  ; NOTE(Richo): If we're inside an import's initialization block we have special
  ; scope rules. We need to look inside the imported program if the import has
  ; been resolved, and fail if it wasn't. The only variables we can access are
  ; the imported program globals.
  (if-let [imp (seek import? path)]
    (if-let [program (-> imp meta :program)]
      (-> program :globals)
      (throw (ex-info "Unresolved import" imp)))
    (mapcat (fn [[first second]]
              (clj/filter variable-declaration?
                          (take-while (complement (partial identical? first))
                                      (children second))))
            (partition 2 1 path))))

(defn locals-in-scope [path]
  ; NOTE(Richo): We take advantage of the fact that globals can only be defined
  ; in the root of the AST, which should always be the last item in the path.
  ; Unless, of course we're inside an import's initialization block, in which
  ; case we shouldn't really have any locals in scope (only the globals from
  ; the imported library).
  (if-let [imp (seek import? path)]
    (if-let [program (-> imp meta :program)]
      []
      (throw (ex-info "Unresolved import" imp)))
    (variables-in-scope (drop-last path))))

(defn variable-named
  "Returns the variable declaration referenced by this name at this point in the ast"
  [name path]
  (seek #(= name (:name %))
        (variables-in-scope path)))

(defn global?
  "Works for both variable and variable-declaration nodes."
  ([node path]
   (if (variable-declaration? node)
     (program? (first path))
     (let [var-name (:name node)]
       (and (contains? (set (map :name (globals-in-scope path))) var-name)
            (not (contains? (set (map :name (locals-in-scope path))) var-name)))))))

(defn local? [node path]
  (not (global? node path)))

(defn generate-ids [ast]
  (transform ast #(assoc % :id (str (random-uuid)))))

(defn id->token [ast]
  (->> (all-children ast)
       (map (fn [node] [(:id node) (-> node meta :token)]))
       ;; NOTE(Richo): Ids could be repeated because I was lazy when writing the blocks->ast
       ;; code and sometimes if I needed to generate a node but didn't have a corresponding
       ;; block I simply reused the parent's id. Of course this is a problem if I want to map
       ;; from blocks to tokens. The easiest is to just ignore duplicates and always use the
       ;; first block since its (probably?) the parent block.
       ;; Anyway, all this is to explain why I used (reduce ...) instead of just (into {})
       (reduce (fn [m [id token]]
                 (if-not (contains? m id)
                   (assoc m id token)
                   m))
               {})))

(defn id->range [ast]
  (->> (id->token ast)
       (map (fn [[id token]]
              (when token
                [id [(t/start token) (t/stop token)]])))
       (into {})))

(defn id->loc [ast]
  (when-let [source (-> ast meta :token :source)]
    (let [lines (map-indexed vector (u/line-indices source))]
      (->> (id->token ast)
           (map (fn [[id token]]
                  (when (and token 
                             (= source (t/source token)))
                    (when-let [[line-idx] (seek (fn [[idx line-range]]
                                                  (u/overlapping-ranges?
                                                   [(t/start token) (t/stop token)]
                                                   line-range))
                                                lines)]
                      [id line-idx]))))
           (into {})))))
