(ns user
  (:require [clojure.java.io :as io]))

(defn reset [])

(defn copy-output
  {:shadow.build/stage :flush}
  [build-state & [to-dir]]
  (when (= (:shadow.build/mode build-state) :release)
    (when-let [from-dir (-> build-state :shadow.build/config :output-dir)]
      (doseq [from (->> (.listFiles (io/file from-dir))
                        (filter (memfn isFile)))]
        (let [to (io/file to-dir (.getName from))]
          (println "Copying output to" (.getPath to))
          (io/copy from to)))))
  build-state)