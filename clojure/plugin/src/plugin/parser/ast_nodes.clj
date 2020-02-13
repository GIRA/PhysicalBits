(ns plugin.parser.ast-nodes)

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

(defn- for-node
  [var from to by block]
  {:__class__ "UziForNode",
   :counter   {:__class__ "UziVariableDeclarationNode"
               :name      (:name var)},
   :start     from,
   :stop      to,
   :step      by,
   :body      block})
(defn- literal-number-node
  [value]
  {:__class__ "UziNumberLiteralNode",
   :value     value})