(ns middleware.server.udp
  (:require [clojure.core.async :as a :refer [thread <!!]]
            [middleware.utils.json :as json]
            [middleware.device.controller :as device]
            [middleware.config :as config])
  (:import (java.net InetAddress DatagramPacket DatagramSocket)))

(def server (atom nil))

; TODO(Richo): Hardcoded single client. Add support for multiple clients
(def ^:private SERVER_PORT 3232)
(def ^:private CLIENT_PORT 3234)

(def ^:private update-loop? (atom false))

(defn get-server-state []
  (let [state @device/state]
    {:pins {:timestamp (-> state :pins :timestamp)
            :available (mapv (fn [pin-name]
                               {:name pin-name
                                :reporting (contains? (-> state :reporting :pins)
                                                      pin-name)})
                             (-> state :board :pin-names))
            :elements (filterv (fn [pin] (contains? (-> state :reporting :pins)
                                                    (:name pin)))
                               (-> state :pins :data vals))}
     :globals {:timestamp (-> state :globals :timestamp)
               :available (mapv (fn [{global-name :name}]
                                  {:name global-name
                                   :reporting (contains? (-> state :reporting :globals)
                                                         global-name)})
                                (filter :name
                                        (-> state :program :running :compiled :globals)))
               :elements (filterv (fn [global] (contains? (-> state :reporting :globals)
                                                          (:name global)))
                                  (-> state :globals :data vals))}}))

(defn start-update-loop []
  (when (compare-and-set! update-loop? false true)
    (thread
     (loop [old-state nil]
       (when @update-loop?
         (let [new-state (get-server-state)]
           (when (not= old-state new-state)
             (let [^String json-state (json/encode new-state)]
               (when-let [^DatagramSocket udp @server]
                 (.send udp
                        (DatagramPacket. (.getBytes json-state)
                                         (.length json-state)
                                         (InetAddress/getByName "localhost")
                                         CLIENT_PORT)))))
           (<!! (a/timeout (config/get-in [:udp :interval] 10)))
           (recur new-state)))))))

(comment
  (stop-update-loop)
  (start-update-loop)
 ,)

(defn stop-update-loop []
  (reset! update-loop? false))

(defn start []
  (when (nil? @server)
    (start-update-loop)
    (let [s (DatagramSocket. SERVER_PORT)]
      (reset! server s))))

(defn stop []
  (when-let [^DatagramSocket s @server]
    (stop-update-loop)
    (reset! server nil)
    (.close s)))
