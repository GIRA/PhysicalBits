(ns plugin.device
  (:require [serial.core :as s]
            [serial.util :as su]
            [clojure.core.async :as a]))


(def MSG_OUT_CONNECTION_REQUEST 255)
(def MSG_OUT_KEEP_ALIVE 7)
(def MAJOR_VERSION 0)
(def MINOR_VERSION 7)


; TODO(Richo): Replace with log/error
(defn- error [msg] (println "ERROR:" msg))

(def default-state {:port-name nil
                    :port nil
                    :a0 0})
(def state (atom default-state))


(defn available-ports [] (su/get-port-names))

(defn connected? []
  (not (nil? (@state :port))))

(defmacro check-connection [then-form]
  `(if (connected?)
     ~then-form
     (error "The board is not connected!")))


(defmacro <? [chan timeout]
  `(first (a/alts! [~chan (a/timeout ~timeout)])))
(defmacro <?? [chan timeout]
  `(first (a/alts!! [~chan (a/timeout ~timeout)])))

(defn- request-connection [port in]
  (a/<!! (a/timeout 2000)) ; NOTE(Richo): Needed in Mac
  (s/write port [MSG_OUT_CONNECTION_REQUEST
                 MAJOR_VERSION
                 MINOR_VERSION])

  (println "Requesting connection...")
  ;(a/<!! (a/timeout 500)) ; TODO(Richo): Not needed in Mac

  (when-let [n1 (<?? in 1000)]
    (let [n2 (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256)]
      (s/write port n2)
      ;(a/<!! (a/timeout 500)) ; TODO(Richo): Not needed in Mac
      (if (= n2 (<?? in 1000))
        (println "Connection accepted!")
        (println "Connection rejected")))))

(defn- keep-alive [port]
  (a/go-loop []
    (when (connected?)
      (s/write port [MSG_OUT_KEEP_ALIVE])
      (a/<! (a/timeout 100))
      (recur))))

(defn- process-input [in]
  (a/go-loop []
    (when (connected?)
      (when-let [cmd (<? in 1000)]
        (println "IN:" cmd))
      ;(swap! state assoc :a0 (a/<! in))
      (recur))))

(defn connect
  ([] (connect (first (available-ports))))
  ([port-name] (connect port-name 57600))
  ([port-name baud-rate]
   (if (connected?)
     (error "The board is already connected")
     (let [port (s/open port-name :baud-rate baud-rate)
           in (a/chan 1000)]
       (s/listen! port #(a/>!! in (.read %)))
       (request-connection port in)
       (keep-alive port)
       (swap! state assoc
              :port port
              :port-name port-name)
       (process-input in)))))

(defn disconnect []
  (check-connection
   (let [port (@state :port)]
     (reset! state default-state)
     (s/close! port))))

(defn send [bytes]
  (check-connection (s/write (@state :port) bytes)))
