(ns middleware.utils.fs.node
  (:require [middleware.utils.fs.common :as common]
            ["path" :as path]
            ["fs" :as fs]))

(deftype NodeFile [path]
  common/File
  (read [file] (str (fs/readFileSync (.-path file))))
  (absolute-path [file] (path/resolve (.-path file)))
  (last-modified [file]
                 (if-let [stats (fs/lstatSync (.-path file)
                                              #js{:throwIfNoEntry false})]
                   (unchecked-int (.-mtimeMs stats))
                   0))
  (exists? [file] (fs/existsSync (.-path file))))

(defn file [parent child]
  (->NodeFile (.join path parent child)))

(defn available? []
  (some? fs/readFileSync))
