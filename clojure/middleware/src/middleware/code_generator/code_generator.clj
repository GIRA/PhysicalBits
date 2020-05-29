(ns middleware.code_generator.code_generator)

(defmulti print-program :__class__)
(defmethod print-program "UziProgramNode" [node] "")


(defmethod print-program :default [arg] (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)) )))