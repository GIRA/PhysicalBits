(ns plugin.parser.ast-nodes)
(defn- conj-if-not-nil
  [map key value]
  (conj map
        (when value [key value])))
(defn program-node
  [imports globals scripts primitives]
  {:__class__  "UziProgramNode"
   :imports    imports,
   :globals    globals
   :scripts    scripts
   :primitives primitives})
(defn import-node
  [alias path block]
  {:__class__           "UziImportNode",
   :alias               alias,
   :path                path,
   :initializationBlock block})
(defn script-node
  [type & {:keys [identifier arguments tick-rate state locals body]
           :or   {arguments []
                  tick-rate nil
                  state     nil
                  body      nil}}]
  (conj-if-not-nil
    {:__class__   type,
     :name        identifier,
     :arguments   arguments,
     :tickingRate tick-rate
     :body        body}
    :state state))
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
(defn association-node
  [key value]
  {:__class__ "Association",
   :key       key
   :value     value})
(defn ticking-rate-node
  [times scale]
  {:__class__ "UziTickingRateNode",
   :value     times,
   :scale     scale})
(defn variable-declaration-node
  ([name] (variable-declaration-node name nil))
  ([name expr]
   (conj-if-not-nil
     {:__class__ "UziVariableDeclarationNode"
      :name      name}
     :value expr)))
(defn variable-node
  [name]
  {:__class__ "UziVariableNode", :name name})
(defn return-node
  [expr]
  {:__class__ "UziReturnNode", :value expr})
(defn call-node
  [selector args]
  {:__class__ "UziCallNode",
   :selector  selector
   :arguments args})
(defn for-node
  [name from to by block]
  {:__class__ "UziForNode",
   :counter   (variable-declaration-node name),
   :start     from,
   :stop      to,
   :step      by,
   :body      block})
(defn while-node
  [pre condition post negated]
  {:__class__ "UziWhileNode",
   :pre       pre,
   :condition condition,
   :post      post,
   :negated   negated})
;TODO(Tera): The Do While Node is not really necessary, since the whileNode has the whole pre & post thing is enough to represent this idea
(defn do-while-node
  [pre condition post negated]
  {:__class__ "UziDoWhileNode",
   :pre       pre,
   :condition condition,
   :post      post,
   :negated   negated})

;TODO(Tera): The Do Until Node is not really necessary, since the whileNode has the whole pre & post thing is enough to represent this idea
(defn do-until-node
  [pre condition post negated]
  {:__class__ "UziDoUntilNode",
   :pre       pre,
   :condition condition,
   :post      post,
   :negated   negated})
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
  [condition true-branch false-branch]
  {:__class__   "UziConditionalNode"
   :condition   condition
   :trueBranch  true-branch
   :falseBranch false-branch})
(defn block-node
  [statements]
  {:__class__ "UziBlockNode" :statements statements})
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
(defn start-node [scripts] {:__class__ "UziScriptStartNode",
                          :scripts scripts})
(defn stop-node [scripts] {:__class__ "UziScriptStopNode",
                          :scripts scripts})
(defn pause-node [scripts] {:__class__ "UziScriptPauseNode",
                          :scripts scripts})
(defn resume-node [scripts] {:__class__ "UziScriptResumeNode",
                          :scripts scripts})