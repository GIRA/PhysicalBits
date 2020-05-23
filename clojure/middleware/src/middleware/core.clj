(ns middleware.core
  (:require [middleware.server.server :as server]
            [middleware.device.controller :as dc])
  (:gen-class))

(defn -main [& args]
  (time (do
          (println "Starting server...")
          (server/start)
          (println "Server started."))))
