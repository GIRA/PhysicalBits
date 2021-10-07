(ns middleware.utils.core
  (:refer-clojure :exclude [format])
  (:require [clojure.string :as str]))

(defn seek [pred coll]
  (reduce #(when (pred %2) (reduced %2)) nil coll))

(defn index-of ^long [^java.util.List v e]
  "Returns -1 if not found"
  (.indexOf v e))

(defn format [text & args]
  "Simple string formatting function. It doesn't support any fancy features
  (but works in cljs)"
  (loop [t text, i 0]
    (if-let [val (nth args i nil)]
      (recur
        (str/replace t (str "%" (inc i)) (str val))
        (inc i))
      t)))

(defn millis ^long []
  #?(:clj (System/currentTimeMillis)
    :cljs (.getTime (js/Date.))))

(defn clamp [^long val ^long lower ^long upper]
  (max lower (min upper val)))

(defn parse-int [str] #?(:clj (Integer/parseInt str)
                          :cljs (js/parseInt str)))

(defn parse-double [str] #?(:clj (Double/parseDouble str)
                           :cljs (js/parseFloat str)))

(defn parse-number [str]
  (try
    (parse-int str)
    (catch #?(:clj Throwable :cljs :default) _
      (parse-double str))))
