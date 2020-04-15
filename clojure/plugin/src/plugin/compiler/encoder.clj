(ns plugin.compiler.encoder
  (:require [plugin.utils.conversions :refer :all]
            [plugin.compiler.emitter :as emit]
            [plugin.compiler.primitives :as prims]))

(defn variable-size
  "Return the number of bytes necessary to encode this variable.
	If the value is negative or float then the size is 4 bytes. Also, the
	max number of bytes is 4."
  [value]
  (if (or (float? value) (neg? value))
    4
    (or (first (filter (fn [size] (< value (Math/pow 2 (* 8 size))))
                       [1 2 3 4]))
        4)))

(defn sort-globals [program]
  (let [sorted-globals (sort-by (fn [{:keys [value]}] [value (variable-size value)])
                                (fn [[a-value a-size] [b-value b-size]]
                                  (if (= a-size b-size)
                                    (< a-value b-value)
                                    (< a-size b-size)))
                                (:globals program))]
    (assoc program :sorted-globals (vec sorted-globals))))

(def default-globals (map emit/constant [0 1 -1]))

(defn all-globals [{globals :sorted-globals}]
  (concat default-globals
          (filter (complement (set default-globals))
                  globals)))

(defn index-of-constant [program value]
  (.indexOf (all-globals program)
            (emit/constant value)))

(defn index-of-variable [program name]
  (.indexOf (map :name (all-globals program))
            name))

(defn index-of-global [program global]
  (if (contains? global :name) ; TODO(Richo): This sucks!
    (index-of-variable program (:name global))
    (index-of-constant program (:value global))))

(defn index-of-local [script variable]
  (.indexOf (map :name (concat (:arguments script)
                               (:locals script)))
            (:name variable)))

(defn- globals-to-encode [globals]
  (let [constants-to-exclude (set default-globals)]
    (filter (complement constants-to-exclude)
            globals)))

(defn encode-globals [globals]
  (let [to-encode (map :value (globals-to-encode globals))
        groups (group-by variable-size to-encode)
        encode-global (fn [value size]
                        (let [actual-value (if (= 4 size)
                                             (float->uint32 value)
                                             value)]
                          (map (fn [n]
                                 (bit-and (bit-shift-right actual-value
                                                           (* 8 n))
                                          16rFF))
                               (range (dec size) -1 -1))))
        encode-group-size (fn [size group]
                            (bit-or (bit-shift-left (count group) 2)
                                    (dec size)))]
    (concat [(count to-encode)]
            (mapcat (fn [size]
                      (mapcat (fn [partition]
                                (concat [(encode-group-size size partition)]
                                        (mapcat #(encode-global % size)
                                                partition)))
                              (partition-all 2r111111 (groups size))))
                    [1 2 3 4]))))

(defn encode-script-header
  [{:keys [arguments delay locals running?]} program]
  (let [has-delay? (> (:value delay) 0)
        has-arguments? (not (empty? arguments))
        has-locals? (not (empty? locals))]
    (concat
            ; First byte:
  		      ; 1 bit : isTicking (1 true / 0 false)
  		      ; 1 bit: hasDelay (1 true / 0 false)
  		      ; 1 bit: hasArguments (1 true / 0 false)
  		      ; 1 bit: hasLocals (1 true / 0 false)
  		      ; 4 bits: reserved for future use
            [(bit-and (bit-or (bit-shift-left (if running? 1 0) 7)
                              (bit-shift-left (if has-delay? 1 0) 6)
                              (bit-shift-left (if has-arguments? 1 0) 5)
                              (bit-shift-left (if has-locals? 1 0) 4))
                      16rFF)]

            ; If the script has a delay write its index on the global list
            (if has-delay?
              [(index-of-constant program (:value delay))]
              [])

            ; TODO(Richo): Arguments!

            ; If the script has locals write the local count followed by
            ; each local index on the global list
            (if has-locals?
              (concat [(count locals)]
                      (map #(index-of-constant program (:value %))
                           locals))
              []))))

(defmulti encode-instruction (fn [instr script program] (:__class__ instr)))

(defn- throw-not-implemented [instr script program & data]
  (throw (ex-info "Not implemented yet!"
                  (apply merge
                    {:instruction instr, :script script, :program program}
                    data))))

(defmethod encode-instruction "UziPushInstruction" ; TODO(Richo) Read-global
  [instr script program]
  (let [index (index-of-global program (:argument instr))]
    (if (> index 16rFF)
      (throw-not-implemented instr script program
                             {:global-index index})
      (if (> index 16rF)
        [16rF8 index]
        [(bit-or 16r80 index)]))))

(defmethod encode-instruction "UziPopInstruction" ; TODO(Richo): Write-global
  [instr script program]
  (let [index (index-of-global program (:argument instr))]
    (if (> index 16rFF)
      (throw-not-implemented instr script program
                             {:global-index index})
      (if (> index 16rF)
        [16rF9 index]
        [(bit-or 16r90 index)]))))

(defmethod encode-instruction "UziReadLocalInstruction"
  [instr script program]
  (let [index (index-of-local script (:argument instr))]
    [16rFF index]))

(defmethod encode-instruction "UziWriteLocalInstruction"
  [instr script program]
  (let [index (index-of-local script (:argument instr))]
    [16rFF (bit-or 16r80 index)]))


(defmethod encode-instruction "UziPrimitiveCallInstruction"
  [instr script program]
  (let [primitive (prims/primitive (-> instr :argument :name))
        code (:code primitive)]
    (if (< code 16)
      [(bit-or 16rA0 code)]
      (if (< code 32)
        [(bit-or 16rB0 (- code 16))]
        (if (< code 287)
          [16rFA (- code 32)]
          (throw-not-implemented instr script program {:primitive primitive}))))))

(defn- encode-script-control [code instr script program]
  (let [index (.indexOf (map :name (:scripts program))
                        (:argument instr))]
    (if (> index 16r7F)
      (throw-not-implemented instr script program
                             {:script-index index})
      (if (> index 16r7)
        [(bit-or 16rF0 (bit-shift-right code 1))
         (bit-or (bit-and 16rFF (bit-shift-left code 7))
                 (bit-and 16r7F index))]
        [(bit-or (bit-shift-left code 3)
                 index)]))))

(defmethod encode-instruction "UziPauseScriptInstruction"
  [instr script program]
  (encode-script-control 2r11101 instr script program))

(defmethod encode-instruction "UziStopScriptInstruction"
  [instr script program]
  (encode-script-control 2r11100 instr script program))

(defmethod encode-instruction "UziResumeScriptInstruction"
  [instr script program]
  (encode-script-control 2r11011 instr script program))

(defmethod encode-instruction "UziStartScriptInstruction"
  [instr script program]
  (encode-script-control 2r11010 instr script program))

(defmethod encode-instruction "UziJMPInstruction"
  [instr script program]
  [16rF0 (-> instr :argument two's-complement)])

(defmethod encode-instruction "UziJZInstruction"
  [instr script program]
  [16rF1 (-> instr :argument two's-complement)])

(defmethod encode-instruction "UziJNZInstruction"
  [instr script program]
  [16rF2 (-> instr :argument two's-complement)])

(defmethod encode-instruction "UziJLTEInstruction"
  [instr script program]
  [16rF5 (-> instr :argument two's-complement)])

(defmethod encode-instruction :default [o _ _]
  (println "Error: MISSING ENCODE FUNCTION")
  (prn o)
  [])

(defn encode-instructions [instructions script program]
  (concat [(count instructions)]
          (mapcat #(encode-instruction % script program) instructions)))

(defn encode-script
  [{:keys [instructions] :as script} program]
  (concat (encode-script-header script program)
          (encode-instructions instructions script program)))

(defn encode-program [{:keys [scripts sorted-globals] :as program}]
  (concat [(count scripts)]
          (encode-globals sorted-globals)
          (mapcat (fn [script]
                    (encode-script script program))
                  scripts)))

(defn encode [program]
  (-> program
      sort-globals
      encode-program
      vec))

#_(
   (def src "

             var a;
             task main() {
               a = 3 + 4;
               a = a + 1;
               toggle(a);
             }
")
   (def program (sort-globals (plugin.compiler.core/compile-uzi-string src)))
   program

   (def globals (:sorted-globals program))





   (all-globals program)
   (encode-globals (:sorted-globals program))

   (let [
        default-globals-set (set (map :value default-globals))
          to-encode (filter (fn [global]
                              (or (contains? global :name)
                                  (not (contains? default-globals-set (:value global)))))
                            globals)]
     to-encode
     #_(mapv variable-size to-encode))

   (index-of-constant (all-globals program) 1000)
   (encode-script-header (get (:scripts program) 0) (all-globals program))
   (encode-program program)








   )
