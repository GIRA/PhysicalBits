(ns middleware.parser.ast-nodes)

(defn program-node
  [& {:keys [imports globals scripts primitives]}]
  {:__class__  "UziProgramNode"
   :imports    imports,
   :globals    globals
   :scripts    scripts
   :primitives primitives})

(defn primitive-node
  ([alias name]
   {:__class__ "UziPrimitiveDeclarationNode",
    :alias     alias,
    :name      name})
  ([name] (primitive-node name name)))

(defn comment-node [text]
  {:__class__ "UziCommentNode"
   :value     text})

(defn block-node
  [statements]
  {:__class__ "UziBlockNode" :statements statements})

(defn import-node
  ([path] (import-node nil path))
  ([alias path] (import-node alias path nil))
  ([alias path block]
   {:__class__           "UziImportNode",
    :alias               alias,
    :path                path,
    :initializationBlock block}))

(defn task-node
  [& {:keys [identifier arguments tick-rate state locals body]
      :or   {arguments []}}]
  {:__class__   "UziTaskNode",
   :name        identifier,
   :arguments   arguments,
   :body        body,
   :state       state,
   :tickingRate tick-rate})

(defn procedure-node
  [& {:keys [identifier arguments locals body]
      :or   {arguments []}}]
  {:__class__   "UziProcedureNode",
   :name        identifier,
   :arguments   arguments,
   :body        body})

(defn function-node
  [& {:keys [identifier arguments locals body]
      :or   {arguments []}}]
  {:__class__   "UziFunctionNode",
   :name        identifier,
   :arguments   arguments,
   :body        body})

(defn literal-number-node
  [value]
  {:__class__ "UziNumberLiteralNode",
   :value     value})

(defn literal-pin-node
  [letter number]
  {:__class__ "UziPinLiteralNode",
   :type      letter,
   :number    number})

(defn assignment-node
  [var expr]
  {:__class__ "UziAssignmentNode" :left var :right expr})

(defn arg-node
  ([value] (arg-node nil value))
  ([name value]
   {:__class__ "Association",
    :key       name
    :value     value}))

(defn ticking-rate-node
  [times scale]
  {:__class__ "UziTickingRateNode",
   :value     times,
   :scale     scale})

(defn variable-declaration-node
  ([name]
   (variable-declaration-node name (literal-number-node 0)))
  ([name expr]
   {:__class__ "UziVariableDeclarationNode"
    :name      name
    :value     expr}))

(defn variable-node
  [name]
  {:__class__ "UziVariableNode", :name name})

(defn return-node
  ([] (return-node nil))
  ([expr] {:__class__ "UziReturnNode", :value expr}))

(defn call-node
  [selector args]
  {:__class__ "UziCallNode",
   :selector  selector
   :arguments args})

(defn for-node
  [name from to by block]
  {:__class__ "UziForNode",
   :counter   (variable-declaration-node name (literal-number-node 0)),
   :start     from,
   :stop      to,
   :step      by,
   :body      block})

; NOTE(Richo): Although we don't *really* need the four while, until, do-while, do-until
; nodes (because they have the pre, post, and negated properties) I think it is useful
; to have them separated into different nodes anyway. Mostly because we want to generate
; code from the AST and if we don't have them separated we lose which structure was
; originally intended by the user. We could infer it, of course, but it is simpler this way.
(defn- conditional-loop-node
  [type & {:keys [pre condition post negated]
           :or {pre (block-node [])
                post (block-node [])
                negated false}}]
  {:__class__ type,
   :pre       pre,
   :condition condition,
   :post      post,
   :negated   negated})

(defn while-node [condition body]
  (conditional-loop-node
    "UziWhileNode"
    :condition condition
    :post body))

(defn until-node [condition body]
  (conditional-loop-node
    "UziUntilNode"
    :condition condition
    :post body
    :negated true))

(defn do-while-node [condition body]
  (conditional-loop-node
    "UziDoWhileNode"
    :pre body
    :condition condition))

(defn do-until-node [condition body]
  (conditional-loop-node
    "UziDoUntilNode"
    :pre body
    :condition condition
    :negated true))

(defn yield-node
  [] {:__class__ "UziYieldNode"})

(defn forever-node
  [block] {:__class__ "UziForeverNode",
           :body      block})

(defn repeat-node
  [times block] {:__class__ "UziRepeatNode"
                 :times     times
                 :body      block})

(defn conditional-node
  ([condition true-branch]
   (conditional-node condition true-branch (block-node [])))
  ([condition true-branch false-branch]
   {:__class__   "UziConditionalNode"
    :condition   condition
    :trueBranch  true-branch
    :falseBranch false-branch}))

(defn binary-expression-node
  [left op right]
  {:__class__ "UziCallNode",
   :selector  op,
   ;INFO(Tera): i had to add these associations since the binary expression get translated into a call
   :arguments [{:__class__ "Association",
                :key       nil,
                :value     left}
               {:__class__ "Association",
                :key       nil,
                :value     right}]})

(defn logical-and-node [left right] {:__class__ "UziLogicalAndNode",
                                     :left      left,
                                     :right     right})

(defn logical-or-node [left right] {:__class__ "UziLogicalOrNode",
                                    :left      left,
                                    :right     right})

(defn start-node [scripts] {:__class__ "UziScriptStartNode",
                            :scripts   scripts})

(defn stop-node [scripts] {:__class__ "UziScriptStopNode",
                           :scripts   scripts})

(defn pause-node [scripts] {:__class__ "UziScriptPauseNode",
                            :scripts   scripts})

(defn resume-node [scripts] {:__class__ "UziScriptResumeNode",
                             :scripts   scripts})
