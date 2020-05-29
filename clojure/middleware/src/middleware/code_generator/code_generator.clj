(ns middleware.code_generator.code_generator)


(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defmethod print-node "UziProgramNode" [node] "")


(defmethod print-node :default [arg] (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)) )))