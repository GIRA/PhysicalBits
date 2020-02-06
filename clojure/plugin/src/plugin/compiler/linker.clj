(ns plugin.compiler.linker
  (:require [clojure.walk :as w]))

; TODO(Richo): Hack until we can actually parse core.uzi and get the actual prims
(def core-primitives
  {"+" "add"})

(defn bind-primitives [ast]
  (w/postwalk #(if (= "UziCallNode" (get % :__class__ ))
                 (assoc % :primitive-name (core-primitives (:selector %)))
                 %)
              ast))
