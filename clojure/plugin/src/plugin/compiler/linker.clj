(ns plugin.compiler.linker
  (:require [plugin.compiler.ast-utils :as ast-utils]
            [clojure.java.io :as io]
            [plugin.parser.core :as parser]
            [clojure.pprint :refer [pprint]]))

; TODO(Richo): Hack until we can actually parse core.uzi and get the actual prims
(def core-primitives
  {"turnOn" "turnOn"
   "turnOff" "turnOff"
   "read" "read"
   "write" "write"
   "getPinMode" "getPinMode"
   "setPinMode" "setPinMode"
   "toggle" "toggle"
   "getServoDegrees" "getServoDegrees"
   "setServoDegrees" "setServoDegrees"
   "servoWrite" "servoWrite"
   "+" "add"
   "-" "subtract"
   "*" "multiply"
   "/" "divide"
   "sin" "sin"
   "cos" "cos"
   "tan" "tan"
   "==" "equals"
   "!=" "notEquals"
   ">" "greaterThan"
   ">=" "greaterThanOrEquals"
   "<" "lessThan"
   "<=" "lessThanOrEquals"
   "!" "negate"
   "delayMs" "delayMs"
   "&" "bitwiseAnd"
   "|" "bitwiseOr"
   "millis" "millis"
   "coroutine" "coroutine"
   "serialWrite" "serialWrite"
   "round" "round"
   "ceil" "ceil"
   "floor" "floor"
   "sqrt" "sqrt"
   "abs" "abs"
   "ln" "ln"
   "log10" "log10"
   "exp" "exp"
   "pow10" "pow10"
   "asin" "asin"
   "acos" "acos"
   "atan" "atan"
   "atan2" "atan2"
   "**" "power"
   "isOn" "isOn"
   "isOff" "isOff"
   "%" "remainder"
   "constrain" "constrain"
   "randomInt" "randomInt"
   "random" "random"
   "isEven" "isEven"
   "isOdd" "isOdd"
   "isPrime" "isPrime"
   "isWhole" "isWhole"
   "isPositive" "isPositive"
   "isNegative" "isNegative"
   "isDivisibleBy" "isDivisibleBy"
   "seconds" "seconds"
   "isCloseTo" "isCloseTo"
   "delayS" "delayS"
   "delayM" "delayM"
   "minutes" "minutes"
   "mod" "modulo"
   "startTone" "tone"
   "stopTone" "noTone"})

(defn bind-primitives [ast]
  (let [scripts (set (map :name
                          (:scripts ast)))]
    (ast-utils/transform
     ast
     ; NOTE(Richo): We should only associate a prim name if the selector doesn't match
     ; an existing script. Scripts have precedence over primitives!
     "UziCallNode" (fn [{:keys [selector] :as node}]
                     (if (contains? scripts selector)
                       node
                       (assoc node
                              :primitive-name (core-primitives selector)))))))

(defn error [msg]
  (throw (Exception. msg)))

(defn apply-alias [ast alias]
  (let [update-node (fn [key node] (assoc node key (str alias "." (key node))))
        locals (atom #{})]
    (ast-utils/transform
     ast
     "UziVariableDeclarationNode" (fn [node]
                                    (if (:local? node)
                                      node
                                      (update-node :name node)))
     "UziVariableNode" (fn [node]
                         (if (:local? node)
                           node
                           (update-node :name node)))
     "UziFunctionNode" (partial update-node :name)
     "UziCallNode" (partial update-node :selector))))

(declare resolve-imports)

(defn resolve-import [{:keys [alias path] :as imp} libs-dir]
  (let [file (io/file libs-dir path)]
    (if (.exists file)
      (let [imported-ast (resolve-imports (parser/parse (slurp file)) libs-dir)]
        {:import (assoc imp :isResolved true)
         :program (apply-alias imported-ast alias)})
      (error "FILE NOT FOUND"))))

(defn resolve-variable-scope [ast]
  (let [locals (atom #{})
        reset-locals! (fn [{:keys [arguments body] :as node}]
                        (reset! locals (set (concat (map :name arguments)
                                                    (map :name (ast-utils/filter node "UziVariableDeclarationNode")))))
                        node)
        assign-scope (fn [{:keys [name] :as node}]
                       (assoc node :local? (contains? @locals name)))]
    (ast-utils/transform
     ast

     "UziTaskNode" reset-locals!
     "UziProcedureNode" reset-locals!
     "UziFunctionNode" reset-locals!

     "UziVariableNode" assign-scope
     "UziVariableDeclarationNode" assign-scope)))

(defn print [a] (pprint a) a)

(defn build-new-program [ast resolved-imports]
  (let [imported-programs (map :program resolved-imports)
         imported-globals (mapcat :globals imported-programs)
         imported-scripts (mapcat :scripts imported-programs)]
    (assoc ast
           :imports (map :import resolved-imports)
           :globals (vec (concat imported-globals (:globals ast)))
           :scripts (vec (concat imported-scripts (:scripts ast))))))

(defn resolve-imports [ast libs-dir]
  (let [resolved-imports (map (fn [imp] (resolve-import imp libs-dir))
                              (filter (complement :isResolved)
                                      (:imports ast)))]
    (-> ast
        resolve-variable-scope
        (build-new-program resolved-imports)
        bind-primitives)))
