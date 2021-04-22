(ns prueba.core
  #_(:require [petitparser.core :as pp]))

(defn foo [] 42)

(defn init []
  (println "Richo capo!!!"))


(comment
(js/alert "Prueba")
(def parser (pp/plus pp/digit))

(defn parse [str]
  (clj->js (pp/parse parser str)))
,)
