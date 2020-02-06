(ns plugin.parser.core
  (:require [instaparse.core :as insta]))

(def parse-program
      (insta/parser
        "program = ws import* ws variableDeclaration* ws script * ws
         import = ws <'import'> ws identifier ws <'from'> ws importPath ws (endl | block)
         importPath = #'\\'[^\\']+\\''
         <script> =  (task | function | procedure)
         block = ws <'{'> ws statementList ws <'}'> ws

         <statementList> = ws statement* statement
         <statement> = ws (variableDeclaration | assignment | return | conditional
                      | startTask | stopTask | pauseTask | resumeTask
                      |while|doWhile|until|doUntil|repeat|forever|for|yield|expressionStatement) ws

         variableDeclaration = <'var'> ws variable (ws <'='> ws expr)?  endl
         assignment = ws variable ws <'='> ws expr  endl
         return =  ws 'return' ws expr? endl
         conditional = ws <'if'> ws expr ws block (ws <'else'> ws expr) endl
         while = ws <'while'> ws expr ws block
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
         scriptReference = ws identifier ws

         identifier = name ('.' name)*
         variable = ws identifier ws

         task=<'task'> ws identifier paramsList taskState tickingRate? block ws

         paramsList = ws<'('> (ws argument (ws ',' ws argument)*)? ws <')'> ws
         argument = ws identifier ws
         taskState = ws ('running'|'stopped') ws
         tickingRate = number ws <'/'> ( 's' | 'm' | 'h' | 'd')

         function = ws <'func'> ws identifier ws paramsList ws block ws
         procedure = ws <'proc'> ws identifier ws paramsList ws block ws






         <expr>=(binaryExpr | nonBinaryExpr)
         <nonBinaryExpr> = (unary | literal | call | variable | subExpr)
         <unary> = not
         not = ws <'!'> ws nonBinaryExpr ws
         literal = (constant | number )
         constant = ws ('D'|'A') integer ws
         call = ws scriptReference argList ws
         argList = ws <'('> ws (namedArg (ws <','> ws namedArg)?)?<')'>
         <namedArg> = ws( identifier ws <':'> ws)? expr ws
         subExpr = ws <'('> ws expr ws <')'> ws

         binaryExpr = ws nonBinaryExpr ws (binarySelector ws nonBinaryExpr)+ ws
         binarySelector = #'[^a-zA-Z0-9\\s\\[\\]\\(\\)\\{\\}\\\"\\':#_;,]'

         <endl>=ws <';'> ws
         <name>=#'[a-zA-Z_][_\\w]*'
         <ws> = <#'\\s'*>
         <letter> = #'[a-zA-Z]'
         <word> = #'\\w'
         <integer> = #'-?\\d+'
         <number> = #'\\d*\\.?\\d*'"))

(defn parse [str] (parse-program str))

