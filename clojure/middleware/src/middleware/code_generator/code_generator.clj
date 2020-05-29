(ns middleware.code_generator.code_generator)


(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defmethod print-node "UziProgramNode" [node] (clojure.string/join "\n" (map print-node (:globals node))))

(defmethod print-node "UziVariableDeclarationNode" [node] (format "var %s = %s;", (:name node) (print-node (:value node))))
(defmethod print-node "UziNumberLiteralNode" [node] (str (:value node)))
(defmethod print-node "UziPinLiteralNode" [node] (str (:type node) (:number node)))

(defmethod print-node :default [arg] (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)) )))