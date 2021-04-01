(ns benchmarks
  (:require [criterium.core :refer [bench with-progress-reporting]]))

(comment
 (require '[middleware.parser.parser :as new-parser])

 (def sources (mapv slurp
                    (filter #(.isFile %)
                            (file-seq (clojure.java.io/file "../../uzi/libraries")))))

 (def trees (time (mapv new-parser/parse sources)))

 (with-progress-reporting (bench (mapv new-parser/parse sources) :verbose))


 (def src (slurp "../../uzi/tests/syntax.uzi"))
 (with-progress-reporting (bench (new-parser/parse src) :verbose))


 ,)
