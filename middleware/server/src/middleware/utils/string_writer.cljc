(ns middleware.utils.string-writer
  (:require [clojure.string :as str]))

(def ^:dynamic *writer*)

(defn make-writer []
  {:contents (volatile! []) ; TODO(Richo): Maybe use a native string builder or somethin?
   :position (volatile! 0)
   :indent-level (volatile! 0)
   :intervals (volatile! {})})

(defn contents
  ([] (contents *writer*))
  ([writer] (str/join "" @(:contents writer))))

(defn position
  ([] (position *writer*))
  ([writer] @(:position writer)))

(defn intervals
  ([] (intervals *writer*))
  ([writer] @(:intervals writer)))

(defn append!
  ([string] (append! *writer* string))
  ([writer string]
   (vswap! (:contents writer) conj string)
   (vswap! (:position writer) (partial + (count string)))
   writer))

(defn append-line!
  ([] (append! *writer* "\n"))
  ([string] (append-line! *writer* string))
  ([writer string]
   (append! writer string)
   (append! writer "\n")))

(defn append-indent!
  ([] (append-indent! *writer*))
  ([writer]
   (let [level @(:indent-level writer)]
     (dotimes [i level]
              (vswap! (:contents writer) conj "\t"))
     (vswap! (:position writer) (partial + level))
     writer)))

(defn inc-indent!
  ([f] (inc-indent! *writer* f))
  ([writer f]
   (let [old-level @(:indent-level writer)]
     (try
       (vswap! (:indent-level writer) inc)
       (f)
       (finally (vreset! (:indent-level writer) old-level)))
     writer)))

(defn save-interval!
  ([obj f] (save-interval! *writer* obj f))
  ([writer obj f]
   (let [start (position writer)
         result (f obj)
         stop (position writer)]
     (vswap! (:intervals writer) assoc obj [start stop])
     result)))
