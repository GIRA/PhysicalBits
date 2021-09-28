(ns middleware.device.ports.simulator
  (:require [clojure.core.async :as a :refer [<! go timeout]]
            [clojure.string :as str]
            [middleware.device.ports.common :as ports]))

(defn- log-error [msg e]
  (println msg e))

(deftype SimulatorPort []
  ports/UziPort
  (close! [port])
  (make-out-chan! [port]
                  (let [out-chan (a/chan 1000)]
                    (go
                     (try
                       (loop []
                         (when-let [data (<! out-chan)]
                           (js/Serial.write (clj->js data) (count data))
                           #_(println "OUT ->" data)
                           (recur)))
                       (catch :default ex
                         (log-error "ERROR WHILE WRITING OUTPUT (simulator) ->" ex)
                         (a/close! out-chan))))
                    out-chan))
  (make-in-chan! [port]
                 (let [in-chan (a/chan 1000)]
                   (letfn [(listener [bytes]
                                     (try
                                       (let [data (js->clj bytes)]
                                         #_(println "IN ->" data)
                                         (dotimes [i (count data)]
                                                  (when-not (a/put! in-chan (nth data i))
                                                    (js/Serial.removeListener listener))))
                                       (catch :default ex
                                         (log-error "ERROR WHILE READING INPUT (simulator) ->" ex)
                                         (a/close! in-chan))))]
                     (js/Serial.addListener listener))
                   in-chan)))

(defn open-port [port-name baud-rate]
  (try
    (SimulatorPort.)
    (catch :default ex
      (do (log-error "ERROR WHILE OPENING PORT (serial) ->" ex) nil))))
