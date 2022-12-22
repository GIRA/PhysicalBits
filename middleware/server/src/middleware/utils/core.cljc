(ns middleware.utils.core
  (:refer-clojure :exclude [format random-uuid])
  (:require [clojure.string :as str]))

(defn seek
  ([pred coll]
   (reduce #(when (pred %2) (reduced %2)) nil coll))
  ([pred coll default-value]
   (or (reduce #(when (pred %2) (reduced %2)) nil coll)
       default-value)))

(defn index-of
  "Returns -1 if not found"
  ^long [^java.util.List v e]
  (.indexOf v e))

(defn indexed-by [f vs]
  (into {} (map (juxt f identity)) vs))

(defn format
  "Simple string formatting function. It doesn't support any fancy features
  (but works in cljs)"
  [text & args]
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
  ; TODO(Richo): This shouldn't work in cljs because js/parseInt doesn't throw an exception. Check!
  (try
    (parse-int str)
    (catch #?(:clj Throwable :cljs :default) _
      (parse-double str))))

(defn line-indices [string]
  (loop [[line & rest] (str/split string #"\n")
         start 0
         indices (transient [])]
    (if line
      (let [count (count line)
            stop (+ start count)]
        (recur
          rest
          (inc stop)
          (conj! indices [start stop])))
      (persistent! indices))))

(defn overlapping-ranges? [[a1 a2] [b1 b2]]
  (or (and (>= a1 b1) (<= a1 b2))
      (and (>= a2 b1) (<= a2 b2))
      (and (>= b1 a1) (<= b1 a2))
      (and (>= b2 a1) (<= b2 a2))))

(defn random-uuid  []
  #?(:clj (java.util.UUID/randomUUID)
     :cljs (cljs.core/random-uuid)))