(ns plugin.utils.async
  (:require [clojure.core.async :as a]))

(def default-timeout 1000)

(defmacro <?
  ([chan] `(<? ~chan ~default-timeout))
  ([chan timeout] `(first (a/alts! [~chan (a/timeout ~timeout)]))))

(defmacro <??
  ([chan] `(<?? ~chan ~default-timeout))
  ([chan timeout] `(first (a/alts!! [~chan (a/timeout ~timeout)]))))
