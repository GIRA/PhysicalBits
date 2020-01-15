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

; TODO(Richo): Replace with log/error
(defn- error [msg] (println "ERROR:" msg))

;;;;;;;;;;;;;;;;;;;;; ARDUINO ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def default-state {:port-name nil
                    :port nil
                    :a0 0})
(def state (atom default-state))


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

(def MSG_OUT_CONNECTION_REQUEST 255)
(def MAJOR_VERSION 0)
(def MINOR_VERSION 7)

(defn- read-from [in timeout]
  (let [[value _] (a/alts!! [in (a/timeout timeout)])]
    (if-not value
      (error "TIMEOUT!")
      value)))

(defn- request-connection [port in]
  (a/<!! (a/timeout 2000)) ; NOTE(Richo): Needed in Mac
  (s/write port [MSG_OUT_CONNECTION_REQUEST
                 MAJOR_VERSION
                 MINOR_VERSION])

  (println "Requesting connection...")
  ;(a/<!! (a/timeout 500)) ; TODO(Richo): Not needed in Mac

  (when-let [n1 (read-from in 1000)]
    (let [n2 (mod (+ MAJOR_VERSION MINOR_VERSION n1) 256)]
      (s/write port n2)
      ;(a/<!! (a/timeout 500)) ; TODO(Richo): Not needed in Mac
      (if (= n2 (read-from in 1000))
        (println "Connection accepted!")
        (println "Connection rejected")))))

(defn connect
  ([] (connect (first (available-ports))))
  ([port-name] (connect port-name 57600))
  ([port-name baud-rate]
   (if (connected?)
     (error "The board is already connected")
     (let [port (s/open port-name :baud-rate baud-rate)
           in (a/chan 1000)]
       (s/listen! port #(a/>!! in (.read %)))
       (request-connection port in)
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
(def server (atom nil))

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
    (when-not (ws/closed? socket)
      (ws/put! socket (str "seconds: " i))
      (a/<! (a/timeout 1000))
      (recur (inc i)))))

(defn echo-handler [socket _]
  (ws/connect socket socket))

(defn analog-read-handler [socket _]
  (a/go-loop []
    (when-not (ws/closed? socket)
      (ws/put! socket (str "A0: " (@state :a0)))
      (a/<! (a/timeout 100))
      (recur))))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body    (json/generate-string data)})

(defn connect-handler [params]
  (connect (params "port"))
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
                        (POST "/connect" {params :params} (connect-handler params))
                        (POST "/disconnect" req (disconnect-handler req))
                        (GET "/available-ports" [] (json-response {:ports (available-ports)}))
                        (route/not-found "No such page."))

      (wrap-params)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))

(defn start-server []
  (when (nil? @server)
    (let [s (http/start-server handler {:port 3000})]
      (reset! server s))))

(defn stop-server []
  (when-let [s @server]
    (reset! server nil)
    (.close s)))

(defn -main [& args]
  (time (do
          (println "Starting server...")
          (start-server)
          (println "Server started."))))
