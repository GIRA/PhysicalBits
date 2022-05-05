(ns middleware.device.ports.serial
  (:require [clojure.core.async :as a :refer [<! go timeout]]
            [clojure.string :as str]
            ["serialport" :as SerialPort]
            [middleware.device.ports.common :as ports]))

(defn- log-error [msg e]
  (println msg e))

(extend-type SerialPort
  ports/UziPort
  (close! [port] (let [error-msg "ERROR WHILE DISCONNECTING (serial) ->"]
                   (try
                     (when (.-isOpen port)
                       (.close port))
                     (catch :default ex
                       (log-error error-msg ex)))))
  (make-out-chan! [port]
                  (let [error-msg "ERROR WHILE WRITING OUTPUT (serial) ->"
                        out-chan (a/chan 1000)]
                    (go
                     (try
                       (loop []
                         (when-let [data (<! out-chan)]
                           (.write port (js/Uint8Array.from data)
                                   (fn [err]
                                     (when err
                                       (a/close! out-chan))))
                           (recur)))
                       (catch :default ex
                         (log-error error-msg ex)
                         (a/close! out-chan))))
                    out-chan))
  (make-in-chan! [port]
                 (let [in-chan (a/chan (a/sliding-buffer 1000))]
                   (.on port "data" (fn [data]
                                      (dotimes [i (.-length data)]
                                               (a/put! in-chan (aget data i)))))
                   in-chan)))

(defn open-port [port-name baud-rate]
  (try
    (let [port (SerialPort. port-name {:baudRate baud-rate})]
      (.on port "error" (fn [err] (log-error "ERROR (serial) ->" err)))
      port)
    (catch :default ex
      (do (log-error "ERROR WHILE OPENING PORT (serial) ->" ex) nil))))
