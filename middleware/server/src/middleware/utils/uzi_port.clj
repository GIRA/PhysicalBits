(ns middleware.utils.uzi-port
  (:require [clojure.tools.logging :as log]
            [serial.core :as s]
            [serial.util :as su]
            [clojure.string :as str]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]])
  (:import (java.net Socket)))

(def TIMEOUT 12000)

(defn available-ports []
  (vec (sort (su/get-port-names))))

(defprotocol UziPort
  (close! [this])
  (write! [this data])
  (listen! [this listener-fn]))

(defn- custom-open
  ([path & {:keys [baud-rate databits stopbits parity]
            :or {baud-rate 115200, databits s/DATABITS_8, stopbits s/STOPBITS_1, parity s/PARITY_NONE}}]
   (try
     (let [uuid     (.toString (java.util.UUID/randomUUID))
           _        (log/info "custom-open 1")
           port-id  (s/port-identifier path)
           _        (log/info "custom-open 2")
           raw-port ^purejavacomm.SerialPort (.open port-id uuid 0)
           _        (log/info "custom-open 3")
           out      (.getOutputStream raw-port)
           _        (log/info "custom-open 4")
           in       (.getInputStream  raw-port)
           _        (log/info "custom-open 5")
           _        (.setSerialPortParams raw-port baud-rate
                                          databits
                                          stopbits
                                          parity)]

       (assert (not (nil? port-id)) (str "Port specified by path " path " is not available"))
       (log/info "custom-open 6")
       (serial.core.Port. path raw-port out in))
     (catch Exception e
       (throw (Exception. (str "Sorry, couldn't connect to the port with path " path ) e))))))


(defn- defer-timeout [fn timeout]
  (log/info "defer-timeout BEGIN")
  (let [ch (a/chan)
        t (Thread. #(>!! ch (fn)))]
    (log/info "defer-timeout About to start thread")
    (.start t)
    (log/info "defer-timeout Thread started")
    (let [[val _] (a/alts!! [ch (a/timeout timeout)])]
      (log/info "defer-timeout Wait finished")
      (when-not val
        (log/info "defer-timeout About to terminate thread (timeout)")
        (.interrupt t)
        (log/info "defer-timeout Thread interrupted!")
        (throw (Exception. "TIMEOUT")))
      (log/info "defer-timeout Action successful!")
      (log/info (clojure.pprint/pprint val))
      val)))

#_(defn open-serial [port-name baud-rate]
  (log/info "open-serial BEGIN")
  (let [ch (a/chan)
        t (Thread.
           #(>!! ch
                 (try
                   (do
                     (log/info "open-serial About to open port")
                     (custom-open port-name :baud-rate baud-rate))
                   (catch Exception e false))))]
    (log/info "open-serial About to start thread")
    (.start t)
    (log/info "open-serial Thread started")
    (let [[val _] (a/alts!! [ch (a/timeout TIMEOUT)])]
      (log/info "open-serial Wait finished")
      (when-not val
        (log/info "open-serial About to terminate thread (timeout)")
        (.interrupt t)
        (log/info "open-serial Thread interrupted!")
        (throw (Exception. "Serial port open timeout")))
      (log/info "open-serial Open port successful!")
      (log/info (clojure.pprint/pprint val))
      val)))

(defn open-serial [port-name baud-rate]
  (defer-timeout
    #(try
       (log/info "open-serial About to open port")
       (custom-open port-name :baud-rate baud-rate)
       (catch Exception _ false))
    TIMEOUT))

(extend-type serial.core.Port
  UziPort
  (close! [port] (deref (future (s/close! port)) TIMEOUT nil))
  (write! [port data] (do
                        (log/info "UziPort/write!")
                        (s/write port data)))
  (listen! [port listener-fn] (do
                                (log/info "UziPort/listen!")
                                (s/listen! port #(listener-fn (.read %))))))

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

(defn open-socket [address port]
  (Socket. address port))
