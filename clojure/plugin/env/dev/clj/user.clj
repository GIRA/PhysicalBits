(ns user
  (:require [plugin.core :refer :all]
            [clojure.core.async :refer [go-loop <! <!! timeout]]
            [clojure.tools.namespace.repl :as repl])
  (:use [clojure.repl]))

(defn reload []
  (disconnect)
  (stop-server)
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
                 (println (- now begin) ":" (@state :a0))
                 (<! (timeout interval))
                 (recur)))))))))
