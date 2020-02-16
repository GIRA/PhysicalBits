(ns plugin.compiler.core
  (:refer-clojure :exclude [compile])
  (:require [cheshire.core :refer [parse-string]]
            [clojure.walk :as w]
            [plugin.device.boards :as boards]
            [plugin.compiler.ast-utils :as ast-utils]
            [plugin.compiler.emitter :as emit]
            [plugin.compiler.primitives :as prims]
            [plugin.compiler.linker :as linker]))

(defmulti compile-node :__class__)

(defn compile [node ctx]
  (compile-node node (update-in ctx [:path] conj node)))


(defn- rate->delay [{:keys [value scale] :as node}]
  (if-not node 0
    (if (= value 0)
      (double Double/MAX_VALUE)
      (/ (case scale
           "s" 1000
           "m" (* 1000 60)
           "h" (* 1000 60 60)
           "d" (* 1000 60 60 24))
         value))))

(defn variables-in-scope [path]
  (mapcat (fn [[first second]]
            (filter #(= "UziVariableDeclarationNode" (get % :__class__))
                    (take-while #(not (= % first))
                                (ast-utils/children second))))
          (partition 2 1 path)))

(defn collect-globals [ast board]
  (let [vars (atom #{})]
    ; Collect all number literals
    (swap! vars into
           (map (fn [{:keys [value]}] (emit/variable :value value))
                (ast-utils/filter ast "UziNumberLiteralNode")))

    ; Collect all pin literals
    (swap! vars into
           (map (fn [{:keys [type number]}] (emit/variable :value (boards/get-pin-number (str type number) board)))
                (ast-utils/filter ast "UziPinLiteralNode")))

    ; Collect all globals
    (swap! vars into
           (map (fn [{:keys [name value]}] (emit/variable :name name :value value))
                (:globals ast)))

    ; Collect all ticking rates
    (swap! vars into
           (map (fn [{:keys [tickingRate]}] (emit/variable :value (rate->delay tickingRate)))
                (:scripts ast)))

    @vars))

(defmethod compile-node "UziProgramNode" [node ctx]
  (emit/program
   :globals (collect-globals node (ctx :board))
   :scripts (->> (node :scripts)
                 (map #(compile % ctx))
                 vec)))

(defn collect-locals [script-body]
  (mapv (fn [{:keys [unique-name value]}]
          (emit/variable :name unique-name
                         :value (if (= "UziNumberLiteralNode" (:__class__ value))
                                  (-> value :value)
                                  0)))
       (ast-utils/filter script-body "UziVariableDeclarationNode")))

(defmethod compile-node "UziTaskNode"
  [{task-name :name, ticking-rate :tickingRate, state :state, body :body}
   ctx]
  (emit/script
   :name task-name,
   :delay (rate->delay ticking-rate),
   :running? (contains? #{"running" "once"} state)
   :locals (collect-locals body)
   :instructions (let [instructions (compile body ctx)]
                   (if (= "once" state)
                     (conj instructions (emit/stop task-name))
                     instructions))))

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
        (emit/prim (node :primitive-name))))

(defmethod compile-node "UziNumberLiteralNode" [node _]
  [(emit/push-value (node :value))])

(defmethod compile-node "UziVariableNode" [node ctx]
  (let [variables-in-scope (variables-in-scope (ctx :path))
        globals (-> ctx :path last :globals set)
        variable (first (filter #(= (:name node) (:name %))
                                variables-in-scope))
        global? (contains? globals variable)]
    [(if global?
       (emit/push-var (node :name))
       (emit/read-local (variable :unique-name)))]))

(defmethod compile-node "UziVariableDeclarationNode"
  [{:keys [unique-name value]} ctx]
  (if (= "UziNumberLiteralNode" (:__class__ value))
    []
    (conj (compile value ctx)
          (emit/write-local unique-name))))

(defmethod compile-node "UziPinLiteralNode" [{:keys [type number]} ctx]
  [(emit/push-value (boards/get-pin-number (str type number) (ctx :board)))])

(defmethod compile-node :default [node _]
  (println "ERROR! Unknown node: " (:__class__ node))
  :oops)

(defn- create-context [board]
  {:path (list)
   :board board})

(defn- assign-unique-variable-names [ast]
  (let [counter (atom 0)
        globals (-> ast :globals set)]
    (ast-utils/transform
     ast
     "UziVariableDeclarationNode"
     (fn [var]
       (if (contains? globals var)
         var
         (assoc var :unique-name (str (:name var) "#" (swap! counter inc))))))))

(defn- assign-internal-ids
  "This function is important because it will guarantee that all nodes are different
   when compared with =. Due to clojure's philosophy regarding values, identity, and
   equality I need to do this to be able to distinguish two otherwise equal nodes.
   This is particularly crucial for the variables-in-scope function because it relies
   on = to know when to stop looking for variables.
   An alternative could be to use identical? instead of = but I feel it would make
   the code more fragile than simply adding this artificial :internal-id"
  [ast]
  (ast-utils/transform ast :default #(assoc % :internal-id (.toString (java.util.UUID/randomUUID)))))

(defn compile-tree
  ([ast] (compile-tree ast boards/UNO))
  ([ast board]
   (-> ast
       linker/bind-primitives
       assign-unique-variable-names
       assign-internal-ids
       (compile (create-context board)))))

(defn compile-json-string [str]
  (compile-tree (parse-string str true)))
