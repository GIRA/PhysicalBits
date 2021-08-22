(ns middleware.device.ports.socket
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as a :refer [<!! >!! go-loop timeout]]
            [clojure.string :as str]
            [middleware.device.ports.common :as ports])
  (:import (java.net Socket)))

(extend-type java.net.Socket
  ports/UziPort
  (close! [socket]
          (.close socket)
          ; TODO(Richo): There seems to be some race condition if I disconnect/reconnect
          ; quickly. I suspect the problem is that I need to wait until all threads are
          ; finished or maybe I should close the channels and properly clean up the
          ; resources. However, for now a 1s delay seems to work...
          (<!! (timeout 1000)))
  (make-out-chan! [socket]
                  ; TODO(Richo): Close the channel on disconnect!
                  (let [out-chan (a/chan 1000)
                        out-stream (.getOutputStream socket)]
                    (a/thread
                     (loop []
                       (when-not (.isClosed socket)
                         (let [bytes (<!! out-chan)]
                           (.write out-stream (byte-array bytes)))
                         (recur))))
                    out-chan))
  (make-in-chan! [socket]
                 ; TODO(Richo): Close the channel on disconnect!
                 (let [in-chan (a/chan 1000)
                       buffer-size 1000
                       buffer (byte-array buffer-size)
                       in-stream (.getInputStream socket)]
                   (a/thread ; NOTE(Richo): Using a thread because InputStream.read() blocks
                             ; until data is available.
                    (loop []
                      (when-not (.isClosed socket)
                        (let [bytes-read (.read in-stream buffer 0 buffer-size)]
                          (dotimes [i bytes-read]
                                   (>!! in-chan
                                        (bit-and (int (nth buffer i)) 16rFF))))
                        (recur))))
                   in-chan)))

(defn- extract-socket-data [port-name]
  (try
    (when-let [match (re-matches
                      #"((\d+)\.(\d+)\.(\d+)\.(\d+)|localhost)\:(\d+)"
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

(defn open-port [port-name _baud-rate]
  (try
    (when-let [[^String address ^int port]
               (extract-socket-data port-name)]
      (Socket. address port))
    (catch Throwable ex
      (do (log/error ex) nil))))

(comment
 (Socket. "127.0.0.1" 4242)
 (open-port "127.0.0.1:4242" 57600)
 ,,)
