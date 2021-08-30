(ns middleware.device.ports.serial
  (:require #?(:clj [clojure.tools.logging :as log])
            [clojure.core.async :as a :refer [<! go timeout]]
            [clojure.string :as str]
            #?(:clj [serial.core :as s]
               :cljs ["serialport" :as SerialPort])
            [middleware.device.ports.common :as ports]))

(defn- log-error [msg e]
  #?(:clj (log/error msg e)
    :cljs (println "ERROR:" msg e)))

#?(:clj
   (extend-type serial.core.Port
     ports/UziPort
     (close! [port] (s/close! port))
     (make-out-chan! [port]
                     (let [out-chan (a/chan 1000)]
                       (go
                        (try
                          (loop []
                            (when-let [data (<! out-chan)]
                              (s/write port data)
                              (recur)))
                          (catch Throwable ex
                            (log-error "ERROR WHILE WRITING OUTPUT (serial) ->" ex)
                            (a/close! out-chan))))
                       out-chan))
     (make-in-chan! [port]
                    (let [in-chan (a/chan 1000)]
                      (s/listen! port
                                 (fn [^java.io.InputStream input-stream]
                                   (try
                                     (a/put! in-chan (.read input-stream))
                                     (catch Throwable ex
                                       (log-error "ERROR WHILE READING INPUT (serial) ->" ex)
                                       (a/close! in-chan)))))
                      in-chan))))

#?(:cljs
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
                            (log-error "ERROR" ex)
                            (a/close! out-chan))))
                       out-chan))
     (make-in-chan! [port]
                    (let [in-chan (a/chan 1000)]
                      (.on port "data" (fn [data]
                                         (dotimes [i (.-length data)]
                                                  (a/put! in-chan (aget data i)))))
                      in-chan))))

(defn open-port [port-name baud-rate]
  (try
    #?(:clj (s/open port-name :baud-rate baud-rate)
       :cljs (SerialPort. port-name {:baudRate baud-rate}))
    (catch #?(:clj Throwable :cljs :default) ex
      (do (log-error "ERROR while opening port (serial) ->" ex) nil))))
