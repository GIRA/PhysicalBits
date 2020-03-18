(ns plugin.compiler.dead-code-remover
  (:require [plugin.compiler.ast-utils :as ast-utils]))

(defmulti ^:private visit-node :__class__)

(defn- visit [node ctx]
  (visit-node node (update-in ctx [:path] conj node)))

(defn visit-children [node ctx]
  (doseq [child (ast-utils/children node)]
    (visit child ctx)))

(defmethod visit-node "UziProgramNode" [{:keys [scripts]} ctx]
  (doseq [root (filter #(contains? #{"running" "once"} (:state %))
                       scripts)]
    (visit root ctx)))

(defn visit-script [{:keys [name] :as script} ctx]
  (when (not (contains? @(:visited-scripts ctx) name))
    (swap! (:visited-scripts ctx) conj name)
    (visit-children script ctx)))

(defmethod visit-node "UziTaskNode" [node ctx] (visit-script node ctx))
(defmethod visit-node "UziFunctionNode" [node ctx] (visit-script node ctx))
(defmethod visit-node "UziProcedureNode" [node ctx] (visit-script node ctx))

(defn- get-script-named [name ctx]
  (first (filter (fn [script] (= name (:name script)))
                 (-> ctx :path last :scripts))))

(defn visit-script-control [{:keys [scripts]} ctx]
  (doseq [script (map #(get-script-named % ctx)
                      scripts)]
    (visit script ctx)))

(defmethod visit-node "UziScriptStartNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptStopNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptResumeNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptPauseNode" [node ctx] (visit-script-control node ctx))

(defmethod visit-node :default [node ctx] (visit-children node ctx))


#_(
   (require '[plugin.parser.core :as parser])
   (def src "task stopped_script() stopped {}")
   (def ast (parser/parse src))
   (remove-dead-code ast)
   )

(defn create-context []
  {:path (list), :visited-scripts (atom #{})})

(defn remove-dead-code [{:keys [scripts] :as ast}]
  (let [ctx (create-context)]
    (visit ast ctx)
    (assoc ast
           :scripts (filterv #(contains? @(:visited-scripts ctx) (:name %))
                             scripts))))
