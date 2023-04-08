(ns middleware.utils.conversions)

#?(:cljs (def int8 (js/Int8Array. 4)))
#?(:cljs (def int32 (js/Int32Array. (.-buffer int8) 0 1)))
#?(:cljs (def float32 (js/Float32Array. (.-buffer int8) 0 1)))

(defn uint32->float [^long n]
  #?(:clj (Float/intBitsToFloat (unchecked-int n))
     :cljs (do
             (aset int32 0 (unchecked-int n))
             (aget float32 0))))

(defn float->uint32 [^double n]
  #?(:clj (Float/floatToRawIntBits (unchecked-float n))
    :cljs (do
            (aset float32 0 (unchecked-float n))
            (aget int32 0))))

(defn bytes->uint32
  [#?(:clj [^byte n1 ^byte n2 ^byte n3 ^byte n4]
      :cljs [n1 n2 n3 n4])]
  ; NOTE(Richo): Zero fill bit shift right to force an unsigned 32-bit result.
  ; This seems to be only necessary in cljs because of javascript bit operations
  ; returning 32-bit *signed* values.
  ; https://stackoverflow.com/questions/6798111/bitwise-operations-on-32-bit-unsigned-ints
  (unsigned-bit-shift-right
   (bit-or (bit-shift-left n1 24)
           (bit-shift-left n2 16)
           (bit-shift-left n3 8)
           n4)
   0))

(defn bytes->float [^bytes bytes]
  (uint32->float (bytes->uint32 bytes)))

(defn bytes->uint16 [#?(:clj [^byte msb ^byte lsb]
                        :cljs [msb lsb])]
  (bit-or (bit-shift-left msb 8)
          lsb))

(defn uint16->bytes [n]
  [(bit-shift-right (bit-and n 16rFF00) 8)
   (bit-and n 16rFF)])

(defn two's-complement [^long byte]
  (if (>= byte 0)
    byte
    (bit-and 16rFF
             (inc (bit-xor (Math/abs byte)
                           16rFF)))))

#?(:cljs
   ;; HACK(Richo): Ratios are currently not supported in cljs
   (def ratio? (constantly false)))

(defn non-fraction [value]
  (if (ratio? value)
    (float value)
    value))

(defn- NaN? [^double value]
  (not (== value value)))

(defn try-integer [value]
  (if (NaN? value)
    value
    (let [int-val (unchecked-int value)]
      (if (= 0 (compare int-val value))
        int-val
        value))))

(defn bytes->string [bytes]
  #?(:clj (new String (byte-array bytes) "UTF-8")
     :cljs (reduce #(str %1 (String/fromCharCode %2)) "" bytes)))

(defn string->bytes [str]
  #?(:clj (map (comp byte int) str)
     :cljs (map #(.charCodeAt str %) (range (count str)))))

