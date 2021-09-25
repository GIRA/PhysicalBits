(ns middleware.utils.core)

(defn seek [pred coll]
  (reduce #(when (pred %2) (reduced %2)) nil coll))
