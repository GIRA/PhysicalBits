(ns user
  (:require [plugin.core :refer :all]
            [plugin.device :as device :refer [state]]
            [plugin.server :as server :refer [server]]
            [plugin.compiler :as compiler ]
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
   :blink13 [0 1 2 4 13 5 3 232 192 4 2 131 162]
   :seconds-counter [0 2 3 12 0 13 200 192 5 2 132 162 128 2 169 147]
   :ticking-test [0 3 4 16 5 9 13 200 192 6 2 133 162 128 2 132 162 128 6 131 250 29 225 131 250 29 209]})


(def asts
  {:empty {:__class__ "UziProgramNode"
           :globals []
           :id "a1049b64-6166-ae43-b295-6848823eb0ed"
           :imports []
           :primitives []
           :primitivesDict []
           :scripts []}
   })