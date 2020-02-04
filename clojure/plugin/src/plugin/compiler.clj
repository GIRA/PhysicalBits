(ns plugin.compiler
  (:require [cheshire.core :refer [parse-string]]))

(defmulti compile-node :__class__)

(defn compile [node path]
   (println "node: " (node :__class__))
   (println "path: "  (map :__class__ path))
   (println)
   (compile-node node (conj path node)))

(defmethod compile-node "UziProgramNode" [node path]
  {:__class__ "UziProgram"
   :variables (->> (node :scripts)
                   (map #(% :tickingRate))
                   (map #(compile-node % path))
                   vec)
   :scripts (->> (node :scripts)
                 (map #(compile % path))
                 vec)})

(defmethod compile-node "UziTickingRateNode" [node path]
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

(defmethod compile-node "UziTaskNode" [node path]
  {:__class__ "UziScript",
   :arguments [],
   :delay (compile (node :tickingRate) path),
   :instructions (compile (node :body) path),
   :locals [],
   :name (node :name),
   :ticking (contains? #{"running" "once"}
                       (node :state))})

(defmethod compile-node "UziBlockNode" [node path]
  ; TODO(Richo): Add pop instruction if last stmt is expression
  (vec (mapcat #(compile % path) (node :statements))))

(defmethod compile-node "UziAssignmentNode" [node path]
  (let [right (compile (node :right) path)
        var-name (-> node :left :name)]
    (conj right
          {:__class__ "UziPopInstruction"
           :argument {:__class__ "UziVariable"
                      :name var-name}})))

(defmethod compile-node "UziCallNode" [node path]
  ; TODO(Richo): Detect primitive calls correctly!
  (conj (vec (mapcat #(compile (:value %) path)
                     (node :arguments)))
        {:__class__ "UziPrimitiveCallInstruction"
         :argument {:__class__ "UziPrimitive"
                    :name "add"}}))

(defmethod compile-node "UziNumberLiteralNode" [node path]
  [{:__class__ "UziPushInstruction"
    :argument {:__class__ "UziVariable"
               :value (node :value)}}])

(defmethod compile-node "UziVariableNode" [node path]
  ; TODO(Richo): Detect if var is global or local
  [{:__class__ "UziPushInstruction"
    :argument {:__class__ "UziVariable"
               :name (node :name)}}])

(defmethod compile-node :default [node _]
  (println "ERROR! Unknown node: " (:__class__ node))
  :oops)

(defn compile-tree [ast]
  (compile ast (list)))

(defn compile-json-string [str]
  (compile-tree (parse-string str true)))
