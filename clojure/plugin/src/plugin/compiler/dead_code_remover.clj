(ns plugin.compiler.dead-code-remover
  (:require [plugin.compiler.ast-utils :as ast-utils]))

(defmulti ^:private visit :__class__)

(defmethod visit "UziProgramNode" [{:keys [scripts]} visited]
  (doseq [root (filter #(contains? #{"running" "once"} (:state %))
                       scripts)]
    (visit root visited)))

(defn visit-script [{:keys [name] :as script} visited]
  (when (not (contains? @(:scripts visited) name))
    (swap! (:scripts visited) conj name)
    (doseq [child (ast-utils/children script)]
      (visit child visited))))

(defmethod visit "UziTaskNode" [node visited] (visit-script node visited))
(defmethod visit "UziFunctionNode" [node visited] (visit-script node visited))
(defmethod visit "UziProcedureNode" [node visited] (visit-script node visited))

(defmethod visit :default [node visited]
  (doseq [child (ast-utils/children node)]
    (visit child visited)))


#_(
   (require '[plugin.parser.core :as parser])
   (def src "task stopped_script() stopped {}")
   (def ast (parser/parse src))
   (remove-dead-code ast)
   )


(defn remove-dead-code [{:keys [scripts] :as ast}]
  (let [visited {:scripts (atom #{})}]
    (visit ast visited)
    (assoc ast
           :scripts (filterv #(contains? @(:scripts visited) (:name %))
                             scripts))))
