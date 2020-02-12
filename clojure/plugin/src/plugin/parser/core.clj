(ns plugin.parser.core
  (:require [instaparse.core :as insta]))


(defn- first-or-default [pred col default]
  (or (first (filter pred col)) default) )
(defn- first-class-or-default [name col default] (first-or-default #(= (:__class__ %) name) col default))

(defn script
  [type & {:keys [identifier arguments tick-rate state locals body]
      :or {arguments []
           tick-rate nil
           state nil
           body nil}}]
  {:__class__ type,
   :name identifier,
   :arguments arguments,
   :state state,
   :tickingRate tick-rate,
   :body body})



(def scriptTypes #{"UziTaskNode" "UziProcedureNode" "UziFunctionNode"} )
(def transformations
  {:integer (comp clojure.edn/read-string str)
   :float (comp #(Float/parseFloat %) str)
   :program  (fn [& arg] {:__class__ "UziProgramNode"
                          :imports [],
                          :globals [],
                          :scripts (filterv #(contains? scriptTypes (:__class__ %)) arg),
                          :primitives []}),
   :task (fn [identifier params state & rest]
           (script "UziTaskNode"
                   :identifier identifier
                   :arguments params
                   :state (second state)
                   :tick-rate (first-class-or-default "UziTickingRateNode" rest nil)
                   :body (first-class-or-default "UziBlockNode" rest nil)))
   :procedure (fn [identifier params  & rest]
                (script "UziProcedureNode"
                        :identifier identifier
                        :arguments params
                        :body (first-class-or-default "UziBlockNode" rest nil)))
   :function (fn [identifier params  & rest]
                (script "UziFunctionNode"
                        :identifier identifier
                        :arguments params
                        :body (first-class-or-default "UziBlockNode" rest nil)))

   :tickingRate (fn [times unit] {:__class__ "UziTickingRateNode",
                                  :value (:value times),
                                  :scale unit}),
   :namedArg (fn [a & b] {:__class__ "Association",
                          :key (if b a nil),
                          :value (or b a)
                          })
   :constant (fn [letter number] {:__class__ "UziPinLiteralNode",
                                  :type letter,
                                  :number number})
   :number (fn [number] {:__class__ "UziNumberLiteralNode",
                                :value number})
   :call (fn [selector & args] {:__class__ "UziCallNode",
                                :selector selector
                                :arguments args})
   :block (fn [& statements] {:__class__ "UziBlockNode" :statements (vec statements)})
   :paramsList (fn [& params] (or  (vec params) []))
   :argument (fn [name] {:__class__ "UziVariableDeclarationNode",
                         :name name})
   :variable (fn [name] {:__class__ "UziVariableNode",
                         :name name})
   :return (fn [& expr] {:__class__ "UziReturnNode",
                       :value (or (first expr) {:__class__ "UziNumberLiteralNode",
                                                :value 0})})
   :subExpr (fn [rest] rest)
   :binaryExpr (fn [left [_ op] right] {:__class__ "UziCallNode",
                                       :selector op,
                                       :arguments [left right]})
   })

(def parse-program
      (insta/parser
        "program = ws import* ws variableDeclaration* ws (primitive / script) * ws
         import = ws <'import'> ws (identifier ws <'from'> ws)? importPath ws (endl / block)
         importPath = #'\\'[^\\']+\\''
         <script> =  (task / function / procedure)
         block = ws <'{'> ws statementList ws <'}'> ws

         primitive = ws <'prim'> ws ((binarySelector / identifier) ws <':'> ws)? identifier endl

         <statementList> = ws statement*
         <statement> = ws (variableDeclaration | assignment | return | conditional
                      | startTask | stopTask | pauseTask | resumeTask
                      |while|doWhile|until|doUntil|repeat|forever|for|yield|expressionStatement) ws

         variableDeclaration = <'var'> ws variable (ws <'='> ws expr)?  endl
         assignment = ws variable ws <'='> ws expr  endl
         return =  ws <'return'> ws expr? endl
         conditional = ws <'if'> ws expr ws block (ws <'else'> ws expr)
         while = ws <'while'> ws expr ws ( block ws / endl )
         doWhile = ws <'do'> ws block ws <'while'> ws expr endl
         until = ws <'until'> ws expr ws block
         doUntil = ws <'do'> ws block ws <'until'> ws expr endl
         repeat = ws <'repeat'> ws expr ws block ws
         forever = ws <'forever'> ws block ws
         for = ws <'for'> ws variable ws <'='> ws expr ws <'to'> expr ws (<'by'> ws expr ws)? block ws
         yield = ws <'yield'> endl
         <expressionStatement> = expr endl

         startTask = ws <'start'> ws scriptList endl
         stopTask = ws <'stop'> ws scriptList endl
         pauseTask = ws <'pause'> ws scriptList endl
         resumeTask = ws <'resume'> ws scriptList endl



         scriptList = ws scriptReference (ws <','> ws scriptReference)* endl
         <scriptReference> = ws identifier ws

         <identifier> = name ('.' name)*
         variable = ws identifier ws

         task=<'task'> ws identifier paramsList taskState tickingRate? block ws

         paramsList = ws<'('> (ws argument (ws <','> ws argument)*)? ws <')'> ws
         argument = ws identifier ws
         taskState = ws ('running'|'stopped') ws
         tickingRate = number ws <'/'> ( 's' | 'm' | 'h' | 'd')

         function = ws <'func'> ws identifier ws paramsList ws block ws
         procedure = ws <'proc'> ws identifier ws paramsList ws block ws

         comments = (<'\"'> #'[^\"]*' <'\"'>)






         <expr> =(binaryExpr / nonBinaryExpr)
         <nonBinaryExpr> = (unary / literal / call / variable / subExpr)
         <unary> = not
         not = ws <'!'> ws nonBinaryExpr ws
         <literal> = (constant / number)
         constant = ws ('D'/'A') integer ws
         call = ws scriptReference argList ws
         <argList> = ws <'('> ws (namedArg (ws <','> ws namedArg)?)?<')'>
         namedArg = ws( identifier ws <':'> ws)? expr ws
         subExpr = ws <'('> ws expr ws <')'> ws

         binaryExpr = ws nonBinaryExpr ws (binarySelector ws nonBinaryExpr)+ ws
         binarySelector = #'[^a-zA-Z0-9\\s\\[\\]\\(\\)\\{\\}\\\"\\':#_;,]'

         <endl> =ws <';'> ws
         <name> =#'[a-zA-Z_][_\\w]*'
         <ws> = (<#'\\s'*> / comments)+
         <letter> = #'[a-zA-Z]'
         <word> = #'\\w'
         <digits> = #'\\d+'
         integer = '-'? digits
         float = ('NaN' /#'-?Infinity' / integer '.' digits)
         number = (float / integer)"))

(defn parse [str] (insta/transform transformations (parse-program str )))

