(ns prueba.core
  (:require [petitparser.core :as pp])
  (:require [middleware.parser.ast-nodes :as ast])
  (:require [middleware.parser.parser :as p])
  (:require [middleware.compiler.compiler :as c]))

(defn foo [] 42)


(defn init []
  (println "Richo capo!!!"))

(def parser (pp/plus pp/digit))

(defn parse [str]
  (clj->js (pp/parse parser str)))

(defn ^:export compile [str]
  (clj->js (c/compile-uzi-string str)))

(comment
(js/alert "Prueba")

(p/parse "task blink13() running 1/s { toggle(D13); }")

(ast/primitive-node "+" "add")
(parse "45")

,)
