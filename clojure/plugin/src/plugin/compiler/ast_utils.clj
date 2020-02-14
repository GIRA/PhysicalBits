(ns plugin.compiler.ast-utils
  (:refer-clojure :exclude [filter])
  (:require [clojure.core :as clj-core]
            [clojure.walk :as w]))

(defn node? [node]
  (and (map? node)
       (contains? node :__class__)))

(defn filter [ast & types]
  (let [type-set (into #{} types)
        result (atom [])]
    (w/prewalk (fn [x]
                 (when (contains? type-set (get x :__class__))
                   (swap! result conj x))
                 x)
               ast)
    @result))

(defmacro transform-pred [ast & clauses]
  (let [node (gensym 'node)]
    `(w/prewalk
      (fn [~node]
        (if (node? ~node)
          (cond
            ~@(mapcat (fn [[pred result-expr]]
                        (if (= :default pred)
                          `(:else (~result-expr ~node))
                          `((~pred ~node) (~result-expr ~node))))
                      (partition 2 clauses))
            :else ~node)
          ~node))
      ~ast)))

(defmacro transform [ast & clauses]
  `(transform-pred ~ast
                   ~@(mapcat (fn [[type result-fn]]
                               (if (= :default type)
                                 `(~type ~result-fn)
                                 `((fn [node#] (= ~type (get node# :__class__))) ~result-fn)))
                             (partition 2 clauses))))

(defn children [ast]
  (->> ast
       vals
       (mapcat #(cond
                  (node? %) [%]
                  (vector? %) (clj-core/filter node? %)
                  :else nil))
       (clj-core/filter (complement nil?))))
