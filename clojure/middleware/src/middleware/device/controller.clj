(ns middleware.device.controller
  (:refer-clojure :exclude [send])
  (:require [serial.core :as s]
            [serial.util :as su]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]]
            [middleware.utils.async :refer :all]
            [middleware.device.protocol :refer :all]
            [middleware.device.boards :refer :all]
            [middleware.utils.conversions :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]))

; TODO(Richo): Replace with log/error
(defn- error [msg] (println "ERROR:" msg))

(def initial-state {:port-name nil
                    :port nil
                    :reporting {:pins #{}
                                :globals #{}}
                    :pins {}
                    :globals {}
                    :scripts []
                    :profiler nil
                    :debugger nil
                    :running-program nil})
(def state (atom initial-state)) ; TODO(Richo): Make it survive reloads

(def events (a/chan))
(def events-pub (a/pub events :type))
(def event-loop? (atom false))

; TODO(Richo): Check if this is not computationally expensive
(defn start-event-loop []
  (when (compare-and-set! event-loop? false true)
    (go-loop [old-state @state]
      (a/timeout 100)
      (let [new-state @state]
        (when (not= old-state new-state)
          (>! events {:type :update, :state (dissoc new-state :port)}))
        (when @event-loop?
          (recur new-state))))))

(defn stop-event-loop []
  (reset! event-loop? false))

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

(defn compile-uzi-string [src]
  (let [program (cc/compile-uzi-string src)
        bytecodes (en/encode program)]
    (swap! state assoc :current-program program)
    program))

(defn run [program]
  (swap! state assoc :running-program program)
  (let [bytecodes (en/encode program)]
    (send (concat [MSG_OUT_SET_PROGRAM] bytecodes))))

(defn start-reporting [] (send [MSG_OUT_START_REPORTING]))
(defn stop-reporting [] (send [MSG_OUT_STOP_REPORTING]))

(defn start-profiling [] (send [MSG_OUT_PROFILE 1]))
(defn stop-profiling [] (send [MSG_OUT_PROFILE 0]))

(defn set-all-breakpoings [] (send [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 1]))
(defn clear-all-breakpoings [] (send [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 0]))

(defn send-continue []
  (swap! state assoc :debugger nil)
  (send [MSG_OUT_DEBUG_CONTINUE]))

(defn set-global-report [global-number report?]
  (swap! state update-in [:reporting :globals]
         (if report? conj disj) global-number)
  (send [MSG_OUT_SET_GLOBAL_REPORT
         global-number
         (if report? 1 0)]))

(defn set-pin-value [pin-name value]
  (let [pin-number (get-pin-number pin-name)]
    (send [MSG_OUT_SET_VALUE
           pin-number
           (Math/round (* value 255.0))])))

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

(defn- read-uint32 [in]
  (go (bytes->uint32 (<! (read-vec? 4 in)))))

(def read-timestamp read-uint32)

(defn- read-uint16 [in]
  (go (bytes->uint16 (<! (read-vec? 2 in)))))

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
  (let [running? (> (bit-and 2r10000000 byte) 0)
        error-code (bit-and 2r01111111 byte)
        error-msg (error-msg error-code)]
    {:index i
     :running? running?
     :error-code error-code
     :error-msg error-msg}))

(defn- process-running-scripts [in]
  (go
   (let [count (<? in)
         values (map-indexed process-script-state
                             (<! (read-vec? count in)))]
     (swap! state assoc
            :scripts values))))

(defn- process-profile [in]
  (go (let [n1 (<? in)
            n2 (<? in)
            value (bit-or n2
                          (bit-shift-left n1 7))]
        (swap! state assoc
               :profiler {:ticks value
                          :interval-ms 100}))))

(defn- process-coroutine-state [in]
  (go (let [index (<? in)
            pc (<! (read-uint16 in))
            fp (<? in)
            stack-size (<? in)
            stack (<! (read-vec? (* 4 stack-size) in))]
        (swap! state assoc
               :debugger {:index index
                          :pc pc
                          :fp fp
                          :stack stack}))))

(defn- process-error [in]
  (go (let [error-code (<? in)]
        (when (> error-code 0)
          (error (error-msg error-code)
                 " has been detected. The program has been stopped")
          (disconnect)))))

(defn- process-trace [in]
  (go (let [count (<? in)
            msg (new String (byte-array (<! (read-vec? count in))) "UTF-8")]
        (println "TRACE:" msg))))

(defn- process-serial-tunnel [in]
  (go (println "SERIAL:" (<? in))))

(defn- process-input [in]
  (go-loop []
    (when (connected?)
      (when-let [cmd (<? in)]
        (<! (condp = cmd
              MSG_IN_PIN_VALUE (process-pin-value in)
              MSG_IN_GLOBAL_VALUE (process-global-value in)
              MSG_IN_RUNNING_SCRIPTS (process-running-scripts in)
              MSG_IN_FREE_RAM (process-free-ram in)
              MSG_IN_PROFILE (process-profile in)
              MSG_IN_COROUTINE_STATE (process-coroutine-state in)
              MSG_IN_ERROR (process-error in)
              MSG_IN_TRACE (process-trace in)
              MSG_IN_SERIAL_TUNNEL (process-serial-tunnel in)
              (go (println "UNRECOGNIZED:" cmd)))))
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


#_(

   (def monitoring? true)
   (go-loop [old-state @state]
     (let [new-state @state]
       (when (not= old-state new-state)
         (a/>!! events {:type :update
                        :state new-state
                        :diff (clojure.data/diff old-state new-state)}))
       (if monitoring? (recur new-state))))

   (def listener (a/chan))
   (def listening? true)
   (def listening? false)
   (go-loop [i 0]
     (println i)
     (println (:diff (<!! listener)))
     (println "==")
     (if listening? (recur (inc i))))


   (a/sub events-pub :update listener)

    (connect "/dev/tty.usbmodem14101")
   (set-pin-report "D13" 1)
   (run (compile-uzi-string "task blink13() running 1/s { toggle(D13); }"))
   (disconnect)

   (def monitoring? false)













   )
