(ns middleware.utils.fs.browser
  (:require [middleware.utils.fs.common :as common]
            [middleware.utils.fs.browser-macros :as m]
            [clojure.string :as str]))

(defn- read-file [path]
  (m/read-file* path "../../uzi"))

(deftype BrowserFile [path]
  common/File
  (read [file] (read-file (.-path file)))
  (write [file data] (throw (ex-info "Not supported!" {})))
  (absolute-path [file] (.-path file))
  (last-modified [file] 1)
  (exists? [file] (some? (read-file (.-path file)))))

(defn file [parent child]
  (->BrowserFile (str (str/replace parent "\\" "/") "/" child)))
