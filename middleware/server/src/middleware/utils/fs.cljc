(ns middleware.utils.fs
  (:refer-clojure :exclude [read exists?])
  (:require #?(:clj [clojure.java.io :as io]
               :cljs [ajax.core :refer [GET]])))

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

(comment
 (js/alert "Richo")

 (require '[ajax.core :refer [GET]])

 (defn handler [response]
   (.log js/console (str response)))

 (defn error-handler [{:keys [status status-text]}]
   (.log js/console (str "something bad happened: " status " " status-text)))

 (GET "http://localhost:8080/core.uzi"
      {:handler handler
       :error-handler error-handler})


 (GET "http://localhost:8080/core.uzi"
      {:handler handler
       :error-handler error-handler})
 ,)
