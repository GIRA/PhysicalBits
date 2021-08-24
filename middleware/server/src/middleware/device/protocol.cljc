(ns middleware.device.protocol
  (:require [clojure.string :as str]
            [middleware.utils.conversions :as c]
            [clojure.core.async :as a :refer [<! >! go-loop go]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CONSTANTS

; Version number
(def MAJOR_VERSION 0)
(def MINOR_VERSION 8)

; Outgoing
(def MSG_OUT_CONNECTION_REQUEST 255)
(def MSG_OUT_SET_PROGRAM 0)
(def MSG_OUT_SET_VALUE 1)
(def MSG_OUT_SET_MODE 2)
(def MSG_OUT_START_REPORTING 3)
(def MSG_OUT_STOP_REPORTING 4)
(def MSG_OUT_SET_REPORT 5)
(def MSG_OUT_SAVE_PROGRAM 6)
(def MSG_OUT_KEEP_ALIVE 7)
(def MSG_OUT_PROFILE 8)
(def MSG_OUT_SET_REPORT_INTERVAL 9)
(def MSG_OUT_SET_GLOBAL 10)
(def MSG_OUT_SET_GLOBAL_REPORT 11)
(def MSG_OUT_DEBUG_CONTINUE	12)
(def MSG_OUT_DEBUG_SET_BREAKPOINTS 13)
(def MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 14)

; Incoming
(def MSG_IN_ERROR 0)
(def MSG_IN_PIN_VALUE 1)
(def MSG_IN_PROFILE 2)
(def MSG_IN_GLOBAL_VALUE 3)
(def MSG_IN_TRACE 4)
(def MSG_IN_COROUTINE_STATE 5)
(def MSG_IN_RUNNING_SCRIPTS 6)
(def MSG_IN_FREE_RAM 7)
(def MSG_IN_SERIAL_TUNNEL 8)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ERRORS

; Error messages
(def ^:private error-msgs
  [0 "NO_ERROR"
   1 "STACK_OVERFLOW"
   2 "STACK_UNDERFLOW"
   4 "ACCESS_VIOLATION"
   8 "OUT_OF_MEMORY"
   16 "READER_TIMEOUT"
   32 "DISCONNECT_ERROR"
   64 "READER_CHECKSUM_FAIL"])

(defn error-msg [^long code]
  (if (= 0 code)
    "NO_ERROR"
    (let [msg (str/join
               " & "
               (map (fn [[_ k]] k)
                    (filter (fn [[^long c _]] (not= 0 (bit-and code c)))
                            (partition-all 2 error-msgs))))]
      (if (empty? msg)
        (str "UNKNOWN_ERROR (" code ")")
        msg))))

(defn error? [code] (not= 0 code))

(defn error-disconnect? [^long code]
  (not= 0 (bit-and code 32)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; OUTGOING

(defn set-global-value [global-number ^double value]
  (let [^long actual-value (c/float->uint32 value)]
    [MSG_OUT_SET_GLOBAL
     global-number
     (bit-and 16rFF (bit-shift-right actual-value 24))
     (bit-and 16rFF (bit-shift-right actual-value 16))
     (bit-and 16rFF (bit-shift-right actual-value 8))
     (bit-and 16rFF actual-value)]))

(defn set-global-report [global-number report?]
  [MSG_OUT_SET_GLOBAL_REPORT
   global-number
   (if report? 1 0)])

(defn set-pin-value [pin-number ^double value]
  [MSG_OUT_SET_VALUE
   pin-number
   ; TODO(Richo): Maybe clamp the value between [0 1]?
   (Math/round (* value 255.0))])

(defn set-pin-report [pin-number report?]
  [MSG_OUT_SET_REPORT
   pin-number
   (if report? 1 0)])

(defn run [bytecodes]
  (let [msb (bit-shift-right (bit-and (count bytecodes)
                                      16rFF00)
                             8)
        lsb (bit-and (count bytecodes)
                     16rFF)]
    (concat [MSG_OUT_SET_PROGRAM msb lsb] bytecodes)))

(defn install [bytecodes]
  (let [msb (bit-shift-right (bit-and (count bytecodes)
                                      16rFF00)
                             8)
        lsb (bit-and (count bytecodes)
                     16rFF)]
    (concat [MSG_OUT_SAVE_PROGRAM msb lsb] bytecodes)))

(defn start-reporting [] [MSG_OUT_START_REPORTING])
(defn stop-reporting [] [MSG_OUT_STOP_REPORTING])

(defn start-profiling [] [MSG_OUT_PROFILE 1])
(defn stop-profiling [] [MSG_OUT_PROFILE 0])

(defn set-report-interval [interval]
  [MSG_OUT_SET_REPORT_INTERVAL interval])

(defn set-all-breakpoints [] [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 1])
(defn clear-all-breakpoints [] [MSG_OUT_DEBUG_SET_BREAKPOINTS_ALL 0])
(defn continue [] [MSG_OUT_DEBUG_CONTINUE])

(defn keep-alive [] [MSG_OUT_KEEP_ALIVE])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; HANDSHAKE

(defn request-connection []
  [MSG_OUT_CONNECTION_REQUEST
   MAJOR_VERSION
   MINOR_VERSION])

(defn confirm-handshake [n1]
  (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256))

(defn perform-handshake [{:keys [in out]}]
  (go
   (>! out (request-connection))
   (when-let [n1 (<! in)]
     (let [n2 (confirm-handshake n1)]
       (>! out [n2])
       (= n2 (<! in))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INCOMING

(defn read-vec! [count in]
  (go
   (when count
     (loop [^long i count, elements []]
       (if (<= i 0)
         elements
         (when-let [val (<! in)]
           (recur (dec i) (conj elements val))))))))

(defn- read-uint16 [in]
  (go
   (when-let [bytes (<! (read-vec! 2 in))]
     (c/bytes->uint16 bytes))))

(defn- read-uint32 [in]
  (go
   (when-let [bytes (<! (read-vec! 4 in))]
     (c/bytes->uint32 bytes))))

(def read-timestamp read-uint32)

(defn process-pin-value [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<! in)
         data (when count
                (loop [i 0, data []]
                  (if (< i count)
                    (let [^long n1 (<! in)
                          ^long n2 (<! in)]
                      (when (and n1 n2)
                        (let [number (bit-shift-right n1 2)
                              value (/ (bit-or n2
                                               (bit-shift-left (bit-and n1 2r11)
                                                               8))
                                       1023.0)]
                          (recur
                            (inc i)
                            (conj data {:number number
                                        :value value})))))
                    data)))]
     (when data
       {:tag :pin-value
        :timestamp timestamp
        :data data}))))

(defn process-global-value [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<! in)
         data (when count
                (loop [i 0, data []]
                  (if (< i count)
                    (let [number (<! in)
                          raw-bytes (<! (read-vec! 4 in))]
                      (when raw-bytes
                        (recur
                          (inc i)
                          (conj data {:number number
                                      :value (c/bytes->float raw-bytes)
                                      :raw-bytes raw-bytes}))))
                    data)))]
     (when data
       {:tag :global-value
        :timestamp timestamp
        :data data}))))

(defn process-free-ram [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         arduino (<! (read-uint32 in))
         uzi (<! (read-uint32 in))]
     (when (and timestamp arduino uzi)
       {:tag :free-ram
        :timestamp timestamp
        :memory {:arduino arduino, :uzi uzi}}))))

(defn- process-script-state [^long byte]
  (let [running? (> (bit-and 2r10000000 byte) 0)
        error-code (bit-and 2r01111111 byte)
        error-msg (error-msg error-code)]
    {:running? running?
     :error? (error? error-code)
     :error-code error-code
     :error-msg error-msg}))

(defn process-running-scripts [in]
  (go
   (let [timestamp (<! (read-timestamp in))
         count (<! in)
         script-data (<! (read-vec! count in))]
     (when script-data
       {:tag :running-scripts
        :timestamp timestamp
        :scripts (mapv process-script-state
                       script-data)}))))


(defn process-profile [in]
  (go
   (let [^long n1 (<! in)
         ^long n2 (<! in)
         report-interval (<! in)]
     (when (and n1 n2 report-interval)
       {:tag :profile
        :data {:ticks (bit-or n2
                              (bit-shift-left n1 7))
               :interval-ms 100
               :report-interval report-interval}}))))

(defn process-coroutine-state [in]
  (go
   (let [index (<! in)
         pc (<! (read-uint16 in))
         fp (<! in)
         ^long stack-size (<! in)
         stack (when stack-size
                 (<! (read-vec! (* 4 stack-size) in)))]
     (when stack
       {:tag :coroutine-state
        :data {:index index
               :pc pc
               :fp fp
               :stack stack}}))))

(defn process-error [in]
  (go
   (let [^long code (<! in)]
     (when (and code (> code 0))
       {:tag :error
        :error {:code code
                :msg (error-msg code)}}))))

(defn process-trace [in]
  (go
   (let [count (<! in)
         bytes (<! (read-vec! count in))]
     (when bytes
       {:tag :trace
        :msg (c/bytes->string bytes)}))))

(defn process-serial-tunnel [in]
  (go
   (when-let [val (<! in)]
     {:tag :serial
      :data val})))

(def dispatch-table
  {MSG_IN_PIN_VALUE process-pin-value
   MSG_IN_GLOBAL_VALUE process-global-value
   MSG_IN_RUNNING_SCRIPTS process-running-scripts
   MSG_IN_FREE_RAM process-free-ram
   MSG_IN_PROFILE process-profile
   MSG_IN_COROUTINE_STATE process-coroutine-state
   MSG_IN_ERROR process-error
   MSG_IN_TRACE process-trace
   MSG_IN_SERIAL_TUNNEL process-serial-tunnel})

(defn process-next-message [in]
  (go
   (when-let [cmd (<! in)]
     (if-let [dispatch-fn (dispatch-table cmd)]
       (first (a/alts! [(dispatch-fn in)
                        (a/timeout 1000)]
                       :priority true))
       {:tag :unknown-cmd, :code cmd}))))
