(ns middleware.device.ports.serial
  (:require [clojure.core.async :as a :refer [<! go timeout]]
            [clojure.string :as str]
            ["serialport" :as SerialPort]
            [middleware.device.ports.common :as ports]))

(defn- log-error [msg e]
  (println "ERROR:" msg e))

(extend-type SerialPort
  ports/UziPort
  (close! [port] (.close port))
  (make-out-chan! [port]
                  (let [out-chan (a/chan 1000)]
                    (go
                     (try
                       (loop []
                         (when-let [data (<! out-chan)]
                           (.write port (js/Uint8Array.from data))
                           (recur)))
                       (catch :default ex
                         (log-error "ERROR WHILE WRITING OUTPUT (serial) ->" ex)
                         (a/close! out-chan))))
                    out-chan))
  (make-in-chan! [port]
                 (let [in-chan (a/chan 1000)]
                   (.on port "data" (fn [data]
                                      (dotimes [i (.-length data)]
                                               (a/put! in-chan (aget data i)))))
                   in-chan)))

(defn open-port [port-name baud-rate]
  (try
    (SerialPort. port-name {:baudRate baud-rate})
    (catch :default ex
      (do (log-error "ERROR WHILE OPENING PORT (serial) ->" ex) nil))))
