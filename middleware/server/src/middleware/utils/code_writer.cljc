(ns middleware.utils.code-writer
  (:require [clojure.string :as str]))

(defn make-writer []
  {:contents (volatile! []) ; TODO(Richo): Maybe use a native string builder or somethin?
   :position (volatile! 0)
   :indent-level (volatile! 0)
   :intervals (volatile! {})})

(defn contents [writer]
  (str/join "" @(:contents writer)))

(defn position [writer] @(:position writer))

(defn intervals [writer] @(:intervals writer))

(defn append! [writer string]
  (vswap! (:contents writer) conj string)
  (vswap! (:position writer) (partial + (count string)))
  writer)

(defn append-line!
  ([writer] (append! writer "\n"))
  ([writer string]
   (append! writer string)
   (append! writer "\n")))

(defn append-indent! [writer]
  (let [level @(:indent-level writer)]
    (dotimes [i level]
             (vswap! (:contents writer) conj "\t"))
    (vswap! (:position writer) (partial + level))
    writer))

(defn inc-indent! [writer f]
  (let [old-level @(:indent-level writer)]
    (try
      (vswap! (:indent-level writer) inc)
      (f writer)
      (finally (vreset! (:indent-level writer) old-level)))
    writer))

(defn save-interval! [writer obj f]
  (let [start (position writer)
         result (f obj)
         stop (position writer)]
    (vswap! (:intervals writer) assoc obj [start stop])
    result))
