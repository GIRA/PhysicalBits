(ns plugin.core
  (:require [serial.core :as s]
            [serial.util :as su]
            [clojure.core.async :as a]
            [compojure.core :as compojure :refer [GET POST]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [compojure.route :as route]
            [aleph.http :as http]
            [clojure.java.io :as io]
            [manifold.stream :as ws]
            [manifold.deferred :as d]
            [cheshire.core :as json])
  (:gen-class))

;;;;;;;;;;;;;;;;;;;;; ARDUINO ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def default-state {:port-name nil
                    :port nil
                    :a0 0})
(def state (atom default-state))

(defn- error [msg] (println "ERROR:" msg))

(defn available-ports [] (su/get-port-names))

(defn connected? []
  (not (nil? (@state :port))))

(defmacro check-connection [then-form]
  `(if (connected?)
     ~then-form
     (error "The board is not connected!")))

(defn- process-input [in]
  (a/go-loop []
    (when (connected?)
      (swap! state assoc :a0 (a/<! in))
      (recur))))

(defn connect
  ([] (connect (first (available-ports)) 115200))
  ([port-name] (connect port-name 115200))
  ([port-name baud-rate]
   (if (connected?)
     (error "The board is already connected")
     (let [port (s/open port-name :baud-rate baud-rate)
           in (a/chan 1000)]
       (s/listen! port #(a/>!! in (.read %)))
       (swap! state assoc
              :port port
              :port-name port-name)
       (process-input in)))))

(defn disconnect []
  (check-connection
   (let [port (@state :port)]
     (reset! state default-state)
     (s/close! port))))

(defn send-data [bytes]
  (check-connection (s/write (@state :port) bytes)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;WEB SERVER;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn wrap-websocket [handler]
  (fn [req]
    (-> (d/let-flow [socket (http/websocket-connection req)]
                    (handler socket req)
                    nil)
        (d/catch {:status 400
                  :headers {"content-type" "application/text"}
                  :body "Expected a websocket request."}))))

(defn seconds-handler [socket _]
  (a/go-loop [i 0]
    (ws/put! socket (str "seconds: " i))
    (a/<! (a/timeout 1000))
    (recur (inc i))))

(defn echo-handler [socket _]
  (ws/connect socket socket))

(defn analog-read-handler [socket _]
  (a/go-loop []
    (when (not (ws/closed? socket))
      (ws/put! socket (str "A0: " (@state :a0)))
      (a/<! (a/timeout 100))
      (recur))))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body    (json/generate-string data)})

(defn connect-handler [req]
  (connect)
  (let [port-name (@state :port-name)]
    (json-response {:port-name port-name})))

(defn disconnect-handler [req]
  (disconnect)
  (json-response "OK"))

(def handler
  (-> (compojure/routes (GET "/" [] (io/resource "public/index.html"))
                        (GET "/seconds" [] (wrap-websocket seconds-handler))
                        (GET "/echo" [] (wrap-websocket echo-handler))
                        (GET "/analog-read" [] (wrap-websocket analog-read-handler))
                        (POST "/connect" req (connect-handler req))
                        (POST "/disconnect" req (disconnect-handler req))
                        (route/not-found "No such page."))
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)
      (wrap-params)))

(defn start-server []
  (http/start-server handler {:port 3000}))

(defn -main [& args]
  (time (do
          (println "Starting server...")
          (start-server)
          (println "Server started."))))
