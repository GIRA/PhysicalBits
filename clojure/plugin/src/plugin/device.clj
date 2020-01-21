(ns plugin.device
  (:refer-clojure :exclude [send])
  (:require [serial.core :as s]
            [serial.util :as su]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]]
            [plugin.utils.async :refer :all]
            [plugin.protocol :refer :all]
            [plugin.boards :refer :all]))

; TODO(Richo): Replace with log/error
(defn- error [msg] (println "ERROR:" msg))

(def initial-state {:port-name nil
                    :port nil
                    :reporting {:pins #{}
                                :globals #{}}
                    :pins {}
                    :globals {}})
(def state (atom initial-state))

(defn get-pin-value [pin-name]
  (-> @state :pins (get pin-name) :value))

(defn available-ports [] (su/get-port-names))

(defn connected? []
  (not (nil? (@state :port))))

(defn disconnect []
  (when-let [port (@state :port)]
    (reset! state initial-state)
    (try
      (s/close! port)
      (catch Throwable e
        (error (str "ERROR WHILE DISCONNECTING -> " (.getMessage e)))))))

(defn send [bytes]
  (when-let [port (@state :port)]
    (try
      (s/write port bytes)
      (catch Throwable e
        (error (str "ERROR WHILE SENDING -> " (.getMessage e)))
        (disconnect))))
  bytes)

(defn start-reporting [] (send [MSG_OUT_START_REPORTING]))
(defn stop-reporting [] (send [MSG_OUT_STOP_REPORTING]))

(defn set-global-report [global-number report?]
  (swap! state update-in [:reporting :globals]
         (if report? conj disj) global-number)
  (send [MSG_OUT_SET_GLOBAL_REPORT
         global-number
         (if report? 1 0)]))


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

(defn- bytes->uint32 [[n1 n2 n3 n4]]
  (bit-or (bit-shift-left n1 24)
          (bit-shift-left n2 16)
          (bit-shift-left n3 8)
          n4))

(defn- uint32->float [uint32]
  (Float/intBitsToFloat (unchecked-int uint32)))

(defn- bytes->float [bytes]
  (uint32->float (bytes->uint32 bytes)))

(defn- read-uint32 [in]
  (go (bytes->uint32 (<! (next-n 4 in)))))

(def read-timestamp read-uint32)

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
                             1023.0)]
                (when-let [pin-name (get-pin-name pin-number)]
                  #_(println (str timestamp " -> " pin-name " : " value))
                  (swap! state update-in [:pins pin-name]
                         (fn [_] {:name pin-name
                                  :number pin-number
                                  :value value}))))))))

(defn- process-global-value [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<? in)]
     (dotimes [_ count]
              (let [number (<? in)
                    n1 (<? in)
                    n2 (<? in)
                    n3 (<? in)
                    n4 (<? in)
                    float-value (bytes->float [n1 n2 n3 n4])]
                (swap! state update-in [:globals number]
                       (fn [_] {:name "?"
                                :number number
                                :value float-value
                                :raw-bytes [n1 n2 n3 n4]})))))))

(defn- process-free-ram [in]
  (go (let [arduino (<! (read-uint32 in))
            uzi (<! (read-uint32 in))]
        (swap! state update-in [:memory]
               (fn [_] {:arduino arduino
                        :uzi uzi})))))

(defn- process-script-state [i byte]
  {:index i
   :running? (> (bit-and 2r10000000 byte) 0)
   :error-code (bit-and 2r01111111 byte)})

(defn- process-running-scripts [in]
  (go
   (let [count (<? in)
         values (map-indexed process-script-state
                             (<! (next-n count in)))]
     (swap! state assoc
            :scripts values))))

(defn- process-input [in]
  (go-loop []
    (when (connected?)
      (when-let [cmd (<? in)]
        (<! (condp = cmd
              MSG_IN_PIN_VALUE (process-pin-value in)
              MSG_IN_GLOBAL_VALUE (process-global-value in)
              MSG_IN_RUNNING_SCRIPTS (process-running-scripts in)
              MSG_IN_FREE_RAM (process-free-ram in)
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
