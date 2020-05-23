(ns middleware.compiler.utils.program
  (:require [middleware.compiler.emitter :as emit]))


(defn value-size
  "Return the number of bytes necessary to encode this value.
  	If the value is negative or float then the size is 4 bytes. Also, the
  	max number of bytes is 4."
  [value]
  (if (or (float? value) (neg? value))
    4
    (or (first (filter (fn [size] (< value (Math/pow 2 (* 8 size))))
                       [1 2 3 4]))
        4)))

; TODO(Richo): The functions in this namespace rely on the program having its globals
; sorted. That means that I need to remember to call this function before using any
; of them. Of course, I should just change the compiler to emit the program with its
; variables already sorted and avoid this problem. However, in order to do that I need
; to fix a lot of tests first, and it's kind of a pain in the ass. So first, I will
; make it work by calling this function in the correct places and then I'll refactor
; the compiler.
(defn sort-globals [program]
  (let [sorted-globals (sort-by (fn [global] (assoc global :size (value-size (:value global))))
                                (fn [{a-name :name, a-value :value, a-size :size}
                                     {b-name :name, b-value :value, b-size :size}]
                                  (if (= a-size b-size)
                                    (if (= a-value b-value)
                                      (compare (or a-name "")
                                               (or b-name ""))
                                      (< a-value b-value))
                                    (< a-size b-size)))
                                (:globals program))]
    (assoc program :globals (vec sorted-globals))))

(def default-globals
  "This values are *always* first in the global list, whether they
   are used or not. The VM knows about this already so we don't need
   to encode them."
  (map emit/constant [0 1 -1]))

(defn all-globals [program]
  "Returns all the globals in the program in the correct order"
  (concat default-globals
          (filter (complement (set default-globals))
                  (:globals program))))

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
