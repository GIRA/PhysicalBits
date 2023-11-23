(ns middleware.mendieta
  (:require [clojure.core.async :refer [go <! timeout]]
            ["express" :as express]
            ["express-ws" :as ws]))

(def !state (atom {:activities []}))

(comment
  
  ; Race condition
  (reset! !state {:activities [ 1 2 3 ]})
  (swap! !state )

  )

(def !server (atom nil))

(defn init-activity-controller! [^js app]
  (.get (.route app "/activities")
        (fn [req res]
          (.send res (clj->js (:activities @!state))))))

(defn start! [port]
  (let [app (express)]
    (ws app)
    (.use app (.json express))
    (.use app (.urlencoded express #js {:extended true}))
    (.use app (.static express "frontend"))
    (init-activity-controller! app)
    (reset! !server
            (.listen app port #(println "Server started on"
                                        (str "http://localhost:" port))))))

(defn stop! []
  (when-let [server @!server]
    (println "Stopping server...")
    (.close server)))

(defn main [& args]
  (println "Hola mundo")
  (start! 5000))

(defn ^:dev/before-load reload-begin* []
  (stop!))

(defn ^:dev/after-load reload-end* []
  (start! 5000))
