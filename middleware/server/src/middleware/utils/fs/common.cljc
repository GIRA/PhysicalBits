(ns middleware.utils.fs.common
  (:refer-clojure :exclude [read exists?]))

(defprotocol File
  (read [this])
  (absolute-path [this])
  (last-modified [this])
  (exists? [this]))

(def ^:private current-fs
  (atom (fn [& args] (throw (ex-info "NO FS SET!" {})))))

(defn register-fs! [f]
  (reset! current-fs f))

(defn file [parent child]
  (@current-fs parent child))
