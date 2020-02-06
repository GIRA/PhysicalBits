(ns plugin.parser.core
  (:require [instaparse.core :as insta]))

(def parse-program
      (insta/parser
        "program = ws import* ws variableDeclaration* ws script * ws
         import = ws <'import'> ws identifier ws <'from'> ws importPath ws (';' | block)
         importPath = #'\\'[^\\']+\\''
         script = #'\\w+'
         block = '{' ws '}'
         variableDeclaration = #'\\w+'
         identifier = name ('.' name)*
         <name>=#'[a-zA-Z_][_\\w]*'
         <ws> = <#'\\s'*>
         <letter> = #'[a-zA-Z]'
         <word> = #'\\w'
         <number> = #'\\d*\\.?\\d*'"))

(defn parse [str] (parse-program str))

