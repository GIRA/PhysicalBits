(ns middleware.device.ports.simulator
  (:require [clojure.core.async :as a :refer [<! go timeout]]
            [clojure.string :as str]
            [middleware.device.ports.common :as ports]))

(defn- log-error [msg e]
  (println msg e))

(deftype SimulatorPort [open?]
  ports/UziPort
  (close! [port] (reset! open? false))
  (make-out-chan! [port]
                  (let [error-msg "ERROR WHILE WRITING OUTPUT (simulator) ->"
                        out-chan (a/chan 1000)]
                    (go
                     (try
                       (loop []
                         (when-let [data (<! out-chan)]
                           (js/Serial.write (clj->js data) (count data))
                           (println "OUT ->" data)
                           (when @(.-open? port)
                             (recur))))
                       (catch :default ex
                         (log-error error-msg ex)
                         (a/close! out-chan))))
                    out-chan))
  (make-in-chan! [port]
                 (let [error-msg "ERROR WHILE READING INPUT (simulator) ->"
                       in-chan (a/chan 1000)
                       closed-signal (random-uuid)]
                   (go
                    (try
                      (js/Serial.readAvailable) ; Discard old data
                      (loop []
                        (if-some [bytes (js/Serial.readAvailable)]
                          (let [data (js->clj bytes)]
                            (println "IN ->" data)
                            (dotimes [i (count data)]
                                     (when-not (>! in-chan (nth data i))
                                       (throw closed-signal))))
                          (println "READING..."))
                        (<! (timeout 10))
                        (when @(.-open? port)
                          (recur)))
                      (catch :default ex
                        (println "ERROR" ex)
                        (when-not (= ex closed-signal)
                          (log-error error-msg ex)
                          (a/close! in-chan)))))
                   in-chan)))

(defn open-port [port-name baud-rate]
  (try
    (SimulatorPort. (atom true))
    (catch :default ex
      (do (log-error "ERROR WHILE OPENING PORT (serial) ->" ex) nil))))
