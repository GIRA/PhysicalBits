(ns middleware.output.logger
  (:refer-clojure :exclude [newline])
  (:require [clojure.tools.logging :as log]
            [clojure.string :as str]))

(def ^:private entries* (atom []))

(defn- uzi-format [text args]
  (loop [t text, i 0]
    (if-let [val (get args i)]
      (recur
        (str/replace t (str "%" (inc i)) val)
        (inc i))
      t)))

(defn read-entries! []
  (let [[entries _] (reset-vals! entries* [])]
    (when-not (empty? entries)
      (doseq [entry entries]
        (log/info (uzi-format (:text entry) (:args entry)))))
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
