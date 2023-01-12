(ns user
  (:require [middleware.device.ports.scanner :as port-scanner]
            [middleware.device.controller :as dc]
            [middleware.server.http :as http]
            [middleware.server.udp :as udp]
            [clojure.java.browse :refer [browse-url]]
            [middleware.main :as main])
  (:use [clojure.tools.namespace.repl :as repl :only [refresh-all]]))

(defn stop []
  (dc/disconnect!)
  (http/stop)
  (udp/stop)
  (port-scanner/stop!))

(defn start []
  (main/start {:uzi "../../uzi/libraries"
               :web "../../gui"
               :server-port 3000
               :arduino-port nil
               :open-browser nil}))

(defn reload []
  (stop)
  (repl/refresh))

(defn open-browser []
  (browse-url "http://localhost:3000"))

(defn init []
  (main/init-dependencies))

(init)

(comment
  (reload)
  (refresh-all)
  (start)
  (open-browser)
  ,,,)