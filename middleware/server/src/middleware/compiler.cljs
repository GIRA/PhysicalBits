(ns middleware.compiler
  (:require [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]))

(defn init []
  (fs/register-fs! #'browser/file)
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
