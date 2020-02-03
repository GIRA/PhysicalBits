(ns plugin.compiler
  (:require [cheshire.core :refer [parse-string]]))

(defmulti compile :__class__)

(defmethod compile "UziProgramNode" [node]
  {:__class__ "UziProgram"
   :variables (->> (node :scripts)
                   (map #(-> % :tickingRate))
                   (map compile)
                   vec)
   :scripts (->> (node :scripts)
                 (map compile)
                 vec)})

(defmethod compile "UziTickingRateNode" [node]
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

(defmethod compile "UziTaskNode" [node]
  {:__class__ "UziScript",
   :arguments [],
   :delay (compile (node :tickingRate)),
   :instructions [],
   :locals [],
   :name (node :name),
   :ticking (contains? #{"running" "once"}
                       (node :state))})

(defmethod compile :default [_] :oops)

(defn compile-json-string [str]
  (compile (parse-string str true)))
