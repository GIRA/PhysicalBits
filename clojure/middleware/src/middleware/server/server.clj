(ns middleware.server.server
  (:require [clojure.core.async :as a]
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
            [cheshire.core :as json]
            [middleware.device.controller :as device]
            [middleware.compiler.compiler :as cc])
  (:gen-class))

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
      (ws/put! socket (str "A0: " (device/get-pin-value "A0")))
      (a/<! (a/timeout 100))
      (recur))))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body    (json/generate-string data)})

(defn connect-handler [params]
  (device/connect (params "port"))
  (let [port-name (@device/state :port-name)]
    (json-response {:port-name port-name})))

(defn disconnect-handler [req]
  (device/disconnect)
  (json-response "OK"))

(defn compile-handler [params]
  (let [program (cc/compile-uzi-string (params "src")
                                       :lib-dir (params "libs"))]
    (json-response program)))

(def handler
  (-> (compojure/routes (GET "/" [] (io/resource "public/index.html"))
                        (GET "/seconds" [] (wrap-websocket seconds-handler))
                        (GET "/echo" [] (wrap-websocket echo-handler))
                        (GET "/analog-read" [] (wrap-websocket analog-read-handler))
                        (POST "/connect" {params :params} (connect-handler params))
                        (POST "/disconnect" req (disconnect-handler req))
                        (GET "/available-ports" [] (json-response {:ports (device/available-ports)}))
                        (POST "/compile" {params :params} (compile-handler params))
                        (route/not-found "No such page."))

      (wrap-params)
      (wrap-resource "public")
      (wrap-content-type)
      (wrap-not-modified)))

(defn start []
  (when (nil? @server)
    (let [s (http/start-server handler {:port 3000})]
      (reset! server s))))

(defn stop []
  (when-let [s @server]
    (reset! server nil)
    (.close s)))
