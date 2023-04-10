(ns middleware.ast.nodes)

(defn- node [type & args]
  (let [base {:__class__ type}]
    (if args
      (apply assoc base args)
      base)))

(defn program-node
  [& {:keys [imports globals scripts primitives]
      :or {imports [] globals [] scripts [] primitives []}}]
  (node "UziProgramNode"
        :imports    imports
        :globals    globals
        :scripts    scripts
        :primitives primitives))

(defn primitive-node
  ([alias name]
   (node "UziPrimitiveDeclarationNode"
    :alias     alias
    :name      name))
  ([name] (primitive-node name name)))

(defn comment-node [text]
  (node "UziCommentNode"
   :value     text))

(defn block-node
  [statements]
  (node "UziBlockNode"
   :statements statements))

(defn import-node
  ([path] (node "UziImportNode"
           :path path))
  ([alias path]
   (assoc (import-node path)
          :alias alias))
  ([alias path block]
   (assoc (import-node alias path)
          :initializationBlock block)))

(defn task-node
  [& {:keys [name arguments tick-rate state body]
      :or   {arguments []}}]
  (node   "UziTaskNode"
   :name        name
   :arguments   arguments
   :body        body
   :state       (or state "once")
   :tickingRate tick-rate))

(defn procedure-node
  [& {:keys [name arguments body]
      :or   {arguments []}}]
  (node   "UziProcedureNode"
   :name        name
   :arguments   arguments
   :body        body))

(defn function-node
  [& {:keys [name arguments body]
      :or   {arguments []}}]
  (node   "UziFunctionNode"
   :name        name
   :arguments   arguments
   :body        body))

(defn literal-number-node
  [value]
  (node "UziNumberLiteralNode"
   :value     value))

(defn literal-pin-node
  [letter number]
  (node "UziPinLiteralNode"
   :type      letter
   :number    number))

(defn assignment-node
  [var expr]
  (node "UziAssignmentNode"
   :left var
   :right expr))

(defn arg-node
  ([value] (arg-node nil value))
  ([name value]
   (let [result (node "Association"
                      :key       name
                      :value     value)]
     (if-let [token (-> value meta :token)]
       (vary-meta result assoc :token token)
       result))))

(defn ticking-rate-node
  [times scale]
  (node "UziTickingRateNode"
   :value     times
   :scale     scale))

(defn variable-declaration-node
  ([name]
   (variable-declaration-node name (literal-number-node 0)))
  ([name expr]
   (node "UziVariableDeclarationNode"
    :name      name
    :value     expr)))

(defn variable-node
  [name]
  (node "UziVariableNode"
   :name name))

(defn return-node
  ([] (return-node nil))
  ([expr] (node "UziReturnNode"
           :value expr)))

(defn call-node
  [selector args]
  (node "UziCallNode"
   :selector  selector
   :arguments args))

(defn for-node
  [name from to by block]
  (node "UziForNode"
   :counter   (variable-declaration-node name)
   :start     from
   :stop      to
   :step      by
   :body      block))

; NOTE(Richo): Although we don't *really* need the four while until do-while do-until
; nodes (because they have the pre post and negated properties) I think it is useful
; to have them separated into different nodes anyway. Mostly because we want to generate
; code from the AST and if we don't have them separated we lose which structure was
; originally intended by the user. We could infer it of course but it is simpler this way.
(defn- conditional-loop-node
  [type & {:keys [pre condition post negated]
           :or {pre (block-node [])
                post (block-node [])
                negated false}}]
  (node type
   :pre       pre
   :condition condition
   :post      post
   :negated   negated))

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

(defn yield-node []
  (node "UziYieldNode"))

(defn forever-node
  [block] (node "UziForeverNode"
           :body      block))

(defn repeat-node
  [times block] (node "UziRepeatNode"
                 :times     times
                 :body      block))

(defn conditional-node
  ([condition true-branch]
   (conditional-node condition true-branch (block-node [])))
  ([condition true-branch false-branch]
   (node   "UziConditionalNode"
    :condition   condition
    :trueBranch  true-branch
    :falseBranch false-branch)))

(defn binary-expression-node
  [left op right]
  (node "UziCallNode"
   :selector  op
   :arguments [(arg-node left)
               (arg-node right)]))

(defn logical-and-node [left right]
  (node "UziLogicalAndNode"
   :left      left
   :right     right))

(defn logical-or-node [left right]
  (node "UziLogicalOrNode"
   :left      left
   :right     right))

(defn start-node [scripts]
  (node "UziScriptStartNode"
   :scripts   scripts))

(defn stop-node [scripts]
  (node "UziScriptStopNode"
   :scripts   scripts))

(defn pause-node [scripts]
  (node "UziScriptPauseNode"
   :scripts   scripts))

(defn resume-node [scripts]
  (node "UziScriptResumeNode"
   :scripts   scripts))

(defn string-node [value]
  (node "UziStringNode"
        :value value))