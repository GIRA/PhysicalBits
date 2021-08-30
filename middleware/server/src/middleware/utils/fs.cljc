(ns middleware.utils.fs
  (:refer-clojure :exclude [read exists?])
  (:require #?(:clj [clojure.java.io :as io])
            #?(:cljs [middleware.utils.fs-macros :as m])))

(defn read [file]
  #?(:clj (slurp file)
     :cljs (m/read-file* (:path file) "../../uzi")))

(defn absolute-path [file]
  #?(:clj (.getAbsolutePath file)
     :cljs (:path file)))

(defn last-modified [file]
  #?(:clj (.lastModified file)
     :cljs (:last-modified file)))

(defn exists? [file]
  #?(:clj (.exists file)
     :cljs (:exists? file)))

(defn file [parent child]
  #?(:clj (io/file parent child)
     :cljs {:path (str (clojure.string/replace parent "\\" "/") "/" child),
            :exists? true, :last-modified (rand)}))
