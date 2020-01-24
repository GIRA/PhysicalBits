(ns plugin.compiler 
  (:require  [cheshire.core :refer [parse-string]] )
  
  (:gen-class))
 
(defn compile-ast [tree] tree)
(defn compile-json-string [str] 
  (compile-ast (cheshire.core/parse-string str true)))

