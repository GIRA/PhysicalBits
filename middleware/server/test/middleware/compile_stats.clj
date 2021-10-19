(ns middleware.compile-stats
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.core.async :as a :refer [go <!!]]
            [middleware.sound-notification :as sound]
            [middleware.compilation.parser :as p]
            [middleware.compilation.compiler :as cc]
            [middleware.compilation.encoder :as en]
            [middleware.compilation.codegen :as cg]
            [middleware.program.utils :as program]
            [middleware.test-utils :refer [test-name init-dependencies]]))

(def compile-stats-path "../../firmware/Simulator/SimulatorTest/TestFiles/CompileStats.csv")
(def ^:private programs (atom {}))

(defn register-program! [ast-or-src & args]
  (let [namespace (:ns (meta (first *testing-vars*)))
        ns-name (if namespace (ns-name namespace) "ACAACA")
        test-name (test-name)
        key (str ns-name "/" test-name)]
    (swap! programs update
           key
           #(conj % {:name (str key "#" (count %))
                     :ast-or-src ast-or-src
                     :args args}))))

(defn- collect-stats [programs]
  (map (fn [{:keys [name ast-or-src args]}]
         (let [src (if (string? ast-or-src)
                     ast-or-src
                     (cg/generate-code ast-or-src))
               ast (p/parse src)
               program (apply cc/compile-tree ast args)]
           {:instruction-count (count (program/instructions program))
            :global-count (count (:globals program))
            :encoded-size (count (en/encode program))
            :name name}))
       (apply concat (vals programs))))

(defn- write-row! [^java.io.BufferedWriter writer row]
  (.write writer (str/join "," (map #(if (ratio? %) (double %) %) row)))
  (.newLine writer))

(defn- write-compile-stats []
  (when (not (empty? @programs))
    (init-dependencies)
    (let [cols [:name :instruction-count :global-count :encoded-size]
          rows (sort-by :name (collect-stats @programs))
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
                                                     2)))))]
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
          (write-row! w (map row cols))))))
  (reset! programs {}))

(defmethod report :summary [m]
  (sound/play! (and (zero? (:fail m))
                    (zero? (:error m))))
  (a/thread (write-compile-stats))
  (with-test-out
    (println "\nRan" (:test m) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (:fail m) "failures," (:error m) "errors.")))
