(ns middleware.program.emitter
  (:require [middleware.utils.core :refer [seek]]
            [middleware.utils.conversions :as c]))

(defn- value-size-int [^long value]
  (or (seek (fn [^long size] (< value (Math/pow 2 (* 8 size))))
            [1 2 3 4])
      4))

(defn- value-size
  "Return the number of bytes necessary to encode this value.
   If the value is negative or float then the size is 4 bytes. Also, the
   max number of bytes is 4."
  ^long [value]
  (if (or (zero? value)
          (pos-int? value))
    (value-size-int value)
    4))

(defn- sort-globals [globals]
  (sort (fn [{a-name :name, ^double a-value :value, ^long a-size :size}
             {b-name :name, ^double b-value :value, ^long b-size :size}]
          (if (= a-size b-size)
            (if (= a-value b-value)
              (compare (or a-name "")
                       (or b-name ""))
              (< a-value b-value))
            (< a-size b-size)))
        globals))

(defn program [& {:keys [globals scripts]
                  :or {globals [] scripts []}}]
  {:__class__ "UziProgram"
   :globals (-> globals sort-globals vec)
   :scripts scripts})

(def ^:private simplify
  "Tries to reduce the value to its equivalent integer or float (ratios not allowed)"
  (comp c/try-integer c/non-fraction))

(defn variable
  ([name] (variable name 0))
  ([name value]
   (let [actual-value (simplify (or value 0))]
     {:__class__ "UziVariable"
      :name name
      :value actual-value
      :size (value-size actual-value)})))

(defn constant [value]
  (let [actual-value (simplify (or value 0))]
    {:__class__ "UziVariable"
     :value actual-value
     :size (value-size actual-value)}))

(defn script
  [& {:keys [name type delay running? arguments locals instructions]
      :or {type :timer, delay 0, running? false,
           arguments [], locals [], instructions []}}]
  ; Possible types: 
  ; * task (delay = 0, running? = T/F)
  ; * function (delay = 0, running? = F)
  ; * procedure (delay = 0, running? = F)
  ; * timer (delay = [0-Infinity], running? = T/F)
  (when (not= type :timer)
    (assert (zero? delay) "Only timer scripts can have a :delay > 0")
    (when (not= type :task)
      (assert (not running?) "Only tasks or timers can have :running? = true")))
  {:__class__ "UziScript"
   :name name
   :type type
   :running? running?
   :delay (constant delay)
   :arguments arguments
   :locals locals
   :instructions instructions})

(defn write-global [var-name]
  {:__class__ "UziPopInstruction"
   :argument (variable var-name)})

(defn prim-call [prim-name]
  {:__class__ "UziPrimitiveCallInstruction"
   :argument {:__class__ "UziPrimitive"
              :name prim-name}})

(defn push-value [value]
  {:__class__ "UziPushInstruction"
    :argument (constant value)})

(defn read-global [var-name]
  {:__class__ "UziPushInstruction"
   :argument (variable var-name)})


(defn start [script-name]
  {:__class__ "UziStartScriptInstruction"
   :argument script-name})

(defn stop [script-name]
  {:__class__ "UziStopScriptInstruction"
   :argument script-name})

(defn pause [script-name]
  {:__class__ "UziPauseScriptInstruction"
   :argument script-name})

(defn resume [script-name]
  {:__class__ "UziResumeScriptInstruction"
   :argument script-name})

(defn write-local [var-name]
  {:__class__ "UziWriteLocalInstruction"
   :argument (variable var-name)})

(defn read-local [var-name]
  {:__class__ "UziReadLocalInstruction"
   :argument (variable var-name)})

(defn script-call [script-name]
  {:__class__ "UziScriptCallInstruction"
   :argument script-name})

(defn jmp [offset]
  {:__class__ "UziJMPInstruction",
   :argument offset})

(defn jz [offset]
  {:__class__ "UziJZInstruction",
   :argument offset})

(defn jnz [offset]
  {:__class__ "UziJNZInstruction",
   :argument offset})

(defn jlte [offset]
  {:__class__ "UziJLTEInstruction",
   :argument offset})

(defn read-pin [pin-number]
  {:__class__ "UziReadInstruction"
   :argument pin-number})

(defn write-pin [pin-number]
  {:__class__ "UziWriteInstruction"
   :argument pin-number})

(defn turn-on-pin [pin-number]
  {:__class__ "UziTurnOnInstruction"
   :argument pin-number})

(defn turn-off-pin [pin-number]
  {:__class__ "UziTurnOffInstruction"
   :argument pin-number})
