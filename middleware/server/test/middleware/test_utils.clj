(ns middleware.test-utils
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [middleware.compiler.compiler :as cc]))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))

(defn compile-ast [ast]
  (:compiled (cc/compile-tree ast "")))

(defn compile-string [src]
  (:compiled (cc/compile-uzi-string src)))
