(ns plugin.compiler
  (:refer-clojure :exclude [compile])
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :as w]))

(defmulti compile-node :__class__)

(defn compile [node path]
   (println "node: " (node :__class__))
   (println "path: "  (map :__class__ path))
   (println)
   (compile-node node (conj path node)))

(defn- rate->delay [node]
  (if (= (node :value) 0)
    (double Double/MAX_VALUE)
    (/ (case (node :scale)
         "s" 1000
         "m" (* 1000 60)
         "h" (* 1000 60 60)
         "d" (* 1000 60 60 24))
       (node :value))))

(defn collect-globals [ast]
  (let [vars (atom #{})
        find-var #(condp = (get % :__class__)
                    "UziTickingRateNode" (swap! vars conj {:__class__ "UziVariable"
                                                           :value (rate->delay %)})
                    "UziNumberLiteralNode" (swap! vars conj {:__class__ "UziVariable"
                                                             :value (% :value)})

                    ; TODO(Richo): Variable declarations could mean local variables, not globals
                    "UziVariableDeclarationNode" (swap! vars conj {:__class__ "UziVariable"
                                                                   :name (% :name)
                                                                   :value (or (% :value) 0)})
                    nil)]
    (w/prewalk (fn [x] (find-var x) x) ast)
    @vars))


(defmethod compile-node "UziProgramNode" [node path]
  {:__class__ "UziProgram"
   :variables (collect-globals node)
   :scripts (->> (node :scripts)
                 (map #(compile % path))
                 vec)})

(defmethod compile-node "UziTaskNode" [node path]
  {:__class__ "UziScript",
   :arguments [],
   :delay {:__class__ "UziVariable" :value (rate->delay (node :tickingRate))},
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
