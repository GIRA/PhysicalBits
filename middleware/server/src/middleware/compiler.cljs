(ns middleware.compiler
  (:require [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]))

(defn init []
  (println "Compiler loaded successfully!"))

(defn ^:export parse [str]
  (clj->js (p/parse str)))

(defn ^:export compile [str type]
  (println type)
  (if (= type "uzi")
    (clj->js (c/compile-uzi-string str))
    (clj->js (c/compile-json-string str))))

(defn ^:export encode [program]
  (clj->js (en/encode (js->clj program :keywordize-keys true))))
