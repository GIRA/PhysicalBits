(ns user
  (:require [middleware.device.controller :as dc :refer [state]]
            [middleware.server.http :as http :refer [server]]
            [middleware.server.udp :as udp]
            [clojure.java.browse :refer [browse-url]]
            [middleware.core :as core])
  (:use [clojure.tools.namespace.repl :as repl :only [refresh-all]]))

(defn stop []
  (dc/stop-port-scan)
  (dc/disconnect)
  (http/stop)
  (udp/stop))

(defn start []
  (core/main {:uzi "../../uzi/libraries"
              :web "../../gui"
              :server-port 3000
              :arduino-port nil
              :open-browser nil}))

(defn reload []
  (stop)
  (repl/refresh :after 'dc/start-port-scan))

(defn millis [] (System/currentTimeMillis))

(defn open-browser []
  (browse-url "http://localhost:3000"))
