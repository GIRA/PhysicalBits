(ns plugin.compiler.ast-utils
  (:require [clojure.walk :as w]))

(defn filter [ast & types]
  (let [type-set (into #{} types)
        result (atom [])]
    (w/prewalk (fn [x]
                 (when (contains? type-set (get x :__class__))
                   (swap! result conj x))
                 x)
               ast)
    @result))
