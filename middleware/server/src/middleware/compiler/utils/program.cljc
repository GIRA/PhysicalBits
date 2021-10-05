(ns middleware.compiler.utils.program
  (:require [middleware.utils.core :refer [index-of]]
            [middleware.compiler.emitter :as emit]))

(def default-globals
  "These values are *always* first in the global list, whether they
   are used or not. The VM knows about this already so we don't need
   to encode them."
  (mapv emit/constant [0 1 -1]))

(defn all-globals [program]
  "Returns all the globals in the program in the correct order"
  (concat default-globals
          (remove (set default-globals)
                  (:globals program))))

(defn index-of-constant [program value]
  (index-of (all-globals program)
            (emit/constant value)))

(defn index-of-variable
  ([program name]
   (index-of (map :name (all-globals program))
             name))
  ([program name not-found]
   (let [index (index-of-variable program name)]
     (if (= -1 index) not-found index))))

(defn index-of-global ^long [program global]
  (if (contains? global :name) ; TODO(Richo): This sucks!
    (index-of-variable program (:name global))
    (index-of-constant program (:value global))))

(defn index-of-local ^long [script variable]
  (index-of (map :name (concat (:arguments script)
                               (:locals script)))
            (:name variable)))

(defn index-of-script [program script-name]
  (index-of (map :name (:scripts program))
            script-name))

(defn instructions [program]
  (mapcat :instructions (:scripts program)))
