(ns middleware.server.http
  (:require [clojure.core.async :as a :refer [>!! thread]]
            [middleware.utils.async :refer [<??]]
            [clojure.string :as str]
            [compojure.core :as compojure :refer [GET POST]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file :refer [wrap-file]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.util.response :refer [redirect]]
            [compojure.route :as route]
            [aleph.http :as http]
            [manifold.stream :as ws]
            [manifold.deferred :as d]
            [middleware.core :as core]
            [middleware.utils.json :as json]
            [middleware.config :as config])
  (:import [manifold.stream.core IEventSink]))

(def server (atom nil))
(def clients (atom #{}))

(defn notify-clients! [data]
  (doseq [socket @clients]
    (when-not (ws/closed? socket)
      (ws/put! socket data))))

(defn wrap-websocket [handler]
  (fn [req]
    (-> (d/let-flow [socket (http/websocket-connection req)]
                    (handler socket req)
                    nil)
        (d/catch {:status 400
                  :headers {"content-type" "application/text"}
                  :body "Expected a websocket request."}))))

(defn json-response [data & [status]]
  {:status  (or status 200)
   :headers {"Content-Type" "application/hal+json; charset=utf-8"}
   :body    (json/encode data)})

(defn update-handler [^IEventSink socket req]
  (swap! clients conj socket)
  (ws/on-closed socket #(swap! clients disj socket))
  (ws/put! socket (json/encode (core/get-server-state))))

(defn connect-handler [params]
  (json-response (<?? (core/connect! (params "port")))))

(defn disconnect-handler [req]
  (json-response (<?? (core/disconnect!))))

(defn compile-handler
  [uzi-libraries
   {:strs [src type silent] :or {type "uzi", silent "true"}}]
  (json-response (<?? (core/compile! src type (= silent "true")
                                     :lib-dir uzi-libraries))))

(defn run-handler
  [uzi-libraries
   {:strs [src type silent] :or {type "uzi", silent "true"}}]
  (json-response (<?? (core/compile-and-run! src type (= silent "true")
                                             :lib-dir uzi-libraries))))

(defn install-handler [uzi-libraries
                       {:strs [src type] :or {type "uzi"}}]
  (json-response (<?? (core/compile-and-install! src type
                                                 :lib-dir uzi-libraries))))

(defn pin-report-handler [{:strs [pins report] :or {pins "", report ""}}]
  (let [pins (remove empty? (str/split pins #","))
        report (map (partial = "true")
                    (remove empty? (str/split report #",")))]
    (if-not (= (count pins) (count report))
      (json-response "Invalid request parameters" 400)
      (json-response (<?? (core/set-pin-report! (map vector pins report)))))))

(defn global-report-handler [{:strs [globals report] :or {globals "", report ""}}]
  (let [globals (remove empty? (str/split globals #","))
        report (map (partial = "true")
                    (remove empty? (str/split report #",")))]
    (if-not (= (count globals) (count report))
      (json-response "Invalid request parameters" 400)
      (json-response (<?? (core/set-global-report! (map vector globals report)))))))

(defn profile-handler [{:strs [enabled]}]
  (json-response (<?? (core/set-profile! (= "true" enabled)))))

(defn- create-handler [uzi-libraries web-resources]
  (-> (compojure/routes (GET "/" [] (redirect "ide/index.html"))

                        ; Uzi api
                        (GET "/uzi" [] (wrap-websocket update-handler))
                        (POST "/uzi/connect" {params :params} (connect-handler params))
                        (POST "/uzi/disconnect" req (disconnect-handler req))
                        (POST "/uzi/compile" {params :params} (compile-handler uzi-libraries params))
                        (POST "/uzi/run" {params :params} (run-handler uzi-libraries params))
                        (POST "/uzi/install" {params :params} (install-handler uzi-libraries params))
                        (POST "/uzi/pin-report" {params :params} (pin-report-handler params))
                        (POST "/uzi/global-report" {params :params} (global-report-handler params))
                        (POST "/uzi/profile" {params :params} (profile-handler params))

                        (route/not-found "No such page."))

      (wrap-params)
      (wrap-resource "public")
      (wrap-file web-resources)
      (wrap-content-type)
      (wrap-not-modified)))

(defn start [& {:keys [uzi-libraries web-resources server-port]
                :or {uzi-libraries "../../uzi/libraries"
                     web-resources "../../gui"
                     server-port 3000}}]
  (when (and (nil? @server)
             (config/get-in [:http :enabled?] true))
    (core/start-update-loop! (comp notify-clients! json/encode))
    (let [s (http/start-server (create-handler uzi-libraries web-resources)
                               {:port server-port})]
      (reset! server s))))

(defn stop []
  (when-let [^java.io.Closeable s @server]
    (core/stop-update-loop!)
    (doseq [socket @clients]
      (ws/close! socket))
    (reset! clients #{})
    (reset! server nil)
    (.close s)))
