(ns middleware.utils.core
  (:require [clojure.string :as str]))

(defn seek [pred coll]
  (reduce #(when (pred %2) (reduced %2)) nil coll))

(defn index-of ^long [^java.util.List v e]
  "Returns -1 if not found"
  (.indexOf v e))

(defn uzi-format [text & args]
  (loop [t text, i 0]
    (if-let [val (nth args i nil)]
      (recur
        (str/replace t (str "%" (inc i)) (str val))
        (inc i))
      t)))
