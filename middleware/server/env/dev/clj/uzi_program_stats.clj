(ns uzi-program-stats
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.data.csv :as csv]
            [cheshire.core :as json]
            [middleware.core :as uzi]
            [middleware.compilation.encoder :as en]
            [middleware.program.utils :as program]))

(def phb-files
  (->> (keep (fn [file]
               (let [name (.getName file)
                     [exercise student extension] (str/split name #"[_.]")]
                 (when (= extension "phb")
                   {:file (.getPath file)
                    :student (Integer/parseInt student)
                    :exercise (Integer/parseInt exercise)})))
             (.listFiles (io/file "../../test-programs")))
       (sort-by :student)
       (sort-by :exercise)))

(defn read-src [phb-file]
  (let [json (json/parse-string (slurp phb-file))
        src (get-in json ["code"])]
    src))

(defn write-csv-file []
  (with-open [writer (io/writer (io/file "uzi_program_stats.csv"))]
    (csv/write-csv writer [["exercise" "student" "instructions" "bytecodes"]])
    (csv/write-csv writer (mapv (fn [{:keys [file student exercise]}]
                                  (try
                                    (let [program (uzi/compile-uzi-string (read-src file))]
                                      [exercise student
                                       (count (program/instructions program))
                                       (count (en/encode program))])
                                    (catch Throwable ex
                                      (println "ERROR!" file))))
                                phb-files))))

(defn instruction-type [instr]
  (case (:__class__ instr)
    "UziPushInstruction" :read-global
    "UziReadLocalInstruction" :read-local
    "UziScriptCallInstruction" :script-call
    "UziReadInstruction" :read-pin
    "UziJMPInstruction" :jmp
    "UziJZInstruction" :jz
    "UziJNZInstruction" :jnz
    "UziJLTEInstruction" :jlte
    "UziPopInstruction" :write-global
    "UziStartScriptInstruction" :start
    "UziStopScriptInstruction" :stop
    "UziPauseScriptInstruction" :pause
    "UziResumeScriptInstruction" :resume
    "UziWriteLocalInstruction" :write-local
    "UziWriteInstruction" :write-pin
    "UziTurnOnInstruction" :turn-on-pin
    "UziTurnOffInstruction" :turn-off-pin
    "UziPrimitiveCallInstruction" (keyword (str "prim/" (-> instr :argument :name)))))

; NOTE(Richo): This includes even older primitives that are no longer implemented
(def all-instructions [:jmp :jnz :jz :prim/abs :prim/add :prim/delayMs :prim/delayS
                       :prim/divide :prim/equals :prim/greaterThan :prim/greaterThanOrEquals
                       :prim/isEven :prim/isOff :prim/isOn :prim/jmp :prim/jz :prim/lessThan
                       :prim/lessThanOrEquals :prim/logicalAnd :prim/logicalOr :prim/multiply
                       :prim/noTone :prim/pop :prim/read :prim/remainder :prim/retv
                       :prim/servoWrite :prim/setServoDegrees :prim/subtract :prim/toggle
                       :prim/tone :prim/turnOff :prim/turnOn :prim/write :read-global
                       :read-local :script-call :write-global])

(defn instruction-freq []
  (merge (into {} (map #(vector % 0) all-instructions))
         (frequencies (map instruction-type
                           (mapcat (fn [{:keys [file]}]
                                     (try
                                       (let [program (uzi/compile-uzi-string (read-src file))]
                                         (program/instructions program))
                                       (catch Throwable ex
                                         (println "ERROR!" file))))
                                   phb-files)))))

(defn write-freq-file []
  (with-open [writer (io/writer (io/file "uzi_instruction_freq.csv"))]
    (csv/write-csv writer [["instruction" "count"]])
    (csv/write-csv writer (vec (reverse (sort-by second (instruction-freq)))))))


(def !instruction-sizes (atom {}))

(defn register-instruction-size! [instr bytes]
  (swap! !instruction-sizes update (instruction-type instr) conj (count bytes)))

(defn encode-instruction! [instr script program]
  (register-instruction-size! instr (en/encode-instruction* instr script program)))

(defn collect-instruction-sizes []
  (reset! !instruction-sizes {})
  (doseq [program (map (comp uzi/compile-uzi-string read-src :file)
                       phb-files)]
    (doseq [script (:scripts program)]
      (doseq [instr (:instructions script)]
        (encode-instruction! instr script program))))
  (into {}
        (map (fn [[k v]]
               [k [(frequencies v)
                   (float (/ (apply + v) (count v)))]])
             @!instruction-sizes)))

(defn write-instruction-sizes []
  (with-open [writer (io/writer (io/file "uzi_instruction_sizes.csv"))]
    (csv/write-csv writer [["instruction" "average-size (bytes)"]])
    (csv/write-csv writer (map (fn [[k [_ v]]] [k v])
                               (collect-instruction-sizes)))))

(comment  
  
  (declare v0)
  (declare v1)
  (count v0)
  (count v1)

  (with-open [writer (io/writer (io/file "freq_comparison.csv"))]
    (csv/write-csv writer [["instruction" "v0" "v1"]])
    (csv/write-csv writer
                   (mapv (fn [key]
                          [key
                           (get v0 key 0)
                           (get v1 key 0)])
                        (keys (merge v0 v1)))))
  
  
  (write-csv-file)
  (write-freq-file)
  (write-instruction-sizes)
  
  )