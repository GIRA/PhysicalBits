(ns middleware.compilation.codegen
  (:refer-clojure :exclude [print])
  (:require [clojure.string :as str]
            [petitparser.token :as t]
            [middleware.ast.utils :as ast-utils]
            [middleware.utils.string-writer :as sw
             :refer [append! append-line! append-indent! inc-indent!]]
            [middleware.utils.core :as u]))

(def ^:dynamic *intervals*)

(defmulti print-node :__class__)

(defn- print [node]
  (let [start (sw/position sw/*writer*)]
    (print-node node)
    (vswap! *intervals*
            assoc node [start (sw/position sw/*writer*)])))

(defn- print-with-semicolon [node]
  (print node)
  (append! ";"))

(defn- do-separated [action-fn separator-fn coll]
  "Executes action-fn for every item in coll, executing separator-fn in between
  each item. Both action-fn and separator-fn should have side effects.
  Returns nil. "
  (doseq [[i e] (map-indexed vector coll)]
    (when (pos? i) (separator-fn))
    (action-fn e)))

(defmethod print-node "UziProgramNode"
  [{:keys [imports globals primitives scripts]}]
  (doseq [action (interpose ::newline
                            (remove nil?
                                    [(when-not (empty? imports) ::imports)
                                     (when-not (empty? globals) ::globals)
                                     (when-not (empty? primitives) ::primitives)
                                     (when-not (empty? scripts) ::scripts)]))]
    (case action
      ::newline (append! "\n\n")
      ::imports (do-separated print append-line! imports)
      ::globals (do-separated print-with-semicolon append-line! globals)
      ::primitives (do-separated print append-line! primitives)
      ::scripts (do-separated print (partial append! "\n\n") scripts))))

(defn print-optional-block [block]
  (if (empty? (:statements block))
    (append! ";")
    (do
      (append! " ")
      (print block))))

(defmethod print-node "UziImportNode" [node]
  (append! (u/format "import %1 from '%2'"
                     (:alias node)
                     (:path node)))
  (print-optional-block (:initializationBlock node)))

(defmethod print-node "UziVariableDeclarationNode" [{:keys [name value]}]
  (append! (u/format "var %1" name))
  (when value
    (do
      (append! " = ")
      (print value))))

(defmethod print-node "UziPrimitiveDeclarationNode" [{:keys [alias name]}]
  (append! (u/format "prim %1;"
                     (if (= alias name)
                       alias
                       (u/format "%1 : %2" alias name)))))

(defmethod print-node "UziTaskNode" [{:keys [name state tickingRate body]}]
  (append-indent!)
  (append! (u/format "task %1()" name))
  (when (not= "once" state)
    (append! (str " " state)))
  (when tickingRate
    (append! " ")
    (print tickingRate))
  (append! " ")
  (print body))

(defmethod print-node "UziFunctionNode" [{:keys [name arguments body]}]
  (append-indent!)
  (append! (u/format "func %1(%2)" name (str/join ", " (map :name arguments))))
  (append! " ")
  (print body))

(defmethod print-node "UziProcedureNode" [{:keys [name arguments body]}]
  (append-indent!)
  (append! (u/format "proc %1(%2)" name (str/join ", " (map :name arguments))))
  (append! " ")
  (print body))

(defmethod print-node "UziTickingRateNode" [node]
  (append! (u/format "%1/%2" (:value node) (:scale node))))

(defn needs-semicolon? [node]
  (not (ast-utils/control-structure? node)))

(defmethod print-node "UziBlockNode" [{:keys [statements]}]
  (if (empty? statements)
    (append! "{}")
    (do
      (append-line! "{")
      (inc-indent! #(doseq [stmt statements]
                      (append-indent!)
                      (print stmt)
                      (when (needs-semicolon? stmt)
                        (append! ";"))
                      (append-line!)))
      (append-indent!)
      (append! "}"))))

(defmethod print-node "UziNumberLiteralNode" [node]
  (append! (str (:value node))))

(defmethod print-node "UziPinLiteralNode" [node]
  (append! (str (:type node) (:number node))))

(defmethod print-node "UziCallNode" [{:keys [selector arguments]}]
  (if-not (re-matches #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]+" selector)
    ; Regular call
    (do
      (append! selector)
      (append! "(")
      (doseq [arg (interpose ::separator arguments)]
        (if (= ::separator arg)
          (append! ", ")
          (print arg)))
      (append! ")"))

    (if (= 1 (count arguments))
      ; Unary
      (do
        (append! "(")
        (append! selector)
        (print (first arguments))
        (append! ")"))

      ; Binary
      (do
        (append! "(")
        (print (first arguments))
        (append! " ")
        (append! selector)
        (append! " ")
        (print (second arguments))
        (append! ")")))))

(defmethod print-node "Association" [{:keys [key value]}]
  (when key
    (append! key)
    (append! ": "))
  (print value))

(defmethod print-node "UziVariableNode" [node]
  (append! (:name node)))

(defmethod print-node "UziReturnNode" [{:keys [value]}]
  (append! "return")
  (when value
    (append! " ")
    (print value)))

(defmethod print-node "UziYieldNode" [node]
  (append! "yield"))

(defmethod print-node "UziForNode"
  [{:keys [counter start stop step body]}]
  (append! (u/format "for %1 = " (:name counter)))
  (print start)
  (append! " to ")
  (print stop)
  (append! " by ")
  (print step)
  (append! " ")
  (print body))

(defmethod print-node "UziWhileNode" [{:keys [condition post]}]
  (append! "while ")
  (print condition)
  (print-optional-block post))

(defmethod print-node "UziDoWhileNode" [{:keys [pre condition]}]
  (append! "do ")
  (print pre)
  (append! " while(")
  (print condition)
  (append! ");"))

(defmethod print-node "UziUntilNode" [{:keys [condition post]}]
  (append! "until ")
  (print condition)
  (print-optional-block post))

(defmethod print-node "UziDoUntilNode" [{:keys [pre condition]}]
  (append! "do ")
  (print pre)
  (append! " until(")
  (print condition)
  (append! ");"))

(defmethod print-node "UziForeverNode" [node]
  (append! "forever ")
  (print (:body node)))

(defmethod print-node "UziRepeatNode" [{:keys [times body]}]
  (append! "repeat ")
  (print times)
  (append! " ")
  (print body))

(defmethod print-node "UziConditionalNode"
  [{:keys [condition trueBranch falseBranch]}]
  (append! "if ")
  (print condition)
  (append! " ")
  (print trueBranch)
  (when-not (empty? (:statements falseBranch))
    (append! " else ")
    (print falseBranch)))

(defmethod print-node "UziAssignmentNode" [{:keys [left right]}]
  (print left)
  (append! " = ")
  (print right))

(defmethod print-node "UziScriptStartNode" [node]
  (append! (u/format "start %1"
                     (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptStopNode" [node]
  (append! (u/format "stop %1"
                     (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptPauseNode" [node]
  (append! (u/format "pause %1"
                     (str/join ", " (:scripts node)))))

(defmethod print-node "UziScriptResumeNode" [node]
  (append! (u/format "resume %1"
                     (str/join ", " (:scripts node)))))

(defmethod print-node "UziLogicalAndNode" [{:keys [left right]}]
  (append! "(")
  (print left)
  (append! " && ")
  (print right)
  (append! ")"))

(defmethod print-node "UziLogicalOrNode" [{:keys [left right]}]
  (append! "(")
  (print left)
  (append! " || ")
  (print right)
  (append! ")"))

(defmethod print-node :default [node]
  (throw (ex-info "Not Implemented node reached: " {:node node})))

(defn generate-tokens [ast]
  (binding [sw/*writer* (sw/make-writer)
            *intervals* (volatile! {})]
    (print ast)
    (let [src (sw/contents)
          intervals @*intervals*]
      (ast-utils/transform
       ast
       :default (fn [each _]
                  (if-let [[start stop] (get intervals each)]
                    (vary-meta each assoc
                               :token (t/make-token src start (- stop start) nil))
                    each))))))

(defn generate-code [node]
  (-> (generate-tokens node) meta :token :source))
