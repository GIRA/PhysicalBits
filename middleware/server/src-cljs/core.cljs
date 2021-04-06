(ns core
  (:require [petitparser.core :as pp]))

(defn foo [] 42)

(def parser (pp/plus pp/digit))

(defn parse [str]
  (clj->js (pp/parse parser str)))
