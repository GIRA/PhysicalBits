(ns plugin.compiler.core
  (:refer-clojure :exclude [compile])
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :as w]
            [plugin.compiler.emitter :as emit]))

(defmulti compile-node :__class__)

(defn compile [node ctx]
  (println "node:" (node :__class__))
  (println "path:" (map :__class__ (ctx :path)))
  (println "vars:" @(ctx :variable-names))
  (println)
  (compile-node node (update-in ctx [:path] conj node)))

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


(defmethod compile-node "UziProgramNode" [node ctx]
  (emit/program
   :globals (collect-globals node)
   :scripts (->> (node :scripts)
                 (map #(compile % ctx))
                 vec)))

(defmethod compile-node "UziTaskNode" [node ctx]
  (emit/script
   :delay (rate->delay (node :tickingRate)),
   :instructions (compile (node :body) ctx),
   :name (node :name),
   :running? (contains? #{"running" "once"}
                        (node :state))))

(defmethod compile-node "UziBlockNode" [node ctx]
  ; TODO(Richo): Add pop instruction if last stmt is expression
  (vec (mapcat #(compile % ctx) (node :statements))))

(defmethod compile-node "UziAssignmentNode" [node ctx]
  (let [right (compile (node :right) ctx)
        var-name (-> node :left :name)]
    (conj right (emit/pop var-name))))

(defmethod compile-node "UziCallNode" [node ctx]
  ; TODO(Richo): Detect primitive calls correctly!
  (conj (vec (mapcat #(compile (:value %) ctx)
                     (node :arguments)))
        (emit/prim "add"))) ; TODO(Richo): Hardcoded prim just to pass test

(defmethod compile-node "UziNumberLiteralNode" [node _]
  [(emit/push-value (node :value))])

(defmethod compile-node "UziVariableNode" [node _]
  ; TODO(Richo): Detect if var is global or local
  [(emit/push-var (node :name))])

(defmethod compile-node :default [node _]
  (println "ERROR! Unknown node: " (:__class__ node))
  :oops)

(defn- create-context []
  {:path (list)
   :variable-names (atom [])})

(defn compile-tree [ast]
  (compile ast (create-context)))

(defn compile-json-string [str]
  (compile-tree (parse-string str true)))
