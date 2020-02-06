(ns plugin.parser.core
  (:require [instaparse.core :as insta]))

(def parse-program
      (insta/parser
        "program = ws import* ws variableDeclaration* ws script * ws
         import = ws <'import'> ws identifier ws <'from'> ws importPath ws (endl | block)
         importPath = #'\\'[^\\']+\\''
         script = #'\\w+'
         block = '{' ws '}'

         variableDeclaration = <'var'> ws variable (ws <'='> ws expr)? ws endl
         identifier = name ('.' name)*
         variable = ws identifier ws
         expr=(number | variable)

         <endl>=ws <';'> ws
         <name>=#'[a-zA-Z_][_\\w]*'
         <ws> = <#'\\s'*>
         <letter> = #'[a-zA-Z]'
         <word> = #'\\w'
         <number> = #'\\d*\\.?\\d*'"))

(defn parse [str] (parse-program str))

