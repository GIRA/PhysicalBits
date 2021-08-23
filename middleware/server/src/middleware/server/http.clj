(ns middleware.server.http
  (:require [clojure.core.async :as a :refer [<!! >!! thread]]
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
            [clojure.java.io :as io]
            [manifold.stream :as ws]
            [manifold.deferred :as d]
            [middleware.utils.json :as json]
            [middleware.device.controller :as dc]
            [middleware.output.logger :as logger]
            [middleware.config :as config])
  (:import [manifold.stream.core IEventSink]))

(def server (atom nil))

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

(defn connect-handler [params]
  (dc/connect! (params "port"))
  (let [port-name (@dc/state :port-name)]
    (json-response {:port-name port-name})))

(defn disconnect-handler [req]
  (dc/disconnect!)
  (json-response "OK"))

(defn compile-handler
  [uzi-libraries
   {:strs [src type silent] :or {type "uzi", silent "true"}}]
  (let [program (dc/compile src type (= silent "true")
                            :lib-dir uzi-libraries)]
    (json-response program)))

(defn run-handler
  [uzi-libraries
   {:strs [src type silent] :or {type "uzi", silent "true"}}]
  (let [program (dc/compile src type (= silent "true")
                                :lib-dir uzi-libraries)]
    (dc/run program)
    (json-response program)))

(defn install-handler [uzi-libraries
                       {:strs [src type] :or {type "uzi"}}]
  (let [program (dc/compile src type false
                            :lib-dir uzi-libraries)]
    (dc/install program)
    (json-response program)))

(defn pin-report-handler [{:strs [pins report] :or {pins "", report ""}}]
  (let [pins (filterv (complement empty?)
                      (str/split pins #","))
        report (mapv #(= % "true")
                     (filter (complement empty?)
                             (str/split report #",")))]
    (when-not (= (count pins) (count report))
      (json-response "Invalid request parameters" 400))
    (doseq [pin-name pins
            report? report]
      (dc/set-pin-report pin-name report?))
    (json-response "OK")))

(defn global-report-handler [{:strs [globals report] :or {globals "", report ""}}]
  (let [globals (filterv (complement empty?)
                         (str/split globals #","))
        report (mapv #(= % "true")
                     (filter (complement empty?)
                             (str/split report #",")))]
    (when-not (= (count globals) (count report))
      (json-response "Invalid request parameters" 400))
    (doseq [global-name globals
            report? report]
      (dc/set-global-report global-name report?))
    (json-response "OK")))

(defn profile-handler [{:strs [enabled]}]
  (if (= "true" enabled)
    (dc/start-profiling)
    (dc/stop-profiling))
  (json-response "OK"))

(defn- get-connection-data [{:keys [connection]}]
  {:isConnected (when connection @(:connected? connection))
   :portName (:port-name connection)
   :availablePorts (dc/available-ports)})

(defn- get-memory-data [state]
  (:memory state))

(defn- get-tasks-data [state]
  (mapv (fn [s] {:scriptName (:name s)
                 :isRunning (:running? s)
                 :isError (:error? s)})
        (filter :task? (-> state :scripts vals))))

(defn- get-pins-data [state]
  {:timestamp (-> state :pins :timestamp)
   :available (mapv (fn [pin-name]
                      {:name pin-name
                       :reporting (contains? (-> state :reporting :pins)
                                             pin-name)})
                    (-> state :board :pin-names))
   :elements (filterv (fn [pin] (contains? (-> state :reporting :pins)
                                           (:name pin)))
                      (-> state :pins :data vals))})

(defn- get-globals-data [state]
  {:timestamp (-> state :globals :timestamp)
   :available (mapv (fn [{global-name :name}]
                      {:name global-name
                       :reporting (contains? (-> state :reporting :globals)
                                             global-name)})
                    (filter :name
                            (-> state :program :running :compiled :globals)))
   :elements (filterv (fn [global] (contains? (-> state :reporting :globals)
                                              (:name global)))
                      (-> state :globals :data vals))})

(defn- get-pseudo-vars-data [state]
  {:timestamp (-> state :pseudo-vars :timestamp)
   :available (mapv (fn [[name _]] {:name name :reporting true})
                    (-> state :pseudo-vars :data))
   :elements (-> state :pseudo-vars :data vals)})

(defn- get-program-data [state]
  (let [program (-> state :program :current)]
    (-> program
        (select-keys [:type :src :compiled])
        (assoc :ast (:original-ast program)))))

(defn- get-output-data []
  (logger/read-entries!))

(defn- get-server-state []
  (let [state @dc/state]
    {:connection (get-connection-data state)
     :memory (get-memory-data state)
     :tasks (get-tasks-data state)
     :output (get-output-data)
     :pins (get-pins-data state)
     :globals (get-globals-data state)
     :pseudo-vars (get-pseudo-vars-data state)
     :program (get-program-data state)}))

(defn- get-state-diff [old-state new-state]
  (select-keys new-state
               (filter #(not= (% old-state) (% new-state))
                       (keys new-state))))

(def ^:private updates (a/chan))
(def ^:private updates-pub (a/pub updates :type))

; TODO(Richo): The update-loop could be started/stopped automatically
(def ^:private update-loop? (atom false))

; TODO(Richo): This loop sucks, it would be better if we could subscribe
; to the update events coming from the device.
(defn start-update-loop []
  (when (compare-and-set! update-loop? false true)
    (thread
     (loop [old-state nil]
       (when @update-loop?
         (let [new-state (get-server-state)
               diff (get-state-diff old-state new-state)]
           (when-not (empty? diff)
             (>!! updates {:type :update, :state (json/encode diff)}))
           (<!! (a/timeout (config/get-in [:http :interval] 100)))
           (recur new-state)))))))

(defn stop-update-loop []
  (reset! update-loop? false))

(comment
 (stop-update-loop)
 (start-update-loop)
 ,)

(defn uzi-state-handler [^IEventSink socket req]
  (let [in-chan (a/chan)
        topic :update]
    (ws/on-closed socket
                  (fn []
                    (a/unsub updates-pub topic in-chan)
                    (a/close! in-chan)))
    (ws/put! socket (json/encode (get-server-state)))
    (a/sub updates-pub topic in-chan)
    (a/go-loop []
      (when-not (ws/closed? socket)
        (let [{device-state :state} (a/<! in-chan)]
          (ws/put! socket device-state)
          (recur))))))

(defn- create-handler [uzi-libraries web-resources]
  (-> (compojure/routes (GET "/" [] (redirect "ide/index.html"))

                        ; Uzi api
                        (GET "/uzi" [] (wrap-websocket uzi-state-handler))
                        (GET "/uzi/available-ports" [] (json-response {:ports (dc/available-ports)}))
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
    (start-update-loop)
    (let [s (http/start-server (create-handler uzi-libraries web-resources)
                               {:port server-port})]
      (reset! server s))))

(defn stop []
  (when-let [^java.io.Closeable s @server]
    (stop-update-loop)
    (reset! server nil)
    (.close s)))
