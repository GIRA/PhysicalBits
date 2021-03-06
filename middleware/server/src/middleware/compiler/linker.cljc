(ns middleware.compiler.linker
  (:require [middleware.compiler.utils.ast :as ast-utils]
            [middleware.utils.fs :as fs]
            [middleware.parser.parser :as parser]
            [clojure.pprint :refer [pprint]]))

; NOTE(Richo): Cache to avoid parsing the same file several times if it didn't change.
(def parser-cache (atom {}))
(defn parse [^java.io.File file]
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
     "UziCallNode" (fn [{:keys [selector] :as node} _]
                     (if (contains? scripts selector)
                       node
                       (assoc node
                              :primitive-name (core-primitives selector)))))))

(defn apply-alias [ast alias]
  (if-not alias
    ast
    (let [with-alias #(str alias "." %)
           update (fn [key node _] (assoc node key (-> node key with-alias)))
           update-name (partial update :name)
           update-selector (partial update :selector)
           update-alias (partial update :alias)
           update-variable (fn [node path]
                             (if (ast-utils/local? node path)
                               node
                               (update :name node path)))
           update-script-list (fn [node _] (assoc node :scripts (mapv with-alias (:scripts node))))]
      (ast-utils/transform
       ast

       "UziVariableDeclarationNode" update-variable
       "UziVariableNode" update-variable

       "UziTaskNode" update-name
       "UziFunctionNode" update-name
       "UziProcedureNode" update-name

       "UziCallNode" update-selector

       "UziScriptStopNode" update-script-list
       "UziScriptStartNode" update-script-list
       "UziScriptPauseNode" update-script-list
       "UziScriptResumeNode" update-script-list

       "UziPrimitiveDeclarationNode" update-alias))))

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
                            (if (= "once" state)
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
                             ast-utils/assign-internal-ids
                             (apply-initialization-block initializationBlock)
                             (resolve-imports libs-dir
                                              (implicit-imports imp)
                                              (conj visited-imports
                                                    {:alias alias :path path})))]
        {:import (assoc imp
                        :isResolved true
                        :program imported-ast)
         :program (apply-alias imported-ast alias)})
      (throw (ex-info "File not found"
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
   (let [resolved-imports (map (fn [imp] (resolve-import imp libs-dir visited-imports))
                               (concat implicit-imports
                                       (filter (complement :isResolved)
                                               (:imports ast))))]
     (-> ast
         (build-new-program resolved-imports)
         bind-primitives))))
