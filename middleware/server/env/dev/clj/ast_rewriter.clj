(ns ast-rewriter
  (:require [clojure.walk :as w]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]
            [clojure.string :as str]))

(defmulti ^:private rewrite-node :__class__)

(defmethod rewrite-node :default [node] node)

(defmethod rewrite-node "UziProgramNode" [{:keys [imports globals scripts primitives]}]
 (apply list
   (cond-> ['ast/program-node]
           (not (empty? imports)) (conj :imports imports)
           (not (empty? globals)) (conj :globals globals)
           (not (empty? scripts)) (conj :scripts scripts)
           (not (empty? primitives)) (conj :primitives primitives))))

(defmethod rewrite-node "UziFunctionNode" [{:keys [name arguments body]}]
  (apply list
    (cond-> ['ast/function-node :name name]
            (not (empty? arguments)) (conj :arguments arguments)
            (not (empty? body)) (conj :body body))))

(defmethod rewrite-node "UziProcedureNode" [{:keys [name arguments body]}]
  (apply list
    (cond-> ['ast/procedure-node :name name]
            (not (empty? arguments)) (conj :arguments arguments)
            (not (empty? body)) (conj :body body))))

(defmethod rewrite-node "UziTaskNode" [{:keys [name arguments tickingRate state body]}]
  (apply list
    (cond-> ['ast/task-node :name name]
            (not (empty? arguments)) (conj :arguments arguments)
            tickingRate (conj :tick-rate tickingRate)
            state (conj :state state)
            (not (empty? body)) (conj :body body))))

(defmethod rewrite-node "UziTickingRateNode" [{:keys [value scale]}]
  (list 'ast/ticking-rate-node value scale))

(defmethod rewrite-node "UziBlockNode" [{:keys [statements]}]
  (list 'ast/block-node statements))

(defmethod rewrite-node "UziCommentNode" [node]
  (list 'ast/comment-node (:value node)))

(defmethod rewrite-node "UziImportNode" [{:keys [alias path initializationBlock]}]
  (if initializationBlock
    (list 'ast/import-node alias path initializationBlock)
    (if alias
      (list 'ast/import-node alias path)
      (list 'ast/import-node path))))

(defmethod rewrite-node "UziNumberLiteralNode" [node]
  (list 'ast/literal-number-node (:value node)))

(defmethod rewrite-node "UziPinLiteralNode" [node]
  (list 'ast/literal-pin-node (:type node) (:number node)))

(defmethod rewrite-node "UziAssignmentNode" [node]
  (list 'ast/assignment-node (:left node) (:right node)))

(defmethod rewrite-node "Association" [{:keys [key value]}]
  (if key
    (list 'ast/arg-node key value)
    (list 'ast/arg-node value)))

(defmethod rewrite-node "UziVariableDeclarationNode" [{:keys [name value]}]
  (if value
    (list 'ast/variable-declaration-node name value)
    (list 'ast/variable-declaration-node name)))

(defmethod rewrite-node "UziVariableNode" [node]
  (list 'ast/variable-node (:name node)))

(defmethod rewrite-node "UziReturnNode" [node]
  (if (:value node)
    (list 'ast/return-node (:value node))
    (list 'ast/return-node)))

(defmethod rewrite-node "UziCallNode" [{:keys [selector arguments]}]
  (list 'ast/call-node selector arguments))

(defmethod rewrite-node "UziForNode" [node]
  (list 'ast/for-node
        (-> node :counter :name)
        (:start node)
        (:stop node)
        (:step node)
        (:body node)))

(defmethod rewrite-node "UziWhileNode" [node]
  (list 'ast/while-node (:condition node) (:post node)))

(defmethod rewrite-node "UziUntilNode" [node]
  (list 'ast/until-node (:condition node) (:post node)))

(defmethod rewrite-node "UziDoWhileNode" [node]
  (list 'ast/do-while-node (:condition node) (:pre node)))

(defmethod rewrite-node "UziDoUntilNode" [node]
  (list 'ast/do-until-node (:condition node) (:pre node)))

(defmethod rewrite-node "UziYieldNode" [node]
  (list 'ast/yield-node))

(defmethod rewrite-node "UziForeverNode" [node]
  (list 'ast/forever-node (:body node)))

(defmethod rewrite-node "UziRepeatNode" [node]
  (list 'ast/repeat-node (:times node) (:body node)))

(defmethod rewrite-node "UziConditionalNode" [{:keys [condition trueBranch falseBranch]}]
  (if falseBranch
    (list 'ast/conditional-node condition trueBranch falseBranch)
    (list 'ast/conditional-node condition trueBranch)))

(defmethod rewrite-node "UziLogicalAndNode" [{:keys [left right]}]
  (list 'ast/logical-and-node left right))

(defmethod rewrite-node "UziLogicalOrNode" [{:keys [left right]}]
  (list 'ast/logical-or-node left right))

(defmethod rewrite-node "UziScriptStartNode" [node]
  (list 'ast/start-node (:scripts node)))

(defmethod rewrite-node "UziScriptStopNode" [node]
  (list 'ast/stop-node (:scripts node)))

(defmethod rewrite-node "UziScriptPauseNode" [node]
  (list 'ast/pause-node (:scripts node)))

(defmethod rewrite-node "UziScriptResumeNode" [node]
  (list 'ast/resume-node (:scripts node)))

(defmethod rewrite-node "UziPrimitiveDeclarationNode" [{:keys [alias name]}]
  (if (= alias name)
    (list 'ast/primitive-node name)
    (list 'ast/primitive-node alias name)))

(defn- rewrite-tree [code]
  (w/prewalk rewrite-node code))

(defn- fix-newlines ^String [src]
  "Hack to remove a few newlines that make indentation much uglier.
    After this, intellij can indent the code properly"
  (-> src
      (str/replace #"(:globals)\s+" "$1 ")
      (str/replace #"(:scripts)\s+" "$1 ")
      (str/replace #"(:tickingRate)\s+" "$1 ")
      (str/replace #"(:body)\s+" "$1 ")
      (str/replace #"(:name)\s+" "$1 ")
      (str/replace #"(:delay)\s+" "$1 ")
      (str/replace #"(:running\?)\s+" "$1 ")
      (str/replace #"(:once\?)\s+" "$1 ")
      (str/replace #"(:left)\s+" "$1 ")
      (str/replace #"(:right)\s+" "$1 ")
      (str/replace #"(:arguments)\s+" "$1 ")
      (str/replace #"(:value)\s+" "$1 ")
      (str/replace #"(:instructions)\s+" "$1 ")
      (str/replace #"(:primitives)\s+" "$1 ")
      (str/replace #"(:locals)\s+" "$1 ")
      (str/replace #"(:statements)\s+" "$1 ")
      (str/replace #"(:imports)\s+" "$1 ")
      (str/replace #"(:state)\s+" "$1 ")
      (str/replace #"(:tick-rate)\s+" "$1 ")))

(defn rewrite-file [input-path output-path]
  (println "BEWARE! This doesn't preserve comments and the pretty print sucks.")
  (with-open [reader (java.io.PushbackReader. (io/reader input-path))
              writer (io/writer output-path)]
    (loop []
      (when-let [next (edn/read {:eof nil} reader)]
        (.write writer (fix-newlines (with-out-str (pprint/write (rewrite-tree next)
                                                                 :dispatch pprint/code-dispatch
                                                                 :pretty true))))
        (.write writer "\r\n\r\n")
        (recur)))))
