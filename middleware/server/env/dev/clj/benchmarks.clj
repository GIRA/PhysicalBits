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


(def size 100000)
(defn version-1 [size]
  (let [result (atom [])]
    (doseq [n (range 0 size)]
      (swap! result conj n))
    @result))

 (defn version-2 [size]
   (let [result (atom (transient []))]
     (doseq [n (range 0 size)]
       (swap! result conj! n))
     (persistent! @result)))

 (defn version-3 [size]
   (let [result (atom (transient []))]
     (doseq [n (range 0 size)]
       (reset! result (conj! @result n)))
     (persistent! @result)))

 (defn version-4 [size]
   (let [result (volatile! (transient []))]
     (doseq [n (range 0 size)]
       (vswap! result conj! n))
     (persistent! @result)))

 (defn version-5 [size]
   (let [result (volatile! (transient []))]
     (doseq [n (range 0 size)]
       (vreset! result (conj! @result n)))
     (persistent! @result)))

 (with-progress-reporting (bench (version-1 size) :verbose))
 (with-progress-reporting (bench (version-2 size) :verbose))
 (with-progress-reporting (bench (version-3 size) :verbose))
 (with-progress-reporting (bench (version-4 size) :verbose))
 (with-progress-reporting (bench (version-5 size) :verbose))

 (defn vrange [^long n]
   (loop [i 0 v []]
     (if (< i n)
       (recur (inc i) (conj v i))
       v)))

 (defn vrange2 [^long n]
   (loop [i 0 v (transient [])]
     (if (< i n)
       (recur (inc i) (conj! v i))
       (persistent! v))))

(with-progress-reporting (bench (vrange size) :verbose))
(with-progress-reporting (bench (vrange2 size) :verbose))

 ,)
