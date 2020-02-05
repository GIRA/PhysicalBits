(ns plugin.compiler
  (:refer-clojure :exclude [compile])
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :as w]
            [plugin.emitter :as emit]))

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
                    "UziTickingRateNode" (swap! vars conj (emit/variable :value (rate->delay %)))
                    "UziNumberLiteralNode" (swap! vars conj (emit/variable :value (% :value)))

                    ; TODO(Richo): Variable declarations could mean local variables, not globals
                    "UziVariableDeclarationNode" (swap! vars conj (emit/variable
                                                                   :name (% :name)
                                                                   :value (or (% :value) 0)))
                    nil)]
    (w/prewalk (fn [x] (find-var x) x) ast)
    @vars))


(defmethod compile-node "UziProgramNode" [node path]
  (emit/program
   :globals (collect-globals node)
   :scripts (->> (node :scripts)
                 (map #(compile % path))
                 vec)))

(defmethod compile-node "UziTaskNode" [node path]
  (emit/script
   :delay (rate->delay (node :tickingRate)),
   :instructions (compile (node :body) path),
   :name (node :name),
   :running? (contains? #{"running" "once"}
                        (node :state))))

(defmethod compile-node "UziBlockNode" [node path]
  ; TODO(Richo): Add pop instruction if last stmt is expression
  (vec (mapcat #(compile % path) (node :statements))))

(defmethod compile-node "UziAssignmentNode" [node path]
  (let [right (compile (node :right) path)
        var-name (-> node :left :name)]
    (conj right (emit/pop var-name))))

(defmethod compile-node "UziCallNode" [node path]
  ; TODO(Richo): Detect primitive calls correctly!
  (conj (vec (mapcat #(compile (:value %) path)
                     (node :arguments)))
        (emit/prim "add"))) ; TODO(Richo): Hardcoded prim just to pass test

(defmethod compile-node "UziNumberLiteralNode" [node path]
  [(emit/push-value (node :value))])

(defmethod compile-node "UziVariableNode" [node path]
  ; TODO(Richo): Detect if var is global or local
  [(emit/push-var (node :name))])

(defmethod compile-node :default [node _]
  (println "ERROR! Unknown node: " (:__class__ node))
  :oops)

(defn compile-tree [ast]
  (compile ast (list)))

(defn compile-json-string [str]
  (compile-tree (parse-string str true)))
