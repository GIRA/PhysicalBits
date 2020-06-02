(ns middleware.code_generator.code_generator)


(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defn print-optative-block [block]
  (if (= 0 (count (:statements block)))
    ";"
    (str " " (print-node block))))

(defmethod print-node "UziProgramNode" [node]
  (str
    (clojure.string/join "\n" (concat
                                (map print-node (:imports node))
                                (map (fn [node] (str (print-node node) ";")) (:globals node))
                                (map print-node (:primitives node))
                                (map print-node (:scripts node))))))

(defmethod print-node "UziPrimitiveDeclarationNode" [node]
  (format "prim %s;"
          (if (= (:alias node) (:name node))
            (:alias node)
            (format "%s : %s"
                    (:alias node)
                    (:name node)))))

(defmethod print-node "UziImportNode" [node]
  (format "import %s from '%s'%s"
          (:alias node)
          (:path node)
          (print-optative-block (:initializationBlock node))))

(defmethod print-node "UziVariableDeclarationNode" [node]
  (format "var %s = %s", (:name node) (print-node (:value node))))

(defmethod print-node "UziNumberLiteralNode" [node]
  (str (:value node)))

(defmethod print-node "UziPinLiteralNode" [node]
  (str (:type node) (:number node)))

(defmethod print-node "UziTaskNode" [node]
  (format "task %s()%s%s %s"
          (:name node)
          (if (= "once" (:state node)) "" (str " " (:state node)))
          (if (nil? (:tickingRate node)) "" (print-node (:tickingRate node)))
          (print-node (:body node))))

(defmethod print-node "UziFunctionNode" [node]
  (format "func %s(%s) %s"
          (:name node)
          (clojure.string/join ", " (map :name (:arguments node)))
          (print-node (:body node))))

(defmethod print-node "UziProcedureNode" [node]
  (format "proc %s(%s) %s"
          (:name node)
          (clojure.string/join ", " (map :name (:arguments node)))
          (print-node (:body node))))

(defmethod print-node "UziTickingRateNode" [node]
  (format " %d/%s" (:value node) (:scale node)))

(defn add-indent-level [lines]
  (clojure.string/join (map (fn [line] (str "\t" line "\n"))
                            (filter (fn [line] (and (not= "\n" line) (not= "" line)))
                                    (clojure.string/split-lines lines)))))

(defmethod print-node "UziBlockNode" [node]
  (format "{\n%s}"
          (add-indent-level (clojure.string/join "\n"
                                                 (map (fn [expr]
                                                        (if (or (clojure.string/ends-with? expr "}")
                                                                (clojure.string/ends-with? expr ";"))
                                                          expr
                                                          (str expr ";")))
                                                      (map print-node (:statements node)))))))

(defn print-operator-expression [node]
  (if (= 1 (-> node :arguments count))
    ;unary
    (format "(%s%s)"
            (:selector node)
            (print-node (first (:arguments node))))
    ;binary
    (format "(%s %s %s)"
            (print-node (first (:arguments node)))
            (:selector node)
            (print-node (second (:arguments node))))))

(defmethod print-node "UziCallNode" [node]
  (if (nil? (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+" (:selector node)))
    ;non-operator
    (format "%s(%s)"
            (:selector node)
            (clojure.string/join ", " (map print-node (:arguments node))))
    (print-operator-expression node)))

(defmethod print-node "Association" [node]
  (str (if (nil? (:key node)) "" (str (:key node) ": "))
       (print-node (:value node))))

(defmethod print-node "UziVariableNode" [node] (:name node))

(defmethod print-node "UziReturnNode" [node]
  (if (nil? (:value node))
    "return"
    (format "return %s" (print-node (:value node)))))

(defmethod print-node "UziYieldNode" [node] "yield")

(defmethod print-node "UziForNode" [node]
  (format "for %s = %s to %s by %s %s"
          (:name (:counter node))
          (print-node (:start node))
          (print-node (:stop node))
          (print-node (:step node))
          (print-node (:body node))))

(defmethod print-node "UziWhileNode" [node]
  (format "while %s%s"
          (print-node (:condition node))
          (print-optative-block (:post node))))

(defmethod print-node "UziDoWhileNode" [node]
  (format "do %s while(%s)"
          (print-node (:pre node))
          (print-node (:condition node))))

(defmethod print-node "UziUntilNode" [node]
  (format "until %s%s"
          (print-node (:condition node))
          (print-optative-block (:post node))))

(defmethod print-node "UziDoUntilNode" [node]
  (format "do %s until(%s)"
          (print-node (:pre node))
          (print-node (:condition node))))

(defmethod print-node "UziForeverNode" [node]
  (format "forever %s"
          (print-node (:body node))))

(defmethod print-node "UziRepeatNode" [node]
  (format "repeat %s %s"
          (print-node (:times node))
          (print-node (:body node))))

(defmethod print-node "UziConditionalNode" [node]
  (let [trueBranch (format "if %s %s"
                           (print-node (:condition node))
                           (print-node (:trueBranch node)))]
    (if (= 0 (-> node :falseBranch :statements count))
      trueBranch
      (str trueBranch " else " (print-node (:falseBranch node))))))

(defmethod print-node "UziAssignmentNode" [node]
  (format "%s = %s"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node "UziScriptStartNode" [node]
  (format "start %s"
          (clojure.string/join ", " (:scripts node))))

(defmethod print-node "UziScriptStopNode" [node]
  (format "stop %s"
          (clojure.string/join ", " (:scripts node))))

(defmethod print-node "UziScriptPauseNode" [node]
  (format "pause %s"
          (clojure.string/join ", " (:scripts node))))

(defmethod print-node "UziScriptResumeNode" [node]
  (format "resume %s"
          (clojure.string/join ", " (:scripts node))))

(defmethod print-node "UziLogicalAndNode" [node]
  (format "(%s && %s)"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node "UziLogicalOrNode" [node]
  (format "(%s || %s)"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node :default [arg]
  (throw (Exception. (str "Not Implemented node reached: " (:__class__ arg)))))
