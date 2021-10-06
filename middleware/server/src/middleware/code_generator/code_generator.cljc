(ns middleware.code-generator.code-generator
  (:refer-clojure :exclude [print])
  (:require [clojure.string :as str]
            [middleware.utils.core :refer [uzi-format]]))

; TODO(Richo): Concatenating strings seems very inefficient

(defmulti print-node :__class__)

(defn print [node] (print-node node))

(defn print-optional-block [block]
  (if (empty? (:statements block))
    ";"
    (str " " (print-node block))))

(defn- remove-empty [& colls]
  (remove empty? colls))

(defmethod print-node "UziProgramNode" [node]
  (str/join (flatten
             (interpose "\n\n"
                        (remove-empty
                         (interpose "\n" (map print-node (:imports node)))
                         (interpose "\n" (map print-node (:globals node)))
                         (interpose "\n" (map print-node (:primitives node)))
                         (interpose "\n\n" (map print-node (:scripts node))))))))

(defmethod print-node "UziPrimitiveDeclarationNode" [node]
  (uzi-format "prim %1;"
              (if (= (:alias node) (:name node))
                (:alias node)
                (uzi-format "%1 : %2"
                            (:alias node)
                            (:name node)))))

(defmethod print-node "UziImportNode" [node]
  (uzi-format "import %1 from '%2'%3"
              (:alias node)
              (:path node)
              (print-optional-block (:initializationBlock node))))

(defmethod print-node "UziVariableDeclarationNode" [node]
  (if (:value node)
    (uzi-format "var %1 = %2;"
                (:name node)
                (print-node (:value node)))
    (uzi-format "var %1;"
                (:name node))))

(defmethod print-node "UziNumberLiteralNode" [node]
  (str (:value node)))

(defmethod print-node "UziPinLiteralNode" [node]
  (str (:type node) (:number node)))

(defmethod print-node "UziTaskNode" [node]
  (uzi-format "task %1()%2%3 %4"
              (:name node)
              (if (= "once" (:state node)) "" (str " " (:state node)))
              (if (nil? (:tickingRate node)) "" (print-node (:tickingRate node)))
              (print-node (:body node))))

(defmethod print-node "UziFunctionNode" [node]
  (uzi-format "func %1(%2) %3"
              (:name node)
              (str/join ", " (map :name (:arguments node)))
              (print-node (:body node))))

(defmethod print-node "UziProcedureNode" [node]
  (uzi-format "proc %1(%2) %3"
              (:name node)
              (str/join ", " (map :name (:arguments node)))
              (print-node (:body node))))

(defmethod print-node "UziTickingRateNode" [node]
  (uzi-format " %1/%2" (:value node) (:scale node)))

(defn add-indent-level [lines]
  (str/join (map (fn [line] (str "\t" line "\n"))
                 (filter (fn [line] (and (not= "\n" line)
                                         (not= "" line)))
                         (str/split-lines lines)))))

(defmethod print-node "UziBlockNode" [node]
  (if (empty? (:statements node))
    "{}"
    (uzi-format "{\n%1}"
                (add-indent-level
                 (str/join "\n"
                           (map (fn [expr]
                                  (if (or (str/ends-with? expr "}")
                                          (str/ends-with? expr ";"))
                                    expr
                                    (str expr ";")))
                                (map print-node (:statements node))))))))

(defn print-operator-expression [node]
  (if (= 1 (-> node :arguments count))
    ;unary
    (uzi-format "(%1%2)"
                (:selector node)
                (print-node (first (:arguments node))))
    ;binary
    (uzi-format "(%1 %2 %3)"
                (print-node (first (:arguments node)))
                (:selector node)
                (print-node (second (:arguments node))))))

(defmethod print-node "UziCallNode" [node]
  (if (nil? (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+"
                        (:selector node)))
    ;non-operator
    (uzi-format "%1(%2)"
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
    (uzi-format "return %1" (print-node (:value node)))))

(defmethod print-node "UziYieldNode" [node] "yield")

(defmethod print-node "UziForNode" [node]
  (uzi-format "for %1 = %2 to %3 by %4 %5"
              (:name (:counter node))
              (print-node (:start node))
              (print-node (:stop node))
              (print-node (:step node))
              (print-node (:body node))))

(defmethod print-node "UziWhileNode" [node]
  (uzi-format "while %1%2"
              (print-node (:condition node))
              (print-optional-block (:post node))))

(defmethod print-node "UziDoWhileNode" [node]
  (uzi-format "do %1 while(%2)"
              (print-node (:pre node))
              (print-node (:condition node))))

(defmethod print-node "UziUntilNode" [node]
  (uzi-format "until %1%2"
              (print-node (:condition node))
              (print-optional-block (:post node))))

(defmethod print-node "UziDoUntilNode" [node]
  (uzi-format "do %1 until(%2)"
              (print-node (:pre node))
              (print-node (:condition node))))

(defmethod print-node "UziForeverNode" [node]
  (uzi-format "forever %1"
              (print-node (:body node))))

(defmethod print-node "UziRepeatNode" [node]
  (uzi-format "repeat %1 %2"
              (print-node (:times node))
              (print-node (:body node))))

(defmethod print-node "UziConditionalNode" [node]
  (let [trueBranch (uzi-format "if %1 %2"
                               (print-node (:condition node))
                               (print-node (:trueBranch node)))]
    (if (empty? (-> node :falseBranch :statements))
      trueBranch
      (str trueBranch " else " (print-node (:falseBranch node))))))

(defmethod print-node "UziAssignmentNode" [node]
  (uzi-format "%1 = %2"
              (print-node (:left node))
              (print-node (:right node))))

(defmethod print-node "UziScriptStartNode" [node]
  (uzi-format "start %1"
              (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptStopNode" [node]
  (uzi-format "stop %1"
              (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptPauseNode" [node]
  (uzi-format "pause %1"
              (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptResumeNode" [node]
  (uzi-format "resume %1"
              (str/join ", " (:scripts node))))

(defmethod print-node "UziLogicalAndNode" [node]
  (uzi-format "(%1 && %2)"
              (print-node (:left node))
              (print-node (:right node))))

(defmethod print-node "UziLogicalOrNode" [node]
  (uzi-format "(%1 || %2)"
              (print-node (:left node))
              (print-node (:right node))))

(defmethod print-node :default [node]
  (throw (ex-info "Not Implemented node reached: " {:node node})))
