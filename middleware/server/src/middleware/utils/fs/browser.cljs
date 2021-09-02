(ns middleware.utils.fs.browser
  (:require [middleware.utils.fs.common :as common]
            [middleware.utils.fs.browser-macros :as m]
            [clojure.string :as str]))

(deftype BrowserFile [path]
  common/File
  (read [file] (m/read-file* (.-path file) "../../uzi"))
  (write [file data] (throw (ex-info "Not supported!" {})))
  (absolute-path [file] (.-path file))
  (last-modified [file] 1)
  (exists? [file] true))

(defn file [parent child]
  (->BrowserFile (str (str/replace parent "\\" "/") "/" child)))
