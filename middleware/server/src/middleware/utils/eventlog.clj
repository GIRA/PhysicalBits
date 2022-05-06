(ns middleware.utils.eventlog
  (:require [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as str]
            [middleware.utils.core :refer [parse-int random-uuid]]))

(def pc-id (try
             (slurp "pc.uuid")
             (catch java.lang.Throwable _
               (let [id (random-uuid)]
                 (try (spit "pc.uuid" id)
                      (catch java.lang.Throwable _))
                 (str id)))))

(def session-id (str (random-uuid)))

(def FILE_SIZE_LIMIT (* 100 1024 1024))

(def file-name (atom nil))

(defn today []
  (java.time.LocalDate/now))

(defn now []
  (str (java.time.Instant/now)))

(defn events-folder []
  (io/file "events"))

(defn file-size [fname]
  (.length (io/file "events" fname)))

(defn inc-counter [fname]
  (let [[d c e] (str/split fname #"\.")]
    (str d "." (inc (parse-int c)) "." e)))

(defn find-log-file 
  ([] (swap! file-name find-log-file))
  ([name]
   (if name
     (if (< (file-size name) FILE_SIZE_LIMIT)
       name
       (inc-counter name))
     (let [date (today)
           pattern (re-pattern (str date "\\.\\d+\\.csv"))
           last (some->> (events-folder)
                         (.list)
                         (filter #(re-find pattern %))
                         (sort-by #(parse-int (second (str/split % #"\."))))
                         (last))]
       (if last
         (inc-counter last)
         (str date ".1.csv"))))))

(defn append [evt-type & [evt-data]]
  (try
    (let [file (io/file "events" (find-log-file))
          data [pc-id session-id (now) evt-type evt-data]]
      ; TODO(Richo): Optimization. Reuse the writer until file changes
      (with-open [writer (io/writer file :append true)]
        (csv/write-csv writer [data])))
    (catch java.lang.Throwable ex
      (println "ERROR WHILE WRITING TO EVENT LOG ->" ex))))


(comment

  (def w (io/writer "foo.txt" :append true))
  (time (csv/write-csv w [[pc-id session-id (now) "RICHO" nil]]))

  (.close w)
  
  (time (append "RICHO"  #_{:a 1 :b 2 :c "Richo, capo"}))

  (require '[clojure.core.async :as a :refer [<! go]])

  (def stop (atom false))
  (go (loop [i 0]
        (when-not @stop
          (append "RICHO" i)
          (when (= 0 (mod i 1000))
            (<! (a/timeout 10)))
          (recur (inc i))))
      (println "BYE"))

  (reset! stop true)


  )