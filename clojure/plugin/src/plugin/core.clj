(ns plugin.core
  (:require [plugin.server.server :as server])
  (:gen-class))

(defn -main [& args]
  (time (do
          (println "Starting server...")
          (server/start)
          (println "Server started."))))
