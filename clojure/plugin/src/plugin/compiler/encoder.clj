(ns plugin.compiler.encoder
  (:require [plugin.utils.conversions :refer :all]))

(defmulti encode-instruction :__class__)

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

(def default-globals [0 1 -1])

(defn all-globals [{globals :sorted-globals}]
  (concat default-globals
          (filter (complement (set default-globals))
                  (map :value globals))))

(defn encode-globals [globals]
  (let [to-encode (filter (complement (set default-globals))
                          (map :value globals))
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
  [{:keys [arguments delay locals running?]} globals]
  (let [has-delay? (> (:value delay) 0)
        has-arguments? (not (empty? arguments))
        has-locals? (not (empty? locals))]
    (concat [(bit-and (bit-or (bit-shift-left (if running? 1 0) 7)
                              (bit-shift-left (if has-delay? 1 0) 6)
                              (bit-shift-left (if has-arguments? 1 0) 5)
                              (bit-shift-left (if has-locals? 1 0) 4))
                      16rFF)]
            (if has-delay?
              [(.indexOf globals (:value delay))]
              []))))

(defn encode-instructions [instructions]
  (concat [(count instructions)]
          (mapcat encode-instruction instructions)))

(defn encode-script [{:keys [instructions] :as script} globals]
  (concat (encode-script-header script globals)
          (encode-instructions instructions)))

(defn encode-program [{:keys [scripts sorted-globals] :as program}]
  (concat [(count scripts)]
          (encode-globals sorted-globals)
          (mapcat (fn [script]
                    (encode-script script (all-globals program)))
                  scripts)))

(defmethod encode-instruction :default [o] [])

(defn encode [program]
  (-> program
      sort-globals
      encode-program
      vec))

#_(
   (def src "task main() running 1/s {}")
   (def program (sort-globals (plugin.compiler.core/compile-uzi-string src)))
   program







   (all-globals program)
   (encode-globals (:sorted-globals program))
   (encode-script-header (get (:scripts program) 0) (all-globals program))
   (encode-program program)










   )
