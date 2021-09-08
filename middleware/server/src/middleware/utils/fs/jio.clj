(ns middleware.utils.fs.jio
  (:require [middleware.utils.fs.common :as common]
            [clojure.java.io :as io]))

(extend-type java.io.File
  common/File
  (read [file] (slurp file))
  (write [file data] (spit file data))
  (absolute-path [file] (.getAbsolutePath file))
  (last-modified [file] (.lastModified file))
  (exists? [file] (.exists file)))

(defn file [parent child] (io/file parent child))
