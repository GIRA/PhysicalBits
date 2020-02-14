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

(defn children [ast]
  (->> ast
       vals
       (mapcat #(cond
                  (node? %) [%]
                  (vector? %) (clj-core/filter node? %)
                  :else nil))
       (clj-core/filter (complement nil?))))
