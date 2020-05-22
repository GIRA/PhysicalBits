(ns middleware.compiler.dead-code-remover
  (:require [middleware.compiler.ast-utils :as ast-utils]))

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

(defmethod visit-node "UziVariableNode" [node ctx]
  (when (ast-utils/global? node (:path ctx))
    (swap! (:visited-globals ctx) conj (:name node)))
  (visit-children node ctx))

(defmethod visit-node "UziCallNode" [node ctx]
  (if (get node :primitive-name)
    (swap! (:visited-primitives ctx) conj (:selector node))
    (visit (get-script-named (:selector node) ctx) ctx))
  (visit-children node ctx))

(defmethod visit-node :default [node ctx] (visit-children node ctx))


(defn create-context []
  {:path (list),
   :visited-scripts (atom #{}),
   :visited-globals (atom #{}),
   :visited-primitives (atom #{})})

(defn remove-dead-code [ast]
  (let [ctx (create-context)]
    (visit ast ctx)
    (assoc ast
           :imports (mapv #(dissoc % :program)
                          (:imports ast))
           :globals (filterv #(contains? @(:visited-globals ctx) (:name %))
                             (:globals ast))
           :scripts (filterv #(contains? @(:visited-scripts ctx) (:name %))
                             (:scripts ast))
           :primitives (filterv #(contains? @(:visited-primitives ctx) (:alias %))
                                (:primitives ast)))))
