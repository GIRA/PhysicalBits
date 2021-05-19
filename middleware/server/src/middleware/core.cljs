(ns middleware.core
  (:require [middleware.parser.parser :as p])
  (:require [middleware.compiler.compiler :as c]))

(defn init []
  (println "Richo capo!"))

(defn ^:export parse [str]
  (clj->js (p/parse str)))

(defn ^:export compile [str type]
  (println type)
  (if (= type "uzi")
    (clj->js (c/compile-uzi-string str))
    (clj->js (c/compile-json-string str))))
