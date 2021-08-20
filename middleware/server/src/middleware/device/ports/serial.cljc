(ns middleware.device.ports.serial
  (:require [clojure.tools.logging :as log]
            [clojure.core.async :as a :refer [<!! >!! go-loop timeout]]
            [clojure.string :as str]
            [serial.core :as s]
            [middleware.device.ports.common :as ports]))

(extend-type serial.core.Port
  ports/UziPort
  (close! [port]
          (s/close! port)
          ; TODO(Richo): There seems to be some race condition if I disconnect/reconnect
          ; quickly. I suspect the problem is that I need to wait until all threads are
          ; finished or maybe I should close the channels and properly clean up the
          ; resources. However, for now a 1s delay seems to work...
          (<!! (timeout 1000)))
  (write! [port data] (s/write port data))
  (listen! [port listener-fn]
           (s/listen! port
                      (fn [^java.io.InputStream input-stream]
                        (listener-fn (.read input-stream))))))

(defn open-port [port-name baud-rate]
  (try
    (log/info "Trying to open SERIAL!")
    (s/open port-name :baud-rate baud-rate)
    (catch Throwable ex
      (do (log/error ex) nil))))

(comment
 (s/open "127.0.0.1:4242" :baud-rate 57600)
 (open-port "127.0.0.1:4242" 57600)
 ,,)