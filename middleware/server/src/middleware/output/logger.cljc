(ns middleware.output.logger
  (:refer-clojure :exclude [newline])
  (:require #?(:clj [clojure.tools.logging :as log])
            [clojure.string :as str]))

(def ^:private entries* (atom []))

(defn- uzi-format [text args]
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

(defn read-entries! []
  (let [[entries _] (reset-vals! entries* [])]
    (when-not (empty? entries)
      (doseq [{:keys [text args]} entries]
        (log* (uzi-format text args))))
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
