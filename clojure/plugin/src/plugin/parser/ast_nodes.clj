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
(defn script-node
  [type & {:keys [identifier arguments tick-rate state locals body]
           :or   {arguments []
                  tick-rate nil
                  state     nil
                  body      nil}}]
  {:__class__   type,
   :name        identifier,
   :arguments   arguments,
   :state       state,
   :tickingRate tick-rate,
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
  [selector & args]
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
(defn block-node
  [statements]
  {:__class__ "UziBlockNode" :statements statements})
(defn binary-expression-node
  [left op right]
  {:__class__ "UziCallNode",
   :selector  op,
   :arguments [{:__class__ "Association",
                :key       nil,
                :value     left}
               {:__class__ "Association",
                :key       nil,
                :value     right}]})
