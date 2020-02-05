(ns plugin.emitter)

(defn program [& {:keys [globals scripts]
                  :or {globals [] scripts []}}]
  {:__class__ "UziProgram"
   :variables globals
   :scripts scripts})

(defn variable [& {:keys [name value] :or {value 0}}]
  (let [result {:__class__ "UziVariable" :value value}]
    (if name
      (assoc result :name name)
      result)))

(defn script
  [& {:keys [name arguments delay running? locals instructions]
      :or {arguments [] delay 0 running? false locals [] instructions []}}]
  {:__class__ "UziScript",
   :arguments arguments,
   :delay (variable :value delay),
   :instructions instructions,
   :locals locals,
   :name name,
   :ticking running?})

(defn pop [var-name]
  {:__class__ "UziPopInstruction"
   :argument (variable :name var-name)})

(defn prim [prim-name]
  {:__class__ "UziPrimitiveCallInstruction"
   :argument {:__class__ "UziPrimitive"
              :name prim-name}})

(defn push-value [value]
  {:__class__ "UziPushInstruction"
    :argument (variable :value value)})

(defn push-var [var-name]
  {:__class__ "UziPushInstruction"
   :argument (variable :name var-name)})
