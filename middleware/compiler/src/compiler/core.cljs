(ns compiler.core
  (:require [compiler.cc :as cc]))

(println "Hello world!")

;; ADDED
(defn average [a b]
  (/ (+ a b) 2.0))

(defn avg [& args]
  (/ (apply + args) 2.0))

(def compile cc/compile)
