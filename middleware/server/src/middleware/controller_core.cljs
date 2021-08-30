(ns middleware.controller-core
  (:require ["readline" :as readline]
            [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]))

(def rl (.createInterface readline
                          #js {:input js/process.stdin
                               :output js/process.stdout}))

(enable-console-print!)

(defn question [q]
  (let [c (a/promise-chan)]
    (.question rl q (fn [d] (a/put! c d)))
    c))

(defn main []
  (ports/register-constructors! #'serial/open-port)
  (println "Richo capo!")
  (go
   (<! (timeout 500))
   (let [port-name (<! (question "Ingrese el puerto: "))]
     (dc/connect! port-name))
   (loop []
     (println)
     (try
       (let [src (<! (question "uzi> "))
             program (c/compile-uzi-string src)]
         (dc/run program))
       (catch :default ex
         (println "ERROR" ex)))
     (recur))))
