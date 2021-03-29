(ns middleware.parser.old-parser
  (:require [instaparse.core :as insta]
            [instaparse.failure :as fail])
  (:require [middleware.parser.ast-nodes :refer :all])
  (:require [middleware.parser.binary-operator :refer :all]))


(defn- first-or-default [pred col default]
  (or (first (filter pred col)) default))
(defn- first-class-or-default [name col default] (first-or-default #(= (:__class__ %) name) col default))
(defn- filter-class [name col] (filterv #(= (:__class__ %) name) col))

(defn- parse-int [str] (Integer/parseInt str))
(defn- parse-double [str] (Double/parseDouble str))

(def scriptTypes #{"UziTaskNode" "UziProcedureNode" "UziFunctionNode"})
(def transformations
  {:integer             (comp parse-int str)
   :float               (comp parse-double str)
   :identifier          str
   :program             (fn [& arg]
                          (program-node
                           :imports (filter-class "UziImportNode" arg)
                           :globals (filter-class "UziVariableDeclarationNode" arg)
                           :scripts (filterv #(contains? scriptTypes (:__class__ %)) arg)
                           :primitives (filter-class "UziPrimitiveDeclarationNode" arg))),
   :primitive           primitive-node
   :task                (fn [name params state & rest]
                          (task-node :name name
                                     :arguments params
                                     :state (or (second state) "once")
                                     :tick-rate (first-class-or-default "UziTickingRateNode" rest nil)
                                     :body (first-class-or-default "UziBlockNode" rest nil)))
   :procedure           (fn [name params & rest]
                          (procedure-node :name name
                                          :arguments params
                                          :body (first-class-or-default "UziBlockNode" rest nil)))
   :function            (fn [name params & rest]
                          (function-node :name name
                                         :arguments params
                                         :body (first-class-or-default "UziBlockNode" rest nil)))
   ;TODO(Tera): the comments are ignored by the syntax definition for now.
   :comments            (fn [strings] (comment-node strings))
   :tickingRate         (fn [times unit] (ticking-rate-node (:value times) unit)),
   :namedArg            (fn [a & b] (arg-node (if b a nil) (or (first b) a)))
   :constant            literal-pin-node
   :number              (fn [number] (literal-number-node number))
   :call                (fn [selector & args] (call-node selector (vec args)))
   :block               (fn [& statements] (block-node (vec statements)))
   :paramsList          (fn [& params] (or (vec params) []))
   :argument            (fn [name] (variable-declaration-node name (literal-number-node 0)))
   :variableDeclaration (fn [variable & expr]
                          (variable-declaration-node (:name variable) (or (first expr) (literal-number-node 0))))
   :variable            variable-node
   :return              return-node ;(fn [& expr] (return-node (first expr)))
   :subExpr             (fn [rest] rest)
   :for                 (fn
                          ([var from to block] (for-node (:name var) from to (literal-number-node 1) block))
                          ([var from to by block] (for-node (:name var) from to by block)))
   :while               (fn [expr & block] (while-node expr (or (first block) (block-node []))))
   :until               (fn [expr & block] (until-node expr (or (first block) (block-node []))))
   :doWhile             (fn [block expr] (do-while-node expr block))
   :doUntil             (fn [block expr] (do-until-node expr block))
   :forever             forever-node
   :repeat              repeat-node
   :conditional         conditional-node
   :assignment          assignment-node
   :binaryExpr          (fn [left op right]
                          (case op
                            "&&" (logical-and-node left right)
                            "||" (logical-or-node left right)
                            (binary-expression-node left op right)))
   :not                 (fn [& arg] (call-node "!" (vec (map #(arg-node %) arg))))
   :yield               yield-node
   :import              (fn [& args]
                          (let [path (first (filter #(= (first %) :importPath) args))
                                block (first-class-or-default "UziBlockNode" args (block-node []))
                                ;TODO(Tera): there is probably a better way of doing this.
                                name (first (filter
                                              #(and (not= (:__class__ %) "UziBlockNode")
                                                    (not= (first %) :importPath))
                                              args))
                                ]
                            (import-node name (second path) block)))
   :startTask           (fn [& scripts] (start-node (vec scripts)))
   :stopTask            (fn [& scripts] (stop-node (vec scripts)))
   :pauseTask           (fn [& scripts] (pause-node (vec scripts)))
   :resumeTask          (fn [& scripts] (resume-node (vec scripts)))
   })

(def parse-program
  (insta/parser

    "program = ws? import*  ((variableDeclaration | primitive | script) ws?)* ws?
     import = <'import'> ws (identifier ws <'from'> ws)? importPath (endl | ws? block ) ws?
     importPath = <'\\''> #'[^\\']+' <'\\''>
     <script> = (task | function | procedure)
     block = <'{'>  statementList  <'}'>

     primitive = <'prim'> ws ((binarySelector | identifier) ws <':'> ws)? identifier endl

     <statementList> = ws? statement*
     <statement> =  (variableDeclaration / assignment / return / conditional
                  / startTask / stopTask / pauseTask / resumeTask
                  / while / doWhile / until / doUntil / repeat / forever / for
                  / yield / expressionStatement) ws?

     variableDeclaration = <'var'> ws variable (ws? <'='> ws? expr ws?)? endl ws?
     assignment = variable ws? <'='> ws? expr endl
     return = <'return'> separatedExpr? endl
     conditional = <'if'> separatedExpr ws? block (ws? <'else'> ws? block)?
     while = <'while'> separatedExpr ws? ( block ws? | endl )
     doWhile = <'do'> ws? block ws? <'while'> separatedExpr endl
     until = <'until'> separatedExpr ws? (block ws? | endl )
     doUntil = <'do'> ws? block ws? <'until'> separatedExpr endl
     repeat = <'repeat'> separatedExpr ws? block
     forever = <'forever'> ws? block
     for = <'for'> ws variable ws? <'='> ws? expr ws <'to'> separatedExpr ws? (ws <'by'> separatedExpr ws?)? block
     yield = <'yield'> endl
     <expressionStatement> = expr endl

     startTask = <'start'> ws scriptList endl
     stopTask = <'stop'> ws scriptList endl
     pauseTask = <'pause'> ws scriptList endl
     resumeTask = <'resume'> ws scriptList endl



     <scriptList> = scriptReference (ws? <','> ws? scriptReference)*
     <scriptReference> = identifier

     identifier = #'[a-zA-Z_][_\\w]*(\\.[a-zA-Z_][_\\w]*)*'
     variable = identifier

     task=<'task'> ws identifier ws? paramsList ws? taskState ws? tickingRate? ws? block

     paramsList = <'('> (ws? argument (ws? <','> ws? argument)*)? ws? <')'>
     argument =  identifier
     taskState =  ('running'|'stopped')?
     tickingRate =  number ws? <'/'> ws? ( 's' | 'm' | 'h' | 'd')

     function = <'func'> ws identifier ws? paramsList ws? block
     procedure = <'proc'> ws identifier ws? paramsList ws? block

     comments = (<'\"'> #'[^\"]*' <'\"'>)

     <expr> =( nonBinaryExpr / binaryExpr)
     <nonBinaryExpr> = (unary | call | subExpr | valueExpression )
     <valueExpression> = ( literal / variable )
     <unary> = not
     not = <'!'> ws? nonBinaryExpr
     <literal> = (constant | number)
     constant = ('D'|'A') integer
     call = scriptReference ws? argList
     <argList> = <'('> ws? (namedArg (ws? <','> ws? namedArg)*)? ws? <')'>
     namedArg = ( identifier ws? <':'> ws?)? expr
     subExpr = <'('> ws? expr ws? <')'>
     <separatedExpr> =  (ws? subExpr | ws expr)

     <binarySelector> = #'[^a-zA-Z0-9\\s\\[\\]\\(\\)\\{\\}\\\"\\':#_;,]+'
     binaryExpr = nonBinaryExpr ws? (binarySelector ws? nonBinaryExpr ws?)+
     <endl> =ws? <';'>
     <name> =#'[a-zA-Z_][_\\w]*'
     <ws> = (<#'\\s+'> | <comments>)+

     <digits> = #'\\d+'
     integer = '-'? digits
     float = ('NaN' | '-'?'Infinity' | '-'? digits '.' digits)
     number = (float / integer)" ))

(defn expand-binary-expression-nodes [ast]
  (clojure.walk/postwalk
    (fn [node]
      (if (and (vector? node) (= :binaryExpr (first node)))
        (build-binary-expression (rest node))
        node
        ))
    ast))

(defn- throw-if-failure [parse-result]
  (if-let [failure (insta/get-failure parse-result)]
    (let [^String error-msg (with-out-str (fail/pprint-failure failure))]
      (throw (Exception. error-msg)))
    parse-result))

(defn parse [str]
  (->> str
       parse-program
       expand-binary-expression-nodes
       (insta/transform transformations)
       throw-if-failure))
