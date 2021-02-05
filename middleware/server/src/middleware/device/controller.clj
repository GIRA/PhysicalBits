(ns middleware.device.controller
  (:refer-clojure :exclude [send compile])
  (:require [clojure.tools.logging :as log]
            [serial.core :as s]
            [serial.util :as su]
            [clojure.string :as str]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]]
            [middleware.utils.async :refer :all]
            [middleware.device.protocol :refer :all]
            [middleware.device.boards :refer :all]
            [middleware.utils.conversions :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.compiler.utils.ast :as ast]
            [middleware.compiler.utils.program :as program]
            [middleware.output.logger :as logger]
            [middleware.config :as cfg]
            [middleware.device.utils.ring-buffer :as rb])
  (:import (java.net Socket)))

#_(

(start-profiling)
(stop-profiling)

(-> @state :profiler :ticks (* 10))

(-> @state :globals)
(-> @state :pins)
(-> @state :reporting)
(rb/avg (-> @state :timing :diffs))
(millis)
(< (get-in @state [:pseudo-vars :data "juan" :ts])
   (- (-> @state :pseudo-vars :timestamp) 1000))

(millis)
(add-pseudo-var! "richo" 42)

(-> @state :timing)
(swap! state update :timing
       #(assoc %
               :arduino 1
               :middleware 2))
(or (get (get @state :timing) :arduino2) 1)
)

(defn millis [] (System/currentTimeMillis))
(defn constrain [val lower upper] (max lower (min upper val)))

(defprotocol UziPort
  (close! [this])
  (write! [this data])
  (listen! [this listener-fn]))

(extend-type serial.core.Port
  UziPort
  (close! [port] (s/close! port))
  (write! [port data] (s/write port data))
  (listen! [port listener-fn] (s/listen! port #(listener-fn (.read %)))))

(extend-type java.net.Socket
  UziPort
  (close! [socket] (.close socket))
  (write! [socket data]
          (let [out (.getOutputStream socket)
                bytes (if (number? data) [data] data)]
            (.write out (byte-array bytes))))
  (listen! [socket listener-fn]
           (let [buffer-size 1000
                 buffer (byte-array buffer-size)
                 in (.getInputStream socket)]
             (go-loop []
               (when-not (.isClosed socket)
                 (let [bytes-read (.read in buffer 0 buffer-size)]
                   (dotimes [i bytes-read]
                            (listener-fn (bit-and (int (nth buffer i)) 16rFF))))
                 (recur))))))

(def initial-state {:port-name nil
                    :port nil
                    :connected? false
                    :board UNO
                    :reporting {:pins #{}, :globals #{}}
                    :pins {}
                    :globals {}
                    :pseudo-vars {:timestamp nil, :data {}}
                    :scripts []
                    :profiler nil
                    :debugger nil
                    :timing {:arduino nil, :middleware nil, :diffs nil}
                    :memory {:arduino nil, :uzi nil}
                    :program {:current nil, :running nil}
                    :available-ports []})
(def state (atom initial-state)) ; TODO(Richo): Make it survive reloads

(defn- add-pseudo-var! [name value]
  (if (cfg/get-config :pseudo-vars? false)
    (let [now (or (-> @state :timing :arduino) 0)]
      (swap! state
             #(-> %
                  (assoc-in [:pseudo-vars :timestamp] now)
                  (assoc-in [:pseudo-vars :data name]
                            {:name name, :value value, :ts now}))))))

(defn get-pin-value [pin-name]
  (-> @state :pins (get pin-name) :value))

(defn available-ports []
  (vec (sort (su/get-port-names))))

(defn connected? []
  (not (nil? (:port @state))))

(defn disconnect []
  (when-let [port (@state :port)]
    (swap! state
           #(-> initial-state
                ; Keep the current program
                (assoc-in [:program :current]
                           (-> % :program :current))))
    (try
      (close! port)
      (<!! (timeout 1000)) ; TODO(Richo): Wait a second to stabilize port (?)
      (catch Throwable e
        (log/error (str "ERROR WHILE DISCONNECTING -> " (.getMessage e)))))
    (logger/error "Connection lost!")))

(defn send [bytes]
  (when-let [port (@state :port)]
    (try
      (write! port bytes)
      (catch Throwable e
        (log/error (str "ERROR WHILE SENDING -> " (.getMessage e)))
        (disconnect))))
  bytes)


(defn- get-global-number [global-name]
  (program/index-of-variable (-> @state :program :running :compiled)
                             global-name
                             nil))

(defn set-global-report [global-name report?]
  (when-let [global-number (get-global-number global-name)]
    (swap! state update-in [:reporting :globals]
           (if report? conj disj) global-name)
    (send [MSG_OUT_SET_GLOBAL_REPORT
           global-number
           (if report? 1 0)])))

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

(defn compile [src type silent? & args]
  (try
    (let [compile-fn (case type
                       "json" cc/compile-json-string
                       "uzi" cc/compile-uzi-string)
          program (-> (apply compile-fn src args)
                      (update :compiled program/sort-globals)
                      (assoc :type type))
          bytecodes (en/encode (:compiled program))]
      (when-not silent?
        (logger/newline)
        (logger/log "Program size (bytes): %1" (count bytecodes))
        (logger/log (str bytecodes))
        (logger/success "Compilation successful!"))
      (swap! state assoc-in [:program :current] program)
      program)
    (catch Throwable ex
      (when-not silent?
        (logger/newline)
        (logger/error (.getMessage ex))
        ; TODO(Richo): Improve the error message for checker errors
        (when-let [{errors :errors} (ex-data ex)]
          (doseq [{:keys [description node]} errors]
            (logger/error description)
            (logger/error (str node)))))
      (throw ex))))

(defn- update-reporting [program]
  "All pins and globals referenced in the program must be enabled"
  (doseq [global (filter :name (-> program :compiled :globals))]
    (set-global-report (:name global) true))
  (doseq [{:keys [type number]} (filter ast/pin-literal? (-> program :final-ast ast/all-children))]
    (set-pin-report (str type number) true)))

(defn run [program]
  (swap! state assoc-in [:reporting :globals] #{})
  (swap! state assoc-in [:program :running] program)
  (let [bytecodes (en/encode (:compiled program))
        msb (bit-shift-right (bit-and (count bytecodes)
                                      16rFF00)
                             8)
        lsb (bit-and (count bytecodes)
                     16rFF)
        sent (send (concat [MSG_OUT_SET_PROGRAM msb lsb] bytecodes))]
    (update-reporting program)
    sent))

(defn install [program]
  ; TODO(Richo): Should I store the installed program?
  (let [bytecodes (en/encode (:compiled program))
        msb (bit-shift-right (bit-and (count bytecodes)
                                      16rFF00)
                             8)
        lsb (bit-and (count bytecodes)
                     16rFF)]
    (send (concat [MSG_OUT_SAVE_PROGRAM msb lsb] bytecodes))
    (logger/success "Installed program successfully!")))

(defn start-reporting [] (send [MSG_OUT_START_REPORTING]))
(defn stop-reporting [] (send [MSG_OUT_STOP_REPORTING]))

(defn start-profiling [] (send [MSG_OUT_PROFILE 1]))
(defn stop-profiling [] (send [MSG_OUT_PROFILE 0]))

(defn set-report-interval [interval]
  (let [interval (int (constrain interval
                                 (cfg/get-config :report-interval-min 0)
                                 (cfg/get-config :report-interval-max 100)))]
    (when-not (= (-> @state :reporting :interval)
                 interval)
      (swap! state assoc-in [:reporting :interval] interval)
      ;(logger/warning "Setting report interval: %1" interval)
      (send [MSG_OUT_SET_REPORT_INTERVAL interval]))))

(defn set-all-breakpoings [] (send [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 1]))
(defn clear-all-breakpoings [] (send [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 0]))

(defn send-continue []
  (swap! state assoc :debugger nil)
  (send [MSG_OUT_DEBUG_CONTINUE]))

(defn- request-connection [port in]
  (go
   (<! (timeout 2000)) ; NOTE(Richo): Needed in Mac
   (write! port [MSG_OUT_CONNECTION_REQUEST
                  MAJOR_VERSION
                  MINOR_VERSION])
   (logger/log "Requesting connection...")
   ;(<! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
   (if-let [n1 (<? in 1000)]
     (let [n2 (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256)]
       (write! port n2)
       ;(<! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
       (if (= n2 (<? in 1000))
         (do
           (logger/success "Connection accepted!")
           true)
         (do
           (logger/error "Connection rejected")
           false)))
     (do
       (logger/error "Connection timeout")
       false))))

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

(defn- process-timestamp [timestamp]
  "Calculating the time since the last snapshot (both in the vm and the middleware)
  and then calculating the difference between these intervals and adding it as
  pseudo variable so that I can observe it in the inspector. Its value should always
  be close to 0. If not, we try increasing the report interval."
  (let [arduino-time timestamp
        middleware-time (millis)
        timing (-> @state :timing)
        delta-arduino (- arduino-time
                         (or (get timing :arduino) arduino-time))
        delta-middleware (- middleware-time
                            (or (get timing :middleware) middleware-time))
        delta (- delta-arduino delta-middleware)
        timing-diffs (:diffs timing)]
    (rb/push! timing-diffs delta)
    (swap! state update :timing
           #(assoc %
                   :arduino arduino-time
                   :middleware middleware-time))
    (add-pseudo-var! "__delta" delta)
    (let [report-interval (-> @state :reporting :interval)
          report-interval-inc (cfg/get-config :report-interval-inc 1)
          delta-smooth (Math/abs (rb/avg timing-diffs))
          delta-threshold-min (cfg/get-config :delta-threshold-min 1)
          delta-threshold-max (cfg/get-config :delta-threshold-max 5)]
      (add-pseudo-var! "__delta_smooth" delta-smooth)
      (add-pseudo-var! "__report_interval" report-interval)
      ; If the delta-smooth goes below the min we decrement the report-interval
      (when (< delta-smooth delta-threshold-min)
        (set-report-interval (- report-interval report-interval-inc)))
      ; If the delta-smooth goes above the max we increment the report-interval
      (when (> delta-smooth delta-threshold-max)
        (set-report-interval (+ report-interval report-interval-inc))))))

(defn- process-pin-value [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<? in)
         snapshot (atom {:timestamp timestamp, :data {}})]
     (dotimes [_ count]
              (let [n1 (<? in)
                    n2 (<? in)
                    pin-number (bit-shift-right n1 2)
                    value (/ (bit-or n2
                                     (bit-shift-left (bit-and n1 2r11)
                                                     8))
                             1023.0)]
                (when-let [pin-name (get-pin-name pin-number)]
                  (swap! snapshot assoc-in [:data pin-name]
                         {:name pin-name
                          :number pin-number
                          :value value}))))
     (swap! state assoc :pins @snapshot)
     (process-timestamp timestamp))))

(defn- process-global-value [in]
  (go ;(<! (timeout 100)) ; Just for testing...
      (let [timestamp (<! (read-timestamp in))
            count (<? in)
            globals (vec (program/all-globals (-> @state :program :running :compiled)))
            snapshot (atom {:timestamp timestamp, :data {}})]
        (dotimes [_ count]
                 (let [number (<? in)
                       n1 (<? in)
                       n2 (<? in)
                       n3 (<? in)
                       n4 (<? in)
                       float-value (bytes->float [n1 n2 n3 n4])]
                   (when-let [global-name (:name (nth globals number {}))]
                     (swap! snapshot assoc-in [:data global-name]
                            {:name global-name
                             :number number
                             :value float-value
                             :raw-bytes [n1 n2 n3 n4]}))))
        (swap! state assoc :globals @snapshot)
        (process-timestamp timestamp))))

(defn- process-free-ram [in]
  (go (let [timestamp (<! (read-timestamp in))
            arduino (<! (read-uint32 in))
            uzi (<! (read-uint32 in))]
        (swap! state update :memory
               (fn [_] {:arduino arduino, :uzi uzi}))
        (process-timestamp timestamp))))

(defn- process-script-state [i byte]
  (let [running? (> (bit-and 2r10000000 byte) 0)
        error-code (bit-and 2r01111111 byte)
        error-msg (error-msg error-code)
        program (-> @state :program :running)
        script-name (-> program :compiled :scripts (get i) :name)
        task? (-> program :final-ast :scripts (get i) ast/task?)]
    [script-name
     {:index i
      :name script-name
      :running? running?
      :error? (error? error-code)
      :error-code error-code
      :error-msg error-msg
      :task? task?}]))

(defn- process-running-scripts [in]
  (go (let [timestamp (<! (read-timestamp in))
            count (<? in)
            tuples (map-indexed process-script-state
                                (<! (read-vec? count in)))
            [old new] (swap-vals! state assoc :scripts (into {} tuples))]
        (doseq [script (filter :error? (sort-by :index (-> new :scripts vals)))]
          (when-not (= (-> old :scripts (get (:name script)) :error-code)
                       (:error-code script))
            (logger/warning "%1 detected on script \"%2\". The script has been stopped."
                            (:error-msg script)
                            (:name script))))
        (process-timestamp timestamp))))

(defn- process-profile [in]
  (go (let [n1 (<? in)
            n2 (<? in)
            value (bit-or n2
                          (bit-shift-left n1 7))
            report-interval (<? in)]
        (swap! state assoc
               :profiler {:ticks value
                          :interval-ms 100
                          :report-interval report-interval})
        (add-pseudo-var! "__tps" (* 10 value))
        (add-pseudo-var! "__vm-report-interval" report-interval))))

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
          (logger/newline)
          (logger/warning "%1 detected. The program has been stopped"
                          (error-msg error-code))
          (if (error-disconnect? error-code)
            (disconnect))))))

(defn- process-trace [in]
  (go (let [count (<? in)
            msg (new String (byte-array (<! (read-vec? count in))) "UTF-8")]
        (log/info "TRACE:" msg))))

(defn- process-serial-tunnel [in]
  (go (logger/log "SERIAL: %1" (<? in))))

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
              (go (logger/warning "Uzi - Invalid response code: %1" cmd)))))
      (recur))))

(defn- clean-up-reports []
  (go-loop []
    (when (connected?)
      ; If we have pseudo vars, remove old ones (older than 1s)
      (if-not (zero? (count (-> @state :pseudo-vars :data)))
        (if (cfg/get-config :pseudo-vars? false)
          (swap! state update-in [:pseudo-vars :data]
                 #(let [limit (- (or (get-in @state [:pseudo-vars :timestamp]) 0) 1000)]
                    (into {} (remove (fn [[_ value]] (< (:ts value) limit)) %))))
          (swap! state assoc-in [:pseudo-vars :data] {})))
      ; Now remove pins/globals that are not being reported anymore
      (let [reporting (:reporting @state)]
        (swap! state update-in [:pins :data] #(select-keys % (:pins reporting)))
        (swap! state update-in [:globals :data] #(select-keys % (:globals reporting))))
      ; Finally wait 1s and start over
      (<! (timeout 1000))
      (recur))))

(comment
 (def data {"a" 1 "b" 2 "c" 3})
 (count {})
 (select-keys data ["a" "b"])
 (into {} (remove (fn [[key value]] (str/starts-with? "_" key))
                  data))
 ,)

(defn- extract-socket-data [port-name]
  (try
    (when-let [match (re-matches #"((\d+)\.(\d+)\.(\d+)\.(\d+)|localhost)\:(\d+)"
                                 (str/trim port-name))]
      (let [address (nth match 1)
            port (Integer/parseInt (nth match 6))]
        (assert (< 0 port 0x10000))
        (when-not (= address "localhost")
          (assert (every? #(<= 0 % 255)
                          (map #(Integer/parseInt %)
                               (->> match (drop 2) (take 4))))))
        [address port]))
    (catch Throwable ex
      false)))

(defn- open-port [port-name baud-rate]
  (try
    (logger/newline)
    (if-let [[address port] (extract-socket-data port-name)]
      (do
        (logger/log "Connecting on socket...")
        (logger/log "Opening port: %1" port-name)
        (Socket. address port))
      (do
        (logger/log "Connecting on serial...")
        (logger/log "Opening port: %1" port-name)
        (s/open port-name :baud-rate baud-rate)))
    (catch Exception e
      (logger/error "Opening port failed!")
      (log/error e) ; TODO(Richo): Exceptions should be logged but not sent to the client
      nil)))

(defn connect
  ([] (connect (first (available-ports))))
  ([port-name & {:keys [board baud-rate]
                 :or {board UNO, baud-rate 9600}}]
   (if (connected?)
     (log/error "The board is already connected")
     (when-let [port (open-port port-name baud-rate)]
       (let [in (a/chan 1000)]
         (listen! port #(>!! in %))
         (if-not (<?? (request-connection port in) 15000)
           (close! port)
           (do ; Connection successful
             (swap! state assoc
                    :port port
                    :port-name port-name
                    :connected? true
                    :board board
                    :timing {:diffs (rb/make-ring-buffer (cfg/get-config :timing-diffs-size 50))
                             :arduino nil
                             :middleware nil}
                    :reporting {:pins #{}
                                :globals #{}})
             (set-report-interval (cfg/get-config :report-interval-min 0))
             (keep-alive port)
             (process-input in)
             (start-reporting)
             (send-pins-reporting)
             (clean-up-reports))))))))

(defonce ^:private port-scanning? (atom false))

(defn stop-port-scan []
  (reset! port-scanning? false))

(defn start-port-scan []
  (when (compare-and-set! port-scanning? false true)
    (go-loop []
      (when @port-scanning?
        (when-not (connected?)
          (swap! state assoc :available-ports (available-ports)))
        (<! (timeout 1000))
        (recur)))))

(start-port-scan)
