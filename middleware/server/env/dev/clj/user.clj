(ns user
  (:require [middleware.device.ports.scanner :as port-scanner]
            [middleware.device.controller :as dc :refer [state]]
            [middleware.server.http :as http :refer [server]]
            [middleware.server.udp :as udp]
            [clojure.java.browse :refer [browse-url]]
            [middleware.core :as core])
  (:use [clojure.tools.namespace.repl :as repl :only [refresh-all]]))

(defn stop []
  (dc/disconnect!)
  (http/stop)
  (udp/stop)
  (port-scanner/stop!))

(defn start []
  (core/main {:uzi "../../uzi/libraries"
              :web "../../gui"
              :server-port 3000
              :arduino-port nil
              :open-browser nil}))

(defn reload []
  (stop)
  (repl/refresh))

(defn millis [] (System/currentTimeMillis))

(defn open-browser []
  (browse-url "http://localhost:3000"))
