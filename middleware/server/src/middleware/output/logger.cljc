(ns middleware.output.logger
  (:refer-clojure :exclude [newline])
  (:require #?(:clj [clojure.tools.logging :as log])
            [clojure.core.async :as a]
            [clojure.string :as str]))

(def update-chan (a/chan))
(def updates (a/mult update-chan))

(defn- uzi-format [{:keys [text args]}]
  (loop [t text, i 0]
    (if-let [val (get args i)]
      (recur
        (str/replace t (str "%" (inc i)) val)
        (inc i))
      t)))

; TODO(Richo): Find a cross-platform way of logging...
(defn log* [str]
  #?(:clj (log/info str)
     :cljs (println str)))

(defn- append [msg-type format-str args]
  (let [entry {:type msg-type
               :text format-str
               :args (mapv str args)}]
    (a/put! update-chan entry)
    (log* (uzi-format entry))))

(defn info [str & args]
  (append :info str args))

(defn success [str & args]
  (append :success str args))

(defn warning [str & args]
  (append :warning str args))

(defn error [str & args]
  (append :error str args))

(defn exception [ex]
  (error (ex-message ex)))

(defn newline []
  (info ""))

(def log info)
