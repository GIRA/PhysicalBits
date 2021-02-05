(ns user
  (:require [middleware.core :refer :all]
            [middleware.device.controller :as dc :refer [state]]
            [middleware.server.server :as server :refer [server]]
            [middleware.compiler.compiler :as cc]
            [middleware.parser.parser :as pp]
            [middleware.code-generator.code-generator :as cg]
            [program-rewriter :as prw]
            [ast-rewriter :as ast-rw]
            [clojure.core.async :as a :refer [go-loop <! <!! timeout]]
            [clojure.tools.namespace.repl :as repl]
            [clojure.test :as ctest]
            [clojure.java.browse :refer [browse-url]]
            [clojure.tools.logging :as log])
  (:use [clojure.repl]))

(defn stop []
  (dc/stop-port-scan)
  (dc/disconnect)
  (server/stop))

(defn start []
  (server/start))

(defn reload []
  (stop)
  (repl/refresh :after 'dc/start-port-scan))

(defn millis [] (System/currentTimeMillis))

(defn open-browser []
  (browse-url "http://localhost:3000"))
