(ns middleware.test-utils
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [middleware.compiler.compiler :as cc]))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))


(def ^:dynamic *test-name* nil)

(defmacro uzitest
  "Tests declared with uzitest have access to the *test-name* dynamic var"
  [name & body]
  `(deftest ~name
     (binding [*test-name* ~(str name)]
       (do ~@body))))

(defn compile-ast [ast]
  ;(println *test-name*)
  (:compiled (cc/compile-tree ast "")))

(defn compile-string [src]
  ;(println *test-name*)
  (:compiled (cc/compile-uzi-string src)))
