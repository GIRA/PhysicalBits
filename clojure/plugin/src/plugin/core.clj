(ns plugin.core
  (:require [serial.core :as s]
            [serial.util :refer :all])
  (:gen-class))

(def state (atom {:port nil}))

(defn- error [msg] (println "ERROR:" msg))

(defn connected? []
  (not (nil? (@state :port))))

(defmacro check-connection [then-form]
  `(if (connected?)
     ~then-form
     (error "The board is not connected!")))

(defn- process-input [input]
  (println "I:" (.read input)))

(defn connect [port-name baud-rate]
  (if (connected?)
    (error "The board is already connected")
    (let [port (s/open port-name :baud-rate baud-rate)]
      (s/listen! port process-input)
      (reset! state {:port port}))))

(defn disconnect []
  (check-connection
   (let [port (@state :port)]
     (reset! state {:port nil})
     (s/close! port))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
