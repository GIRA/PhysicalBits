(ns user
  (:require [middleware.core :refer :all]
            [middleware.device.controller :as dc :refer [state]]
            [middleware.server.server :as server :refer [server]]
            [middleware.compiler.compiler :as cc]
            [middleware.parser.parser :as pp]
            [middleware.code-generator.code-generator :as cg]
            [clojure.core.async :as a :refer [go-loop <! <!! timeout]]
            [clojure.tools.namespace.repl :as repl]
            [clojure.test :as ctest]
            [clojure.java.browse :refer [browse-url]])
  (:use [clojure.repl]))

(defn stop []
  (dc/disconnect)
  (server/stop))

(defn start []
  (server/start))

(defn reload []
  (stop)
  (repl/refresh))

(defn restart []
  (stop)
  (repl/refresh :after 'user/start))

(defn millis [] (System/currentTimeMillis))

(defn open-browser []
  (browse-url "http://localhost:3000"))

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
                 (println (- now begin) ":" (dc/get-pin-value "A0"))
                 (<! (timeout interval))
                 (recur)))))))))
