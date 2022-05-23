(ns middleware.utils.config
  (:refer-clojure :exclude [get get-in]))

; TODO(Richo): Make this work for real!

(defn get-all
  ([] {})
  ([_path] {}))

(defn get [_key default-value]
  default-value)

(defn get-in [_keys default-value]
  default-value)
