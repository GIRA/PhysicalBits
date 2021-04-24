(ns middleware.code-generator.code-generator
  (:refer-clojure :exclude [print format])
  (:require [clojure.string :as str]))

; TODO(Richo): Concatenating strings seems very inefficient

(defn format [text & args]
  (loop [t text, i 0]
    (if-let [val (nth args i nil)]
      (recur
        (str/replace t (str "%" (inc i)) (str val))
        (inc i))
      t)))

(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defn print-optative-block [block]
  (if (empty? (:statements block))
    ";"
    (str " " (print-node block))))

(defn- remove-empty [& colls]
  (filter (complement empty?) colls))

(defmethod print-node "UziProgramNode" [node]
  (str/join (flatten
             (interpose "\n\n"
                        (remove-empty
                         (interpose "\n" (map print-node (:imports node)))
                         (interpose "\n" (map print-node (:globals node)))
                         (interpose "\n" (map print-node (:primitives node)))
                         (interpose "\n\n" (map print-node (:scripts node))))))))

(defmethod print-node "UziPrimitiveDeclarationNode" [node]
  (format "prim %1;"
          (if (= (:alias node) (:name node))
            (:alias node)
            (format "%1 : %2"
                    (:alias node)
                    (:name node)))))

(defmethod print-node "UziImportNode" [node]
  (format "import %1 from '%2'%3"
          (:alias node)
          (:path node)
          (print-optative-block (:initializationBlock node))))

(defmethod print-node "UziVariableDeclarationNode" [node]
  (if (:value node)
    (format "var %1 = %2;"
            (:name node)
            (print-node (:value node)))
    (format "var %1;"
            (:name node))))

(defmethod print-node "UziNumberLiteralNode" [node]
  (str (:value node)))

(defmethod print-node "UziPinLiteralNode" [node]
  (str (:type node) (:number node)))

(defmethod print-node "UziTaskNode" [node]
  (format "task %1()%2%3 %4"
          (:name node)
          (if (= "once" (:state node)) "" (str " " (:state node)))
          (if (nil? (:tickingRate node)) "" (print-node (:tickingRate node)))
          (print-node (:body node))))

(defmethod print-node "UziFunctionNode" [node]
  (format "func %1(%2) %3"
          (:name node)
          (str/join ", " (map :name (:arguments node)))
          (print-node (:body node))))

(defmethod print-node "UziProcedureNode" [node]
  (format "proc %1(%2) %3"
          (:name node)
          (str/join ", " (map :name (:arguments node)))
          (print-node (:body node))))

(defmethod print-node "UziTickingRateNode" [node]
  (format " %1/%2" (:value node) (:scale node)))

(defn add-indent-level [lines]
  (str/join (map (fn [line] (str "\t" line "\n"))
                 (filter (fn [line] (and (not= "\n" line)
                                         (not= "" line)))
                         (str/split-lines lines)))))

(defmethod print-node "UziBlockNode" [node]
  (if (empty? (:statements node))
    "{}"
    (format "{\n%1}"
            (add-indent-level (str/join "\n"
                                        (map (fn [expr]
                                               (if (or (str/ends-with? expr "}")
                                                       (str/ends-with? expr ";"))
                                                 expr
                                                 (str expr ";")))
                                             (map print-node (:statements node))))))))

(defn print-operator-expression [node]
  (if (= 1 (-> node :arguments count))
    ;unary
    (format "(%1%2)"
            (:selector node)
            (print-node (first (:arguments node))))
    ;binary
    (format "(%1 %2 %3)"
            (print-node (first (:arguments node)))
            (:selector node)
            (print-node (second (:arguments node))))))

(defmethod print-node "UziCallNode" [node]
  (if (nil? (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+"
                        (:selector node)))
    ;non-operator
    (format "%1(%2)"
            (:selector node)
            (str/join ", " (map print-node (:arguments node))))
    (print-operator-expression node)))

(defmethod print-node "Association" [node]
  (str (if (nil? (:key node)) "" (str (:key node) ": "))
       (print-node (:value node))))

(defmethod print-node "UziVariableNode" [node] (:name node))

(defmethod print-node "UziReturnNode" [node]
  (if (nil? (:value node))
    "return"
    (format "return %1" (print-node (:value node)))))

(defmethod print-node "UziYieldNode" [node] "yield")

(defmethod print-node "UziForNode" [node]
  (format "for %1 = %2 to %3 by %4 %5"
          (:name (:counter node))
          (print-node (:start node))
          (print-node (:stop node))
          (print-node (:step node))
          (print-node (:body node))))

(defmethod print-node "UziWhileNode" [node]
  (format "while %1%2"
          (print-node (:condition node))
          (print-optative-block (:post node))))

(defmethod print-node "UziDoWhileNode" [node]
  (format "do %1 while(%2)"
          (print-node (:pre node))
          (print-node (:condition node))))

(defmethod print-node "UziUntilNode" [node]
  (format "until %1%2"
          (print-node (:condition node))
          (print-optative-block (:post node))))

(defmethod print-node "UziDoUntilNode" [node]
  (format "do %1 until(%2)"
          (print-node (:pre node))
          (print-node (:condition node))))

(defmethod print-node "UziForeverNode" [node]
  (format "forever %1"
          (print-node (:body node))))

(defmethod print-node "UziRepeatNode" [node]
  (format "repeat %1 %2"
          (print-node (:times node))
          (print-node (:body node))))

(defmethod print-node "UziConditionalNode" [node]
  (let [trueBranch (format "if %1 %2"
                           (print-node (:condition node))
                           (print-node (:trueBranch node)))]
    (if (empty? (-> node :falseBranch :statements))
      trueBranch
      (str trueBranch " else " (print-node (:falseBranch node))))))

(defmethod print-node "UziAssignmentNode" [node]
  (format "%1 = %2"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node "UziScriptStartNode" [node]
  (format "start %1"
          (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptStopNode" [node]
  (format "stop %1"
          (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptPauseNode" [node]
  (format "pause %1"
          (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptResumeNode" [node]
  (format "resume %1"
          (str/join ", " (:scripts node))))

(defmethod print-node "UziLogicalAndNode" [node]
  (format "(%1 && %2)"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node "UziLogicalOrNode" [node]
  (format "(%1 || %2)"
          (print-node (:left node))
          (print-node (:right node))))

(defmethod print-node :default [node]
  (throw (ex-info "Not Implemented node reached: " {:node node})))
