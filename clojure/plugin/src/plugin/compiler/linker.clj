(ns plugin.compiler.linker
  (:require [plugin.compiler.ast-utils :as ast-utils]))

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
