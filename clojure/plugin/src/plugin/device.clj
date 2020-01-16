(ns plugin.device
  (:refer-clojure :exclude [send])
  (:require [serial.core :as s]
            [serial.util :as su]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]]
            [plugin.utils.async :refer [<? <??]]
            [plugin.protocol :refer :all]
            [plugin.boards :refer :all]))

; TODO(Richo): Replace with log/error
(defn- error [msg] (println "ERROR:" msg))

(def initial-state {:port-name nil
                    :port nil
                    :reporting {:pins #{}
                                :globals #{}}
                    :pins {}})
(def state (atom initial-state))


(defn available-ports [] (su/get-port-names))

(defn connected? []
  (not (nil? (@state :port))))

(defmacro check-connection [then-form]
  `(if (connected?)
     ~then-form
     (error "The board is not connected!")))

(defn disconnect []
  (check-connection
   (let [port (@state :port)]
     (reset! state initial-state)
     (try
       (s/close! port)
       (catch Throwable e
         (error (str "ERROR WHILE DISCONNECTING -> " (.getMessage e))))))))

(defn send [bytes]
  (check-connection
   (try
     (s/write (@state :port) bytes)
     (catch Throwable e
       (error (str "ERROR WHILE SENDING -> " (.getMessage e)))
       (disconnect))))
  bytes)

(defn start-reporting [] (send [MSG_OUT_START_REPORTING]))
(defn stop-reporting [] (send [MSG_OUT_STOP_REPORTING]))

(defn set-pin-report [pin-name report?]
  (when-let [pin-number (get-pin-number pin-name)]
    (swap! state update-in [:reporting :pins]
           (if report? conj disj) pin-name)
    (send [MSG_OUT_SET_REPORT
           pin-number
           (if report? 1 0)])))

(defn send-pins-reporting []
  (let [pins (-> @state :reporting :pins)]
    (doseq [pin-name pins]
      (set-pin-report pin-name true))))

(defn- request-connection [port in]
  (<!! (timeout 2000)) ; NOTE(Richo): Needed in Mac
  (s/write port [MSG_OUT_CONNECTION_REQUEST
                 MAJOR_VERSION
                 MINOR_VERSION])
  (println "Requesting connection...")
  ;(<!! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
  (when-let [n1 (<?? in 1000)]
    (let [n2 (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256)]
      (s/write port n2)
      ;(<!! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
      (if (= n2 (<?? in 1000))
        (println "Connection accepted!")
        (println "Connection rejected")))))

(defn- keep-alive [port]
  (go-loop []
    (when (connected?)
      (send [MSG_OUT_KEEP_ALIVE])
      (<! (timeout 100))
      (recur))))

(defn- read-timestamp [in]
  (go
   (let [n1 (<? in)
         n2 (<? in)
         n3 (<? in)
         n4 (<? in)]
     (bit-or (bit-shift-left n1 24)
             (bit-shift-left n2 16)
             (bit-shift-left n3 8)
             n4))))

(defn- process-pin-value [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<? in)]
     (dotimes [_ count]
              (let [n1 (<? in)
                    n2 (<? in)
                    pin-number (bit-shift-right n1 2)
                    value (/ (bit-or n2
                                     (bit-shift-left (bit-and n1 2r11)
                                                     8))
                             1023)]
                (when-let [pin-name (get-pin-name pin-number)]
                  #_(println (str timestamp " -> " pin-name " : " value))
                  (swap! state update-in [:pins pin-name]
                         (fn [_] {:name pin-name
                                  :number pin-number
                                  :value value}))))))))

(defn- process-input [in]
  (go-loop []
    (when (connected?)
      (when-let [cmd (<? in)]
        (<! (condp = cmd
              MSG_IN_PIN_VALUE (process-pin-value in)
              (go (println "UNRECOGNIZED:" cmd)))))
      ;(swap! state assoc :a0 (<! in))
      (recur))))

(defn connect
  ([] (connect (first (available-ports))))
  ([port-name] (connect port-name 57600))
  ([port-name baud-rate]
   (if (connected?)
     (error "The board is already connected")
     (let [port (s/open port-name :baud-rate baud-rate)
           in (a/chan 1000)]
       (s/listen! port #(>!! in (.read %)))
       (request-connection port in)
       (swap! state assoc
              :port port
              :port-name port-name)
       (keep-alive port)
       (process-input in)
       (start-reporting)
       (send-pins-reporting)))))
