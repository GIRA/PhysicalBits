(ns middleware.config
  (:refer-clojure :exclude [get get-in])
  (:require [clojure.core :as clj]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]))

(def ^:private PATH "config.edn")
(def ^:private cache (atom {}))

(defn- read-file [^java.io.File file]
  (try
    (log/info "Reading config file:" (.getAbsolutePath file))
    (slurp file)
    (catch java.io.FileNotFoundException _
      (log/error "File not found:" (.getAbsolutePath file))
      nil)
    (catch Throwable _ nil)))

(defn- read-config [^java.io.File file]
  (let [path (.getAbsolutePath file)
        last-modified (.lastModified file)
        entry (clj/get @cache path)]
    (if (= last-modified
           (clj/get entry :last-modified))
      (clj/get entry :content)
      (let [content (edn/read-string (read-file file))]
        (swap! cache assoc path {:last-modified last-modified
                                 :content content})
        content))))

(defn get-all
  ([] (get-all PATH))
  ([path] (read-config (io/file path))))

(defn get [key default-value]
  (clj/get (get-all) key default-value))

(defn get-in [keys default-value]
  (clj/get-in (get-all) keys default-value))
