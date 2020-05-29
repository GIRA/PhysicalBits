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
(defmethod print-node "UziFunctionNode" [node] (format "func %s(%s)\n%s"
                                                        (:name node)
                                                        (clojure.string/join ", " (map :name (:arguments node)))
                                                        (print-node (:body node))))
(defmethod print-node "UziProcedureNode" [node] (format "proc %s(%s)\n%s"
                                                        (:name node)
                                                        (clojure.string/join ", " (map :name (:arguments node)))
                                                        (print-node (:body node))))
(defmethod print-node "UziTickingRateNode" [node] (format " %d/%s" (:value node) (:scale node)))

(defn add-indent-level [lines]
  (clojure.string/join (map (fn [line] (str "\t" line "\n")) (filter (fn [line] (and (not= "\n" line) (not= "" line)) )
                                                                     (clojure.string/split-lines lines)))))
(defmethod print-node "UziBlockNode" [node] (format "{\n%s}"
                                                    (add-indent-level(clojure.string/join
                                                                         (map (fn [expr] (str expr ";\n"))
                                                                              (map print-node (:statements node)))))))

(defn print-binary-expression [node]
  (format "(%s %s %s)"
          (print-node (first (:arguments node)))
          (:selector node)
          (print-node (second (:arguments node)))))

(defmethod print-node "UziCallNode" [node]
  (if (nil? (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+" (:selector node)))
    ;non-binary
    (format "%s(%s)"
            (:selector node)
            (clojure.string/join ", " (map print-node (:arguments node))))
    (print-binary-expression node)    )  )
(defmethod print-node "Association" [node] (str (if (nil? (:key node)) "" (str (:key node) " : "))
                                                (print-node (:value node))))
(defmethod print-node "UziVariableNode" [node] (:name node))
(defmethod print-node "UziReturnNode" [node] (format "return %s" (print-node (:value node))))

(defmethod print-node "UziForNode" [node]
  (format "for %s = %s to %s by %s\n%s"
          (:name (:counter node))
          (print-node (:start node))
          (print-node (:stop node))
          (print-node (:step node))
          (print-node (:body node))))
(defmethod print-node "UziAssignmentNode" [node]
  (format "%s = %s"
          (print-node (:left node))
          (print-node (:right node))))


(defmethod print-node :default [arg] (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)) )))