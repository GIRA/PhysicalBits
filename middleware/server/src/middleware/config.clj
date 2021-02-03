(ns middleware.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]))

(def PATH "config.edn")
(def cache (atom {}))

(defn- read-file [file]
  (try
    (log/info "Reading config file:" (.getAbsolutePath file))
    (slurp file)
    (catch java.io.FileNotFoundException _
      (log/error "File not found:" (.getAbsolutePath file))
      nil)
    (catch Throwable _ nil)))

(defn- read-config* [file]
  (let [path (.getAbsolutePath file)
        last-modified (.lastModified file)
        entry (get @cache path)]
    (if (= last-modified
           (get entry :last-modified))
      (get entry :content)
      (let [content (edn/read-string (read-file file))]
        (swap! cache assoc path {:last-modified last-modified
                                 :content content})
        content))))

(defn read-config
  ([] (read-config PATH))
  ([path] (read-config* (io/file path))))

(defn get-config [key default-value]
  (get (read-config) key default-value))

(comment
 (.lastModified (io/file "RICHO"))
 (read-config)
 (get @cache :last-modified)
 (slurp "RICHO")
 (get (read-config "RICHO2") :report-interval 45)
@cache
 ,)
