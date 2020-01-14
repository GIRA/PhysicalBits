(ns plugin.core
  (:require [serial.core :as s]
            [serial.util :refer :all]
            [clojure.core.async :as a])
  (:gen-class))

(def default-state {:port nil :a0 0})
(def state (atom default-state))

(defn- error [msg] (println "ERROR:" msg))

(defn connected? []
  (not (nil? (@state :port))))

(defmacro check-connection [then-form]
  `(if (connected?)
     ~then-form
     (error "The board is not connected!")))

(defn- process-input [in]
  (a/go-loop []
    (when (connected?)
      (swap! state assoc :a0 (a/<! in))
      (recur))))

(defn connect [port-name baud-rate]
  (if (connected?)
    (error "The board is already connected")
    (let [port (s/open port-name :baud-rate baud-rate)
          in (a/chan 1000)]
      (s/listen! port #(a/>!! in (.read %)))
      (reset! state {:port port})
      (process-input in))))

(defn disconnect []
  (check-connection
   (let [port (@state :port)]
     (reset! state default-state)
     (s/close! port))))

(defn send [bytes]
  (check-connection (s/write (@state :port) bytes)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
