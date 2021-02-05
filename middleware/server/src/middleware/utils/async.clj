(ns middleware.utils.async
  (:require [clojure.core.async :as a]))

(def default-timeout 1000)

(defmacro <?
  ([chan] `(<? ~chan ~default-timeout))
  ([chan timeout] `(first (a/alts! [~chan (a/timeout ~timeout)]))))

(defmacro <??
  ([chan] `(<?? ~chan ~default-timeout))
  ([chan timeout] `(first (a/alts!! [~chan (a/timeout ~timeout)]))))

(defn read-vec? [count in]
  (a/go-loop [i count
            v []]
    (if (<= i 0)
      v
      (recur (dec i) (conj v (<? in))))))

(defn read-vec?? [count in]
  (loop [i count
         v []]
    (if (<= i 0)
      v
      (recur (dec i) (conj v (<?? in))))))
