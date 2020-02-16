(ns plugin.compiler.ast-utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj-core]
            [clojure.walk :as w]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn node-type [node]
  (get node :__class__))

(defn compile-time-constant? [node]
  (= "UziNumberLiteralNode" (node-type node)))

(defn filter [ast & types]
  (let [type-set (into #{} types)
        result (atom [])]
    (w/prewalk (fn [x]
                 (when (contains? type-set (node-type x))
                   (swap! result conj x))
                 x)
               ast)
    @result))

(defn transform-pred [ast & clauses]
  (w/prewalk (fn [node]
               (if (node? node)
                 (loop [[pred result-fn & rest] clauses]
                   (if (or (= :default pred)
                           (pred node))
                     (result-fn node)
                     (if (empty? rest)
                       node
                       (recur rest))))
                 node))
             ast))

(defn transform [ast & clauses]
  (let [as-pred (fn [type]
                  (if (= :default type)
                    type
                    #(= type (node-type %))))]
    (apply transform-pred
      ast (mapcat (fn [[type result-fn]]
                    [(as-pred type) result-fn])
                  (partition 2 clauses)))))

(defn children [ast]
  (->> ast
       vals
       (mapcat #(cond
                  (node? %) [%]
                  (vector? %) (clj-core/filter node? %)
                  :else nil))
       (clj-core/filter (complement nil?))))
