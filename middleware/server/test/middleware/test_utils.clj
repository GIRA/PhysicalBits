(ns middleware.test-utils
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [middleware.sound-notification :as sound]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.code-generator.code-generator :as cg]
            [middleware.compiler.utils.program :as p]))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))

(defn test-name []
  (str/join "." (map (comp :name meta) *testing-vars*)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; CompileStats ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def compile-stats-path "../../firmware/Simulator/SimulatorTest/TestFiles/CompileStats.1.csv")
(def ^:private global-stats (atom {}))

(defn register-program! [program-or-string & args]
  (let [ns-name (ns-name (:ns (meta (first *testing-vars*))))
        test-name (test-name)
        src (if (string? program-or-string)
              program-or-string
              (cg/print program-or-string))
        program (apply cc/compile-uzi-string src args)
        stats {:instruction-count (count (p/instructions (:compiled program)))
               :global-count (count (:globals (:compiled program)))
               :encoded-size (count (en/encode (:compiled program)))
               :src src}]
    (swap! global-stats update
           (str ns-name "/" test-name)
           #(conj % (assoc stats :name (str ns-name "/" test-name "#" (count %)))))))

(defn- write-compile-stats []
  (let [cols [:name :instruction-count :global-count :encoded-size]
        rows (sort-by :name (apply concat (vals @global-stats)))
        aggregate (fn [name f]
                    (into {:name name}
                          (map (fn [k] [k (apply f (mapv k rows))])
                               (drop 1 cols))))
        min-values (aggregate "MIN" min)
        max-values (aggregate "MAX" max)
        avg-values (aggregate "AVERAGE" (fn [& values] (/ (reduce + values)
                                                          (count values))))
        median-values (aggregate "MEDIAN" (fn [& values]
                                            (let [sorted (sort values)
                                                  countd (count values)
                                                  midPoint (int (/ countd 2))]
                                              (if (odd? countd)
                                                (nth sorted midPoint)
                                                (/ (+ (nth sorted midPoint)
                                                      (nth sorted (dec midPoint)))
                                                   2)))))

        write-row! (fn [w r]
                     (.write w (str/join "," (map #(if (ratio? %) (double %) %) r)))
                     (.newLine w))]
    (with-open [w (io/writer compile-stats-path)]
      ; Columns
      (write-row! w (map name cols))
      ; Aggregate data
      (write-row! w (map min-values cols))
      (write-row! w (map max-values cols))
      (write-row! w (map avg-values cols))
      (write-row! w (map median-values cols))
      ; Rows
      (doseq [row rows]
        (write-row! w (map row cols)))))
  (reset! global-stats {}))

(defmethod report :summary [m]
  (sound/play! (and (zero? (:fail m))
                    (zero? (:error m))))
  (write-compile-stats)
  (with-test-out
    (println "\nRan" (:test m) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (:fail m) "failures," (:error m) "errors.")))
