(ns middleware.code_generator.code_generator)


(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defmethod print-node "UziProgramNode" [node]
  (str
    (clojure.string/join "\n" (map print-node (:globals node)))
    (clojure.string/join "\n" (map print-node (:scripts node)))))

(defmethod print-node "UziVariableDeclarationNode" [node] (format "var %s = %s;", (:name node) (print-node (:value node))))
(defmethod print-node "UziNumberLiteralNode" [node] (str (:value node)))
(defmethod print-node "UziPinLiteralNode" [node] (str (:type node) (:number node)))
(defmethod print-node "UziTaskNode" [node] (format "task %s()%s%s\n%s"
                                                   (:name node)
                                                   (if (= "once" (:state node)) "" (str " " (:state node)))
                                                   (if (nil? (:tickingRate node)) "" (print-node (:tickingRate node)))
                                                   (print-node (:body node))))

(defmethod print-node "UziTickingRateNode" [node] (format " %d/%s" (:value node) (:scale node)))

(defmethod print-node "UziBlockNode" [node] (format "{\n%s}"
                                                    (clojure.string/join
                                                                         ;TODO(Tera): this is actually a good place to add tabs and indent code
                                                                         (map (fn [expr] (str expr ";\n"))
                                                                              (map print-node (:statements node))))))
(defmethod print-node "UziCallNode" [node] (format "%s(%s)"
                                                   (:selector node)
                                                   (clojure.string/join ", " (map print-node (:arguments node)))))
(defmethod print-node "Association" [node] (str (if (nil? (:key node)) "" (str (:key node) " : "))
                                                (print-node (:value node))))

(defmethod print-node :default [arg] (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)) )))