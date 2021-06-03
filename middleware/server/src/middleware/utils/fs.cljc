(ns middleware.utils.fs
  (:refer-clojure :exclude [read exists?])
  (:require #?(:clj [clojure.java.io :as io]
               :cljs [ajax.core :refer [GET]])
            #?(:cljs [middleware.utils.fs-macros :as m])))

(defn read [file]
  #?(:clj (slurp file)
     :cljs (m/read-file* (:path file)) #_(throw (js/Error. "ACAACA read!"))))

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
