(ns middleware.compilation.linker
  (:require [middleware.ast.utils :as ast-utils]
            [middleware.utils.fs.common :as fs]
            [middleware.compilation.parser :as parser]))

; NOTE(Richo): Cache to avoid parsing the same file several times if it didn't change.
(def parser-cache (atom {}))
(defn parse [file]
  (let [path (fs/absolute-path file)
        last-modified (fs/last-modified file)
        entry (get @parser-cache path)]
    (if (= last-modified
           (get entry :last-modified))
      (get entry :content)
      (let [content (parser/parse (fs/read file))]
        (swap! parser-cache assoc path {:last-modified last-modified
                                        :content content})
        content))))

(defn bind-primitives [ast]
  (let [scripts (set (map :name
                          (:scripts ast)))
        core-primitives (into {} (map (fn [{:keys [name alias]}] [alias name])
                                      (:primitives ast)))]
    (ast-utils/transform
     ast
     ; NOTE(Richo): We should only associate a prim name if the selector doesn't match
     ; an existing script. Scripts have precedence over primitives!
     (fn [node]
       (if (ast-utils/call? node)
         (let [selector (node :selector)]
           (if (contains? scripts selector)
             node
             (assoc node
                    :primitive-name (core-primitives selector))))
         node)))))

(defn apply-alias [ast alias]
  (let [with-alias #(str alias "." %)
        update (fn [key node] (assoc node key (-> node key with-alias)))
        update-name (partial update :name)
        update-selector (partial update :selector)
        update-alias (partial update :alias)
        update-variable (fn [node path]
                          (if (ast-utils/local? node path)
                            node
                            (update :name node)))
        update-script-list (fn [node] (assoc node :scripts (mapv with-alias (:scripts node))))]
    (ast-utils/transform-with-path
     ast
     (fn [node path]
       (case (ast-utils/node-type node)
         "UziVariableDeclarationNode" (update-variable node path)
         "UziVariableNode" (update-variable node path)

         "UziTaskNode" (update-name node)
         "UziFunctionNode" (update-name node)
         "UziProcedureNode" (update-name node)

         "UziCallNode" (update-selector node)

         "UziScriptStopNode" (update-script-list node)
         "UziScriptStartNode" (update-script-list node)
         "UziScriptPauseNode" (update-script-list node)
         "UziScriptResumeNode" (update-script-list node)

         "UziPrimitiveDeclarationNode" (update-alias node)
         node)))))

(defn apply-initialization-block [ast {:keys [statements] :as init-block}]
  (let [globals (into {}
                      (map (fn [node] [(-> node :left :name), (-> node :right)])
                           (filter (fn [node]
                                     (and (= "UziAssignmentNode" (ast-utils/node-type node))
                                          (ast-utils/compile-time-constant? (:right node))))
                                   statements)))
        scripts (into {}
                      (mapcat (fn [{:keys [scripts] :as node}]
                                (let [state (ast-utils/script-control-state node)]
                                  (map (fn [name] [name state])
                                       scripts)))
                              (filter ast-utils/script-control?
                                      statements)))]
    (assoc ast
           :globals (mapv (fn [{:keys [name value] :as global}]
                            (assoc global :value (get globals name value)))
                          (:globals ast))
           :scripts (mapv (fn [{:keys [name state] :as script}]
                            (if (or (nil? state)
                                    (= "once" state))
                              script
                              (assoc script :state (get scripts name state))))
                          (:scripts ast)))))

(defn implicit-imports
  ([] [{:__class__ "UziImportNode" :path "core.uzi"}])
  ([import]
   (filterv #(not= (:path import) (:path %))
            (implicit-imports))))

(declare resolve-imports) ; Forward declaration to be able to call it from resolve-import

(defn resolve-import
  [{:keys [alias path initializationBlock] :as imp}, libs-dir, visited-imports]
  (when (contains? visited-imports {:alias alias :path path})
    (throw (ex-info "Dependency cycle detected" {:import imp, :visited visited-imports})))
  (let [file (fs/file libs-dir path)]
    (if (fs/exists? file)
      (let [imported-ast (-> (parse file)
                             (apply-initialization-block initializationBlock)
                             (resolve-imports libs-dir
                                              (implicit-imports imp)
                                              (conj visited-imports
                                                    {:alias alias :path path})))]
        {:import (vary-meta imp assoc :program imported-ast)
         :program (if alias
                    (apply-alias imported-ast alias)
                    imported-ast)})
      (throw (ex-info ; TODO(Richo): Improve this error message so that we can translate it
              (str "File not found: " path)
              {:import imp
               :file file
               :absolute-path (fs/absolute-path file)})))))

(defn build-new-program [ast resolved-imports]
  (let [imported-programs (map :program resolved-imports)
         imported-globals (mapcat :globals imported-programs)
         imported-scripts (mapcat :scripts imported-programs)
         imported-prims (mapcat :primitives imported-programs)]
    (assoc ast
           :imports (mapv :import resolved-imports)
           :globals (vec (concat imported-globals (:globals ast)))
           :scripts (vec (concat imported-scripts (:scripts ast)))
           :primitives (vec (concat imported-prims (:primitives ast))))))

(defn resolve-imports
  ([ast libs-dir]
   (resolve-imports ast libs-dir (implicit-imports) #{}))
  ([ast libs-dir implicit-imports visited-imports]
   (let [resolved-imports (mapv (fn [imp] (resolve-import imp libs-dir visited-imports))
                                (concat implicit-imports (:imports ast)))]
     (-> ast
         (build-new-program resolved-imports)
         bind-primitives))))
