(ns middleware.utils.fs
  (:refer-clojure :exclude [read exists?])
  (:require #?(:clj [clojure.java.io :as io])))

(defn read [file]
  #?(:clj (slurp file)
     :cljs (throw (js/Error. "ACAACA read!"))))

(defn absolute-path [file]
  #?(:clj (.getAbsolutePath file)
     :cljs (throw (js/Error. "ACAACA absolute-path!"))))

(defn last-modified [file]
  #?(:clj (.lastModified file)
     :cljs (throw (js/Error. "ACAACA last-modified!"))))

(defn exists? [file]
  #?(:clj (.exists file)
     :cljs (throw (js/Error. "ACAACA exists?!"))))

(defn file [parent child]
  #?(:clj (io/file parent child)
     :cljs (throw (js/Error. "ACAACA file!"))))
