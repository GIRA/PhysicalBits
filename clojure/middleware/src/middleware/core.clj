(ns middleware.core
  (:require [clojure.tools.logging :as log]
            [middleware.server.server :as server]
            [middleware.device.controller :as dc])
  (:gen-class))

(defn -main [& args]
  (time (do
          (log/info "Starting server...")
          (server/start)
          (log/info "Server started."))))
