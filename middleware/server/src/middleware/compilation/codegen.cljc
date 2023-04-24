(ns middleware.compilation.codegen
  (:refer-clojure :exclude [print])
  (:require [clojure.string :as str]
            [petitparser.token :as t]
            [middleware.compilation.parser :as parser]
            [middleware.ast.utils :as ast-utils]
            [middleware.utils.code-writer :as cw]))

(defmulti print-node :__class__)

(defn- print-metadata
  "For debugging purposes, you can attach a metadata string under the key
   ::metadata that will get printed as an uzi comment right before the node"
  [node writer]
  (when-let [metadata (-> node meta ::metadata)]
    (cw/append! writer "\"" metadata "\" ")))

(defn- print* [writer node]
  (print-metadata node writer)
  (print-node node writer))

(defn- print [writer node]
  (cw/save-interval! writer node #(print* writer %)))

(defn- print-stmt [writer node]
  (let [needs-semicolon? (not (ast-utils/control-structure? node))]
    (cw/save-interval!
     writer node
     (fn [node]
       (print* writer node)
       (when needs-semicolon?
         (cw/append! writer ";"))))))

(defn- print-seq [writer coll & {:keys [followed-by separated-by]}]
  "Iterates over coll executing the print function for each item.
  If followed-by is set to some value, it appends it *after* each item.
  If separated-by is set to some value, it appends it *between* each item."
  (if-let [sep separated-by]
    (doseq [[i e] (map-indexed vector coll)]
      (when (pos? i)
        (cw/append! writer sep))
      (print writer e)
      (when followed-by
        (cw/append! writer followed-by)))
    (doseq [e coll]
      (print writer e)
      (when followed-by
        (cw/append! writer followed-by)))))

(defmethod print-node "UziProgramNode"
  [{:keys [imports globals primitives scripts]} writer]
  (doseq [action (interpose ::newline
                            (remove nil?
                                    [(when-not (empty? imports) ::imports)
                                     (when-not (empty? globals) ::globals)
                                     (when-not (empty? primitives) ::primitives)
                                     (when-not (empty? scripts) ::scripts)]))]
    (case action
      ::newline (cw/append! writer "\n\n")
      ::imports (print-seq writer imports :separated-by "\n")
      ::globals (print-seq writer globals :followed-by ";" :separated-by "\n")
      ::primitives (print-seq writer primitives :followed-by ";" :separated-by "\n")
      ::scripts (print-seq writer scripts :separated-by "\n\n"))))

(defn print-optional-block [writer block]
  (if (empty? (:statements block))
    (cw/append! writer ";")
    (doto writer
          (cw/append! " ")
          (print block))))

(defmethod print-node "UziImportNode"
  [{:keys [alias path initializationBlock]} writer]
  (if alias
    (doto writer
        (cw/append! "import " alias " from '" path "'")
        (print-optional-block initializationBlock))
    (doto writer
      (cw/append! "import '" path "'")
      (print-optional-block initializationBlock))))

(defmethod print-node "UziVariableDeclarationNode" [{:keys [name value] :as node} writer]
  (if (node ::exclude-var-declaration?)
    (cw/append! writer name)
    (do (cw/append! writer "var " name)
        (when value
          (cw/append! writer " = ")
          (print writer value)))))

(defmethod print-node "UziPrimitiveDeclarationNode" [{:keys [alias name]} writer]
  (cw/append! writer "prim " (if (= alias name)
                               alias
                               (str alias " : " name))))

(defmethod print-node "UziTaskNode" [{:keys [name state tickingRate body]} writer]
  (cw/append-indent! writer)
  (cw/append! writer "task " name "()")
  (when (not= "once" state)
    (cw/append! writer " " state))
  (when tickingRate
    (cw/append! writer " ")
    (print writer tickingRate))
  (cw/append! writer " ")
  (print writer body))

(defmethod print-node "UziFunctionNode" [{:keys [name arguments body]} writer]
  (doto writer
    (cw/append-indent!)
    (cw/append! "func " name "(")
    (print-seq (map #(assoc % ::exclude-var-declaration? true) 
                    arguments) 
               :separated-by ", ")
    (cw/append! ") ")
    (print body)))

(defmethod print-node "UziProcedureNode" [{:keys [name arguments body]} writer]
  (doto writer
    (cw/append-indent!)
    (cw/append! "proc " name "(")
    (print-seq (map #(assoc % ::exclude-var-declaration? true)
                    arguments)
               :separated-by ", ")
    (cw/append! ") ")
    (print body)))

(defmethod print-node "UziTickingRateNode" [node writer]
  (cw/append! writer (:value node) "/" (:scale node)))

(defmethod print-node "UziBlockNode" [{:keys [statements]} writer]
  (if (empty? statements)
    (cw/append! writer "{}")
    (doto writer
          (cw/append-line! "{")
          (cw/inc-indent! (fn [writer]
                            (doseq [stmt statements]
                              (cw/append-indent! writer)
                              (print-stmt writer stmt)
                              (cw/append-line! writer))))
          (cw/append-indent!)
          (cw/append! "}"))))

(defmethod print-node "UziNumberLiteralNode" [node writer]
  (cw/append! writer (str (:value node))))

(defmethod print-node "UziPinLiteralNode" [node writer]
  (cw/append! writer (str (:type node) (:number node))))

(defn binary? [selector]
  "Returns true if the selector is a binary operator.
  All the characters in selector *must* be binary"
  (every? parser/binary? selector))

(defmethod print-node "UziCallNode" [{:keys [selector arguments]} writer]
  (if-not (binary? selector)
    ; Regular call
    (doto writer
          (cw/append! selector "(")
          (print-seq arguments :separated-by ", ")
          (cw/append! ")"))

    (if (= 1 (count arguments))
      ; Unary
      (doto writer
            (cw/append! "(" selector)
            (print (first arguments))
            (cw/append! ")"))

      ; Binary
      (doto writer
            (cw/append! "(")
            (print (first arguments))
            (cw/append! " " selector " ")
            (print (second arguments))
            (cw/append! ")")))))

(defmethod print-node "Association" [{:keys [key value]} writer]
  (when key
    (cw/append! writer key ": "))
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
    (cw/append! "for ")
    (print (assoc counter ::exclude-var-declaration? true))
    (cw/append! " = ")
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
  (cw/append! writer "start " (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptStopNode" [node writer]
  (cw/append! writer "stop " (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptPauseNode" [node writer]
  (cw/append! writer "pause " (str/join ", " (:scripts node))))

(defmethod print-node "UziScriptResumeNode" [node writer]
  (cw/append! writer "resume " (str/join ", " (:scripts node))))

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

(defmethod print-node "UziStringNode" [{:keys [value]} writer]
  (doto writer
    (cw/append! "'")
    (cw/append! value)
    (cw/append! "'")))

(defmethod print-node :default [node _]
  (throw (ex-info "Unknown node" {:node node})))

(defn generate-tokens [ast]
  (let [writer (cw/make-writer)]
    (print writer ast)
    (let [src (cw/contents writer)
          intervals (cw/intervals writer)]
      (ast-utils/transform
       ast
       (fn [each]
         (if-let [[start stop] (get intervals each)]
           (let [token (t/make-token src start (- stop start) nil)]
             (vary-meta each assoc :token token))
           each))))))

(defn generate-code [node]
  (-> (generate-tokens node) meta :token :source))