(ns middleware.compiler.checker
  (:require [middleware.compiler.ast-utils :as ast-utils]))

(defn- register-error! [description node errors]
  (swap! errors conj {:node node
                      :description description}))

(defn- assert [bool description node errors]
  (when (not bool)
    (register-error! description node errors))
  bool)

(defmulti check-node (fn [node errors path] (:__class__ node)))

(defn- check [node errors path]
  (check-node node errors (conj path node))
  (doseq [child-node (ast-utils/children node)]
    (check child-node)))

(defmethod check-node "UziProgramNode" [node errors path]
  (let [imports (:imports node)]
    (doseq [import imports]
      )))

(defmethod check-node :default [node _ _]
  (throw (ex-info "MISSING: " node)))

(defn check-tree [ast]
  (let [errors (atom [])
        path (list)]
    (check ast errors path)
    @errors))
