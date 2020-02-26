(ns plugin.compiler.ast-utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj-core]
            [clojure.walk :as w]
            [plugin.compiler.primitives :as prims]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn node-type [node]
  (get node :__class__))

(defn compile-time-constant? [node]
  (= "UziNumberLiteralNode" (node-type node)))

(defn compile-time-value [node not-constant]
  (if (compile-time-constant? node)
    (:value node)
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

(defn has-side-effects? [{:keys [primitive-name arguments] :as node}]
  (if (= "UziCallNode" (node-type node))
    (if-not primitive-name
      true ; Script calls could always have side-effects
      (if (some has-side-effects? (map :value arguments))
        true
        (if-let [{[_ after] :stack-transition} (prims/primitive primitive-name)]
          (not (= 1 after))
          true)))
    (not (expression? node))))


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
(defmethod children-keys "UziProgramNode" [_] [:imports :globals :scripts])
(defmethod children-keys "UziRepeatNode" [_] [:times :body])
(defmethod children-keys "UziReturnNode" [_] [:value])
(defmethod children-keys "UziTaskNode" [_] [:arguments :tickingRate :body])
(defmethod children-keys "UziProcedureNode" [_] [:arguments :body])
(defmethod children-keys "UziFunctionNode" [_] [:arguments :body])
(defmethod children-keys "UziVariableDeclarationNode" [_] [:value])
(defmethod children-keys "Association" [_] [:value])
(defmethod children-keys :default [_] [])

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

; TODO(Richo): Refactor this function. This is a mess...
(defn transform-pred [ast & clauses]
  (cond

    (node? ast)
    (let [node (loop [[pred result-fn & rest] clauses]
                 (if (or (= :default pred)
                         (pred ast))
                   (result-fn ast)
                   (if (empty? rest)
                     ast
                     (recur rest))))
          valid-keys (clj-core/filter #(not (nil? (node %)))
                                       (children-keys node))]
      (loop [keys valid-keys, result node]
        (let [[first & rest] keys]
          (if first
            (let [new-result (assoc result
                                    first
                                    (apply transform-pred (result first) clauses))]
              (if (empty? rest)
                new-result
                (recur rest new-result)))
            result))))

    (vector? ast)
    (mapv #(apply transform-pred % clauses)
          ast)

    :else ast))


(defn transform [ast & clauses]
  (let [as-pred (fn [type]
                  (if (= :default type)
                    type
                    #(= type (node-type %))))]
    (apply transform-pred
      ast (mapcat (fn [[type result-fn]]
                    [(as-pred type) result-fn])
                  (partition 2 clauses)))))
