(ns middleware.compilation.codegen
  (:refer-clojure :exclude [print])
  (:require [clojure.string :as str]
            [petitparser.token :as t]
            [middleware.ast.utils :as ast-utils]
            [middleware.utils.code-writer :as cw
             :refer [append! append-line! append-indent! inc-indent!]]
            [middleware.utils.core :as u]))

(defmulti print-node :__class__)

(defn- print [writer node]
  (cw/save-interval! writer node #(print-node % writer)))

(defn- do-separated [action-fn separator-fn coll]
  "Executes action-fn for every item in coll, executing separator-fn in between
  each item. Both action-fn and separator-fn should have side effects.
  Returns nil. "
  (doseq [[i e] (map-indexed vector coll)]
    (when (pos? i) (separator-fn))
    (action-fn e)))

(defmethod print-node "UziProgramNode"
  [{:keys [imports globals primitives scripts]} writer]
  (let [print-regular (partial print writer)
        print-with-semicolon #(doto writer (print %) (cw/append! ";"))
        print-newline #(cw/append-line! writer)
        print-doubleline #(cw/append! writer "\n\n")]
    (doseq [action (interpose ::newline
                              (remove nil?
                                      [(when-not (empty? imports) ::imports)
                                       (when-not (empty? globals) ::globals)
                                       (when-not (empty? primitives) ::primitives)
                                       (when-not (empty? scripts) ::scripts)]))]
      (case action
        ::newline (print-doubleline)
        ::imports (do-separated print-regular print-newline imports)
        ::globals (do-separated print-with-semicolon print-newline globals)
        ::primitives (do-separated print-regular print-newline primitives)
        ::scripts (do-separated print-regular print-doubleline scripts)))))

(defn print-optional-block [writer block]
  (if (empty? (:statements block))
    (cw/append! writer ";")
    (doto writer
      (cw/append! " ")
      (print block))))

(defmethod print-node "UziImportNode"
  [{:keys [alias path initializationBlock]} writer]
  (doto writer
        (cw/append! (u/format "import %1 from '%2'" alias path))
        (print-optional-block initializationBlock)))

(defmethod print-node "UziVariableDeclarationNode" [{:keys [name value]} writer]
  (cw/append! writer (u/format "var %1" name))
  (when value
    (cw/append! writer " = ")
    (print writer value)))

(defmethod print-node "UziPrimitiveDeclarationNode" [{:keys [alias name]} writer]
  (cw/append! writer
              (u/format "prim %1;"
                        (if (= alias name)
                          alias
                          (u/format "%1 : %2" alias name)))))

(defmethod print-node "UziTaskNode" [{:keys [name state tickingRate body]} writer]
  (cw/append-indent! writer)
  (cw/append! writer (u/format "task %1()" name))
  (when (not= "once" state)
    (cw/append! writer (str " " state)))
  (when tickingRate
    (cw/append! writer " ")
    (print writer tickingRate))
  (cw/append! writer " ")
  (print writer body))

(defmethod print-node "UziFunctionNode" [{:keys [name arguments body]} writer]
  (doto writer
        (cw/append-indent!)
        (cw/append! (u/format "func %1(%2)"
                              name
                              (str/join ", " (map :name arguments))))
        (cw/append! " ")
        (print body)))

(defmethod print-node "UziProcedureNode" [{:keys [name arguments body]} writer]
  (doto writer
        (cw/append-indent!)
        (cw/append! (u/format "proc %1(%2)"
                              name
                              (str/join ", " (map :name arguments))))
        (cw/append! " ")
        (print body)))

(defmethod print-node "UziTickingRateNode" [node writer]
  (cw/append! writer (u/format "%1/%2" (:value node) (:scale node))))

(defn needs-semicolon? [node]
  (not (ast-utils/control-structure? node)))

(defmethod print-node "UziBlockNode" [{:keys [statements]} writer]
  (if (empty? statements)
    (cw/append! writer "{}")
    (doto writer
      (cw/append-line! "{")
      (cw/inc-indent! (fn [writer]
                        (doseq [stmt statements]
                          (cw/append-indent! writer)
                          (print writer stmt)
                          (when (needs-semicolon? stmt)
                            (cw/append! writer ";"))
                          (cw/append-line! writer))))
      (cw/append-indent!)
      (cw/append! "}"))))

(defmethod print-node "UziNumberLiteralNode" [node writer]
  (cw/append! writer (str (:value node))))

(defmethod print-node "UziPinLiteralNode" [node writer]
  (cw/append! writer (str (:type node) (:number node))))

(defmethod print-node "UziCallNode" [{:keys [selector arguments]} writer]
  (if-not (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+" selector)
    ; Regular call
    (do
      (cw/append! writer selector)
      (cw/append! writer "(")
      (doseq [arg (interpose ::separator arguments)]
        (if (= ::separator arg)
          (cw/append! writer ", ")
          (print writer arg)))
      (cw/append! writer ")"))

    (if (= 1 (count arguments))
      ; Unary
      (doto writer
        (cw/append! "(")
        (cw/append! selector)
        (print (first arguments))
        (cw/append! ")"))

      ; Binary
      (doto writer
        (cw/append! "(")
        (print (first arguments))
        (cw/append! " ")
        (cw/append! selector)
        (cw/append! " ")
        (print (second arguments))
        (cw/append! ")")))))

(defmethod print-node "Association" [{:keys [key value]} writer]
  (when key
    (cw/append! writer key)
    (cw/append! writer ": "))
  (print writer value))

(defmethod print-node "UziVariableNode" [node writer]
  (cw/append! writer (:name node)))

(defmethod print-node "UziReturnNode" [{:keys [value]} writer]
  (cw/append! writer "return")
  (when value
    (cw/append! writer " ")
    (print writer value)))

(defmethod print-node "UziYieldNode" [node writer]
  (cw/append! writer "yield"))

(defmethod print-node "UziForNode"
  [{:keys [counter start stop step body]} writer]
  (doto writer
        (cw/append! (u/format "for %1 = " (:name counter)))
        (print start)
        (cw/append! " to ")
        (print stop)
        (cw/append! " by ")
        (print step)
        (cw/append! " ")
        (print body)))

(defmethod print-node "UziWhileNode" [{:keys [condition post]} writer]
  (doto writer
        (cw/append! "while ")
        (print condition)
        (print-optional-block post)))

(defmethod print-node "UziDoWhileNode" [{:keys [pre condition]} writer]
  (doto writer
        (cw/append! "do ")
        (print pre)
        (cw/append! " while(")
        (print condition)
        (cw/append! ");")))

(defmethod print-node "UziUntilNode" [{:keys [condition post]} writer]
  (doto writer
        (cw/append! "until ")
        (print condition)
        (print-optional-block post)))

(defmethod print-node "UziDoUntilNode" [{:keys [pre condition]} writer]
  (doto writer
        (cw/append! "do ")
        (print pre)
        (cw/append! " until(")
        (print condition)
        (cw/append! ");")))

(defmethod print-node "UziForeverNode" [node writer]
  (doto writer
        (cw/append! "forever ")
        (print (:body node))))

(defmethod print-node "UziRepeatNode" [{:keys [times body]} writer]
  (doto writer
        (cw/append! "repeat ")
        (print times)
        (cw/append! " ")
        (print body)))

(defmethod print-node "UziConditionalNode"
  [{condition :condition, t-branch :trueBranch, f-branch :falseBranch} writer]
  (doto writer
        (cw/append! "if ")
        (print condition)
        (cw/append! " ")
        (print t-branch))
  (when-not (empty? (:statements f-branch))
    (cw/append! writer " else ")
    (print writer f-branch)))

(defmethod print-node "UziAssignmentNode" [{:keys [left right]} writer]
  (doto writer
        (print left)
        (cw/append! " = ")
        (print right)))

(defmethod print-node "UziScriptStartNode" [node writer]
  (cw/append! writer
              (u/format "start %1" (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptStopNode" [node writer]
  (cw/append! writer
              (u/format "stop %1" (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptPauseNode" [node writer]
  (cw/append! writer
              (u/format "pause %1" (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptResumeNode" [node writer]
  (cw/append! writer
              (u/format "resume %1" (str/join ", " (:scripts node)))))

(defmethod print-node "UziLogicalAndNode" [{:keys [left right]} writer]
  (doto writer
        (cw/append! "(")
        (print left)
        (cw/append! " && ")
        (print right)
        (cw/append! ")")))

(defmethod print-node "UziLogicalOrNode" [{:keys [left right]} writer]
  (doto writer
        (cw/append! "(")
        (print left)
        (cw/append! " || ")
        (print right)
        (cw/append! ")")))

(defmethod print-node :default [node _]
  (throw (ex-info "Unknown node" {:node node})))

(defn generate-tokens [ast]
  (let [writer (cw/make-writer)]
    (print writer ast)
    (let [src (cw/contents writer)
          intervals (cw/intervals writer)]
      (ast-utils/transform
       ast
       :default (fn [each _]
                  (if-let [[start stop] (get intervals each)]
                    (vary-meta each assoc
                               :token (t/make-token src start (- stop start) nil))
                    each))))))

(defn generate-code [node]
  (-> (generate-tokens node) meta :token :source))
