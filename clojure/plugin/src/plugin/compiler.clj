(ns plugin.compiler 
  (:require  [cheshire.core :refer [parse-string]] )
  
  (:gen-class))

(defmulti compile-node :__class__)

(defmethod compile-node "UziProgramNode" [n] n)
(defmethod compile-node :default [_] :oops)

(defn compile-ast [tree] (compile-node tree))
(defn compile-json-string [str] 
  (compile-ast (parse-string str true)))




