(ns middleware.config
  (:refer-clojure :exclude [get])
  (:require [clojure.core :as clj-core]
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
        entry (clj-core/get @cache path)]
    (if (= last-modified
           (clj-core/get entry :last-modified))
      (clj-core/get entry :content)
      (let [content (edn/read-string (read-file file))]
        (swap! cache assoc path {:last-modified last-modified
                                 :content content})
        content))))

(defn get-all
  ([] (get-all PATH))
  ([path] (read-config (io/file path))))

(defn get [key default-value]
  (clj-core/get (get-all) key default-value))
