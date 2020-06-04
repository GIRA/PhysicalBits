(ns middleware.output.logger
  (:refer-clojure :exclude [newline])
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def ^:private entries* (atom []))

(defn read-entries! []
  (let [[entries _] (reset-vals! entries* [])]
    (when-not (empty? entries)
      (log/info (format "Messages read from output logger: %d\n"
                        (count entries))
                (str/join "\n" (mapv str entries))))
    entries))

(defn- append [msg-type format-str args]
  (swap! entries*
         conj {:type msg-type
               :text format-str
               :args (mapv str args)}))

(defn info [str & args]
  (append :info str args))

(defn success [str & args]
  (append :success str args))

(defn warning [str & args]
  (append :warning str args))

(defn error [str & args]
  (append :error str args))

(defn newline []
  (info ""))

(def log info)
