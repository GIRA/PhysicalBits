(ns middleware.test-utils
  (:require [clojure.data :as data]))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))
