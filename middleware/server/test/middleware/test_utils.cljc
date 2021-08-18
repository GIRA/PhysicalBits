(ns middleware.test-utils
  (:require [clojure.data :as data]
            [clojure.core.async :as a]
            #?(:cljs [cljs.test :refer-macros [async]])))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))

; NOTE(Richo): https://stackoverflow.com/a/30781278
(defn test-async
  "Asynchronous test awaiting ch to produce a value or close."
  [ch]
  #?(:clj
     (a/<!! ch)
     :cljs
     (async done
            (a/take! ch (fn [_] (done))))))
