(ns middleware.device.protocol
  (:require [clojure.string :as str]
            [middleware.utils.conversions :as c]))

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

(defn request-connection []
  [MSG_OUT_CONNECTION_REQUEST
   MAJOR_VERSION
   MINOR_VERSION])

(defn confirm-handshake [n1]
  (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256))

(defn keep-alive [] [MSG_OUT_KEEP_ALIVE])
