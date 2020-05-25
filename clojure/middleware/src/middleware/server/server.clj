(ns middleware.server.server
  (:require [clojure.core.async :as a]
            [compojure.core :as compojure :refer [GET POST]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [redirect]]
            [compojure.route :as route]
            [aleph.http :as http]
            [clojure.java.io :as io]
            [manifold.stream :as ws]
            [manifold.deferred :as d]
            [middleware.utils.json :as json]
            [middleware.device.controller :as device]
            [middleware.output.logger :as logger])
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
   :body    (json/encode data)})

(defn connect-handler [params]
  (device/connect (params "port"))
  (let [port-name (@device/state :port-name)]
    (json-response {:port-name port-name})))

(defn disconnect-handler [req]
  (device/disconnect)
  (json-response "OK"))

(defn compile-handler [{:strs [src type silent]
                        :or {type "uzi", silent true}}]
  (let [program (device/compile src type silent)]
    (json-response program)))

(defn run-handler [{:strs [src type silent]
                    :or {type "uzi", silent true}}]
  (let [program (device/compile src type silent)
        bytes (device/run program)]
    (json-response program)))

(defn- format-server-state [state output]
  (-> state

      ; NOTE(Richo): First we remove all the keys we don't need
      (dissoc :connected? :port :board :reporting :scripts :profiler)

      ; NOTE(Richo): And then we add the missing keys
      (assoc :isConnected (:connected? state)
             :pins {:available (mapv (fn [pin-name]
                                       {:name pin-name
                                        :reporting (contains? (-> state :reporting :pins)
                                                              pin-name)})
                                     (-> state :board :pin-names))
                    :elements (filterv (fn [pin] (contains? (-> state :reporting :pins)
                                                            (:name pin)))
                                       (-> state :pins vals))}
             :globals {:available (mapv (fn [{global-name :name}]
                                          {:name global-name
                                           :reporting (contains? (-> state :reporting :globals)
                                                                 global-name)})
                                        (filter :name
                                                (-> state :program :running :compiled :globals)))
                       :elements (filterv (fn [global] (contains? (-> state :reporting :globals)
                                                                  (:name global)))
                                          (-> state :globals vals))}
             :output output
             :tasks (mapv (fn [s] {:scriptName (:name s)
                                   :isRunning (:running? s)
                                   :isError (:error? s)})
                          (:scripts state)))))

(defn- get-server-state []
  (format-server-state @device/state
                       (logger/read-entries!)))

(def ^:private updates (a/chan))
(def ^:private updates-pub (a/pub updates :type))

; TODO(Richo): The update-loop could be started/stopped automatically
(def ^:private update-loop? (atom false))

(defn start-update-loop []
  (when (compare-and-set! update-loop? false true)
    (a/go-loop [old-state nil]
      (when @update-loop?
        (let [new-state (get-server-state)]
          (when (not= old-state new-state)
            (a/>! updates {:type :update, :state new-state}))
          (a/<! (a/timeout 100))
          (recur new-state))))))

(defn stop-update-loop []
  (reset! update-loop? false))

(defn uzi-state-handler [socket req]
  (let [in-chan (a/chan)
        topic :update]
    (ws/put! socket (json/encode (get-server-state)))
    (a/sub updates-pub topic in-chan)
    (a/go-loop []
      (if (ws/closed? socket)
        (a/unsub updates-pub topic in-chan)
        (let [{device-state :state} (a/<! in-chan)]
          (ws/put! socket (json/encode device-state))
          (recur))))))

(def handler
  (-> (compojure/routes (GET "/" [] (redirect "ide/index.html"))

                        ; Testing
                        (GET "/seconds" [] (wrap-websocket seconds-handler))
                        (GET "/echo" [] (wrap-websocket echo-handler))
                        (GET "/analog-read" [] (wrap-websocket analog-read-handler))


                        ; Uzi api
                        (GET "/uzi" [] (wrap-websocket uzi-state-handler))
                        (GET "/uzi/available-ports" [] (json-response {:ports (device/available-ports)}))
                        (POST "/uzi/connect" {params :params} (connect-handler params))
                        (POST "/uzi/disconnect" req (disconnect-handler req))
                        (POST "/uzi/compile" {params :params} (compile-handler params))
                        (POST "/uzi/run" {params :params} (run-handler params))

                        (route/not-found "No such page."))

      (wrap-params)
      (wrap-resource "public")
      (wrap-file "../../web")
      (wrap-content-type)
      (wrap-not-modified)))

(defn start []
  (when (nil? @server)
    (start-update-loop)
    (let [s (http/start-server handler {:port 3000})]
      (reset! server s))))

(defn stop []
  (when-let [s @server]
    (stop-update-loop)
    (reset! server nil)
    (.close s)))
