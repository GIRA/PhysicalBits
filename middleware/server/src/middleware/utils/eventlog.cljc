(ns middleware.utils.eventlog
  (:require #?(:clj [clojure.java.io :as io])
            [clojure.string :as str]
            [middleware.utils.core :refer [parse-int]]))

(def ^:const FILE_SIZE_LIMIT (* 1024 1024))

(def file-name (atom nil))

(defn today []
  #?(:clj (java.time.LocalDate/now)))

(defn events-folder []
  #?(:clj (io/file "events")))

(defn file-size [fname]
  #?(:clj (.length (io/file "events" fname))))

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

(defn append [str]
  #?(:clj (spit (io/file "events" (find-log-file))
                str :append true)))

(comment

  (require '[clojure.core.async :as a :refer [<! go]])

  (def stop (atom false))
  
  (go (loop [i 0]
        (when-not @stop
          (append (str "RICHO " i "\n"))
          (when (= 0 (mod i 1000))
            (<! (a/timeout 10)))
          (recur (inc i))))
      (println "BYE"))
  
  (reset! stop true)
  )