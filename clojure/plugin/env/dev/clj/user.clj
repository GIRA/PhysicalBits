(ns user
  (:require [plugin.core :refer :all]
            [plugin.device :as device :refer [state]]
            [plugin.server :as server :refer [server]]
            [clojure.core.async :as a :refer [go-loop <! <!! timeout]]
            [clojure.tools.namespace.repl :as repl])
  (:use [clojure.repl]))

(defn reload []
  (device/disconnect)
  (server/stop)
  (repl/refresh))

(defn millis [] (System/currentTimeMillis))

(defn print-a0
  ([] (print-a0 5000 10))
  ([ms] (print-a0 ms 10))
  ([ms interval]
   (let [begin (millis)
         end (+ begin ms)]
     (time
      (<!! (go-loop []
             (let [now (millis)]
               (when (< now end)
                 (println (- now begin) ":" (device/get-pin-value "A0"))
                 (<! (timeout interval))
                 (recur)))))))))

(def programs
  {:empty [0 0]
   :blink13 [0 1 2 4 13 5 3 232 192 4 2 131 162]})
