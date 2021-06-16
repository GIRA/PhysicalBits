(ns middleware.server.udp
  (:require [clojure.core.async :as a :refer [thread <!!]]
            [clojure.tools.logging :as log]
            [middleware.utils.json :as json]
            [middleware.device.controller :as device]
            [middleware.config :as config])
  (:import (java.net InetAddress DatagramPacket DatagramSocket)))

(def server (atom nil))

; TODO(Richo): Hardcoded single client. Add support for multiple clients
(def ^:const SERVER_PORT 3232)
(def ^:const CLIENT_PORT 3234)

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

(defn- start-incoming-thread []
  (thread
   (let [buf (byte-array 256)
         packet (DatagramPacket. buf (count buf))]
     (loop []
       (when @update-loop?
         (when-let [^DatagramSocket udp @server]
           (.receive udp packet)
           (let [msg (String. buf 0 (.getLength packet) "utf8")
                 {:keys [action name value]} (json/decode msg)]
             (println ">>>" msg)
             (try
               (case action
                 "set_pin_value" (device/set-pin-value name value)
                 "set_global_value" (device/set-global-value name value))
               (catch Throwable e (log/error "ERROR WHILE RECEIVING (udp) ->" e)))))
         (recur))))))


(defn- start-outgoing-thread []
  (thread
   (loop [old-state nil]
     (when @update-loop?
       (let [new-state (get-server-state)]
         (when (not= old-state new-state)
           (let [^String json-state (json/encode new-state)]
             (when-let [^DatagramSocket udp @server]
               (let [buf (.getBytes json-state "utf8")
                     packet (DatagramPacket. buf
                                             (count buf)
                                             (InetAddress/getByName "localhost")
                                             CLIENT_PORT)]
                 (.send udp packet)))))
         (<!! (a/timeout (config/get-in [:udp :interval] 10)))
         (recur new-state))))))

(defn start-update-loop []
  (when (compare-and-set! update-loop? false true)
    (start-incoming-thread)
    (start-outgoing-thread)))

(comment
  (stop)
  (start)

  @server
 ,)

(defn stop-update-loop []
  (reset! update-loop? false))

(defn start []
  (when (and (nil? @server)
             (config/get-in [:udp :enabled?] true))
    (start-update-loop)
    (let [s (DatagramSocket. SERVER_PORT)]
      (reset! server s))))

(defn stop []
  (when-let [^DatagramSocket s @server]
    (stop-update-loop)
    (reset! server nil)
    (.close s)))
