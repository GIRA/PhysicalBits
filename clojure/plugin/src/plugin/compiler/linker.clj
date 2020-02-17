(ns plugin.compiler.linker
  (:require [plugin.compiler.ast-utils :as ast-utils]))

; TODO(Richo): Hack until we can actually parse core.uzi and get the actual prims
(def core-primitives
  {"+" "add"
   "toggle" "toggle"})

(defn bind-primitives [ast]
  (ast-utils/transform
   ast
   "UziCallNode" #(assoc % :primitive-name (core-primitives (:selector %)))))
