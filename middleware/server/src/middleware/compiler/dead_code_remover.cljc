(ns middleware.compiler.dead-code-remover
  (:require [middleware.compiler.utils.ast :as ast-utils]
            [taoensso.tufte :as tufte :refer (defnp p profiled profile)]))

(defmulti ^:private visit-node :__class__)

(defnp ^:private visit [node ctx]
  (visit-node node (update-in ctx [:path] conj node)))

(defnp visit-children [node ctx]
  (doseq [child (ast-utils/children node)]
    (visit child ctx)))

(defmethod visit-node "UziProgramNode" [{:keys [scripts]} ctx]
  (p ::visit-program
     (doseq [root (filter #(contains? #{"running" "once"} (:state %))
                          scripts)]
       (visit root ctx))))

(defnp visit-script [{:keys [name] :as script} ctx]
  (when (not (contains? @(:visited-scripts ctx) name))
    (swap! (:visited-scripts ctx) conj name)
    (visit-children script ctx)))

(defmethod visit-node "UziTaskNode" [node ctx] (visit-script node ctx))
(defmethod visit-node "UziFunctionNode" [node ctx] (visit-script node ctx))
(defmethod visit-node "UziProcedureNode" [node ctx] (visit-script node ctx))

(defnp ^:private get-script-named [name ctx]
  (first (filter (fn [script] (= name (:name script)))
                 (-> ctx :path last :scripts))))

(defnp visit-script-control [{:keys [scripts]} ctx]
  (doseq [script (map #(get-script-named % ctx)
                      scripts)]
    (visit script ctx)))

(defmethod visit-node "UziScriptStartNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptStopNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptResumeNode" [node ctx] (visit-script-control node ctx))
(defmethod visit-node "UziScriptPauseNode" [node ctx] (visit-script-control node ctx))

(defmethod visit-node "UziVariableNode" [node ctx]
  (p ::visit-variable
     (when (ast-utils/global? node (:path ctx))
       (swap! (:visited-globals ctx) conj (:name node)))
     (visit-children node ctx)))

(defmethod visit-node "UziCallNode" [node ctx]
  (p ::visit-call
     (if (get node :primitive-name)
       (swap! (:visited-primitives ctx) conj (:selector node))
       (visit (get-script-named (:selector node) ctx) ctx))
     (visit-children node ctx)))

(defmethod visit-node :default [node ctx]
  (p ::visit-default
     (visit-children node ctx)))


(defnp create-context []
  {:path (list),
   :visited-scripts (atom #{}),
   :visited-globals (atom #{}),
   :visited-primitives (atom #{})})

(defnp remove-dead-code [ast]
  (let [ctx (create-context)]
    (visit ast ctx)
    (assoc ast
           :globals (filterv #(contains? @(:visited-globals ctx) (:name %))
                             (:globals ast))
           :scripts (filterv #(contains? @(:visited-scripts ctx) (:name %))
                             (:scripts ast))
           :primitives (filterv #(contains? @(:visited-primitives ctx) (:alias %))
                                (:primitives ast)))))
