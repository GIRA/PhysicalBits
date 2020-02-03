(ns plugin.compiler
  (:require [cheshire.core :refer [parse-string]]))

(defmulti compile-node :__class__)

(defmethod compile-node "UziProgramNode" [node]
  {:__class__ "UziProgram"
   :variables (->> (node :scripts)
                   (map #(-> % :tickingRate))
                   (map compile-node)
                   vec)
   :scripts (->> (node :scripts)
                 (map compile-node)
                 vec)})

(defmethod compile-node "UziTickingRateNode" [node]
  (let [delay-ms (if (= (node :value) 0)
                   (double Double/MAX_VALUE)
                   (/ (case (node :scale)
                        "s" 1000
                        "m" (* 1000 60)
                        "h" (* 1000 60 60)
                        "d" (* 1000 60 60 24))
                      (node :value)))]
    {:__class__ "UziVariable"
     :value delay-ms}))

(defmethod compile-node "UziTaskNode" [node]
  {:__class__ "UziScript",
   :arguments [],
   :delay (compile-node (node :tickingRate)),
   :instructions [],
   :locals [],
   :name (node :name),
   :ticking (contains? #{"running" "once"}
                       (node :state))})

(defmethod compile-node :default [_] :oops)

(defn compile-ast [tree] (compile-node tree))
(defn compile-json-string [str]
  (compile-ast (parse-string str true)))
