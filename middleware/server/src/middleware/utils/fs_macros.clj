(ns middleware.utils.fs-macros)

(defmacro read-file* [path]
  (let [files (filter (memfn isFile)
                      (clojure.core/file-seq (clojure.java.io/file "../../uzi/libraries")))
        data (into {} (map (fn [f] [(.getName f) (clojure.core/slurp f)]) files))]
    `(get ~data ~path nil)))
