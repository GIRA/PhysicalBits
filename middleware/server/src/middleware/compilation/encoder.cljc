(ns middleware.compilation.encoder
  (:require [middleware.utils.conversions :refer [float->uint32 two's-complement]]
            [middleware.ast.primitives :as prims]
            [middleware.program.utils :as p]))

(defn- globals-to-encode [program]
  "We need to exclude the default-globals from the encoding"
  (remove (set p/default-globals)
          (:globals program)))

(defn- encode-global [{:keys [^double value ^long size]}]
  "If the size equals 4 we have to encode it as a float"
  (let [^long actual-value (if (= 4 size)
                             (float->uint32 value)
                             value)]
    (map (fn [^long n]
           (bit-and (bit-shift-right actual-value
                                     (* 8 n))
                    16rFF))
         (range (dec size) -1 -1))))


(defn- encode-global-group [^long size group]
  "The first byte or each group says how many variables and the size.
   - 6 bits: var count
   - 2 bits: size
     00 -> 1 byte
     01 -> 2 bytes
     10 -> 3 bytes
     11 -> 4 bytes"
  (concat [(bit-or (bit-shift-left (count group) 2)
                   (dec size))]
          (mapcat encode-global group)))

(defn encode-globals [program]
  "The globals are grouped by size before encoding. We use 6 bits to
   specify each group size, so we are limited to 63 variables per group."
  (let [to-encode (globals-to-encode program)
        groups (group-by :size to-encode)]
    (concat [(count to-encode)]
            (mapcat (fn [size]
                      (mapcat #(encode-global-group size %)
                              (partition-all 2r111111 (groups size))))
                    [1 2 3 4]))))

(defmulti encode-instruction (fn [instr script program] (:__class__ instr)))

(defn- throw-not-implemented
  ([script data]
   (throw (ex-info "Not implemented yet!"
                   (apply merge {:script script} data))))
  ([instr script program & data]
   (throw (ex-info "Not implemented yet!"
                   (apply merge
                     {:instruction instr, :script script, :program program}
                     data)))))

(defmethod encode-instruction "UziPushInstruction" ; TODO(Richo) Read-global
  [instr script program]
  (let [index (p/index-of-global program (:argument instr))]
    (if (> index 16rFF)
      (throw-not-implemented instr script program
                             {:global-index index})
      (if (> index 16rF)
        [16rF8 index]
        [(bit-or 16r80 index)]))))

(defmethod encode-instruction "UziPopInstruction" ; TODO(Richo): Write-global
  [instr script program]
  (let [index (p/index-of-global program (:argument instr))]
    (if (> index 16rFF)
      (throw-not-implemented instr script program
                             {:global-index index})
      (if (> index 16rF)
        [16rF9 index]
        [(bit-or 16r90 index)]))))

(defmethod encode-instruction "UziReadLocalInstruction"
  [instr script program]
  (let [index (p/index-of-local script (:argument instr))]
    [16rFF index]))

(defmethod encode-instruction "UziWriteLocalInstruction"
  [instr script program]
  (let [index (p/index-of-local script (:argument instr))]
    [16rFF (bit-or 16r80 index)]))


(defmethod encode-instruction "UziPrimitiveCallInstruction"
  [instr script program]
  (let [primitive (prims/primitive (-> instr :argument :name))
        ^long code (:code primitive)]
    (if (< code 16)
      [(bit-or 16rA0 code)]
      (if (< code 32)
        [(bit-or 16rB0 (- code 16))]
        (if (< code 287)
          [16rFA (- code 32)]
          (throw-not-implemented instr script program {:primitive primitive}))))))

(defmethod encode-instruction "UziScriptCallInstruction"
  [instr script program]
  (let [index (p/index-of-script program (:argument instr))]
    (if (> index 16rFF)
      (throw-not-implemented instr script program
                             {:script-index index})
      (if (> index 16rF)
        [16rFC index]
        [(bit-or 16rC0
                 index)]))))

(defn- encode-script-control [^long code instr script program]
  (let [index (p/index-of-script program (:argument instr))]
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

(defmethod encode-instruction "UziTurnOnInstruction"
  [#?(:clj {:keys [^byte argument] :as instr}
      :cljs {:keys [argument] :as instr})
   script program]
  (if (> argument 16r1F)
    (throw-not-implemented instr script program)
    [(bit-or 16r00 argument)]))

(defmethod encode-instruction "UziTurnOffInstruction"
  [#?(:clj {:keys [^byte argument] :as instr}
      :cljs {:keys [argument] :as instr})
   script program]
  (if (> argument 16r1F)
    (throw-not-implemented instr script program)
    [(bit-or 16r20 argument)]))

(defmethod encode-instruction "UziWriteInstruction"
  [#?(:clj {:keys [^byte argument] :as instr}
      :cljs {:keys [argument] :as instr})
   script program]
  (if (> argument 16r1F)
    (throw-not-implemented instr script program)
    [(bit-or 16r40 argument)]))

(defmethod encode-instruction "UziReadInstruction"
  [#?(:clj {:keys [^byte argument] :as instr}
      :cljs {:keys [argument] :as instr})
   script program]
  (if (> argument 16r1F)
    (throw-not-implemented instr script program)
    [(bit-or 16r60 argument)]))

(defmethod encode-instruction :default [instr script program]
  (throw-not-implemented instr script program))

(defn encode-instructions [script program]
  (mapcat #(encode-instruction % script program)
          (:instructions script)))

(defn- encode-instruction-count [script]
  (let [count (-> script :instructions count)]
    (if (> count 16r7FFF)
      (throw-not-implemented script {:instruction-count count})
      (if (> count 16r7F)
        [(bit-or (bit-shift-right count 8) 16r80)
         (bit-and count 16rFF)]
        [count]))))

(defn encode-script-header
  [{:keys [arguments delay locals running? once?] :as script} program]
  (let [^double delay (:value delay)
        has-delay? (> delay 0)
        has-arguments? (not (empty? arguments))
        has-locals? (not (empty? locals))]
    (concat
     ; First byte:
     ; 1 bit : is running? (1 true / 0 false)
     ; 1 bit : run once? (1 true / 0 false)
     ; 1 bit: has delay? (1 true / 0 false)
     ; 1 bit: has arguments? (1 true / 0 false)
     ; 1 bit: has locals? (1 true / 0 false)
     ; 3 bits: reserved for future use
     [(bit-and (bit-or (bit-shift-left (if running? 1 0) 7)
                       (bit-shift-left (if once? 1 0) 6)
                       (bit-shift-left (if has-delay? 1 0) 5)
                       (bit-shift-left (if has-arguments? 1 0) 4)
                       (bit-shift-left (if has-locals? 1 0) 3))
               16rFF)]

     ; If the script has a delay write its index on the global list
     (when has-delay?
       [(p/index-of-constant program delay)])

     ; If the script has arguments write the argument count
     (when has-arguments?
       [(count arguments)])

     ; If the script has locals write the local count followed by
     ; each local index on the global list
     (when has-locals?
       (concat [(count locals)]
               (map #(p/index-of-constant program (:value %))
                    locals)))

     (encode-instruction-count script))))

(defn encode-program [program]
  (concat [(count (:scripts program))]
          (encode-globals program)
          (mapcat #(encode-script-header % program)
                  (:scripts program))
          (mapcat #(encode-instructions % program)
                  (:scripts program))))

(defn encode [program]
  (-> program
      encode-program
      vec))
