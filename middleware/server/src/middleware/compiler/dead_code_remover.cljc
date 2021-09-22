(ns middleware.compiler.dead-code-remover
  (:require [middleware.utils.core :refer [seek]]
            [middleware.compiler.utils.ast :as ast-utils]))

(declare ^:private visit-node)

(defn ^:private visit [node ctx]
  (visit-node node (update ctx :path conj node)))

(defn ^:private visit-children [node ctx]
  (doseq [child (ast-utils/children node)]
    (visit child ctx)))

(defn ^:private visit-program [{:keys [scripts]} ctx]
  (doseq [root (filter #(contains? #{"running" "once"} (:state %))
                       scripts)]
    (visit root ctx)))

(defn ^:private visit-script [{:keys [name] :as script} ctx]
  (when-not (contains? (:scripts @(:visited ctx)) name)
    (vswap! (:visited ctx) update :scripts conj name)
    (visit-children script ctx)))

(defn ^:private get-script-named [name ctx]
  (seek (fn [script] (= name (:name script)))
        (-> ctx :path last :scripts)))

(defn ^:private visit-script-control [{:keys [scripts]} ctx]
  (let [visited-scripts (:scripts @(:visited ctx))]
    (doseq [script (map #(get-script-named % ctx)
                        (remove visited-scripts scripts))]
      (visit script ctx))))

(defn ^:private visit-variable [node ctx]
  (when (ast-utils/global? node (:path ctx))
    (vswap! (:visited ctx) update :globals conj (:name node)))
  (visit-children node ctx))

(defn ^:private visit-call
  [{:keys [primitive-name selector] :as node} ctx]
  (if primitive-name
    (vswap! (:visited ctx) update :primitives conj selector)
    (when-not (contains? (:scripts @(:visited ctx)) selector)
      (visit (get-script-named selector ctx) ctx)))
  (visit-children node ctx))

(defn ^:private visit-node [node ctx]
  (case (ast-utils/node-type node)
    "UziProgramNode"        (visit-program node ctx)
    "UziTaskNode"           (visit-script node ctx)
    "UziFunctionNode"       (visit-script node ctx)
    "UziProcedureNode"      (visit-script node ctx)
    "UziScriptStartNode"    (visit-script-control node ctx)
    "UziScriptStopNode"     (visit-script-control node ctx)
    "UziScriptResumeNode"   (visit-script-control node ctx)
    "UziScriptPauseNode"    (visit-script-control node ctx)
    "UziVariableNode"       (visit-variable node ctx)
    "UziCallNode"           (visit-call node ctx)
    (visit-children node ctx)))

(defn ^:private create-context []
  {:path (list),
   :visited (volatile! {:scripts #{}
                        :globals #{}
                        :primitives #{}})})

(defn remove-dead-code [ast]
  (let [ctx (create-context)]
    (visit ast ctx)
    (let [{:keys [globals scripts primitives]} @(:visited ctx)]
      (assoc ast
             :globals (filterv #(contains? globals (:name %))
                               (:globals ast))
             :scripts (filterv #(contains? scripts (:name %))
                               (:scripts ast))
             :primitives (filterv #(contains? primitives (:alias %))
                                  (:primitives ast))))))
