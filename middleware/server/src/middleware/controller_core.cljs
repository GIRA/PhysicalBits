(ns middleware.controller-core
  (:require ;["serialport" :as SerialPort]
            [clojure.core.async :as a]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]))

(defn main []
  (ports/register-constructors! #'serial/open-port)
  (println "Richo capo!"))

(comment

@ports/constructors

(def port (ports/connect! "COM4" 9600))
port
(ports/disconnect! port)
(a/go-loop [i 0]
  (when-let [data (a/<! (:in port))]
    (println i data)
    (recur (inc i))))

(a/put! (:out port) [65 66 67])


(def port (SerialPort. "COM4" {:baudRate 9600}))

(.on port "data" (fn [data]
                   (dotimes [i (.-length data)]
                            (println (aget data i)))))
(js/Uint8Array.from [0 1 2 3 4 5])
(.write port "Richo!")
(.write port (js/Uint8Array.from [65 66 67]))
(.close port)
(c/compile-uzi-string "task blink13() running 1/s { toggle(D13); }")

SerialPort
 ,,,)
