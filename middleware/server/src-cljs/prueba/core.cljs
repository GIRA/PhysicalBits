(ns prueba.core
  (:require [petitparser.core :as pp]))

(defn foo [] 42)

(defn init []
  (println "Richo capo!!!"))

(def parser (pp/plus pp/digit))

(defn parse [str]
  (clj->js (pp/parse parser str)))

(comment
(js/alert "Prueba")

(parse "45")

,)
