(ns middleware.server.udp
  (:require [clojure.core.async :as a :refer [thread <!!]]
            [clojure.tools.logging :as log]
            [middleware.utils.json :as json]
            [middleware.device.controller :as device]
            [middleware.utils.config :as config])
  (:import (java.net InetAddress DatagramPacket DatagramSocket)))

(def server (atom nil))

; TODO(Richo): Hardcoded single client. Add support for multiple clients
(def ^:const SERVER_PORT 3232)
(def ^:const CLIENT_PORT 3234)

(def ^:private update-loop? (atom false))

(defn get-server-state []
  (let [state @device/state]
    {:pins (-> state :pins :data vals)
     :globals (-> state :globals :data vals)}))

(defn- send! [data]
  (when-let [^DatagramSocket udp @server]
    (let [^String json (json/encode data)
          buf (.getBytes json "utf8")
          packet (DatagramPacket. buf
                                  (count buf)
                                  (InetAddress/getByName "localhost")
                                  CLIENT_PORT)]
      (.send udp packet))))

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
                  "init" (send! (get-server-state))
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
           (send! new-state))
         (<!! (a/timeout (config/get-in [:udp :interval] 10)))
         (recur new-state))))))

(defn start-update-loop []
  (when (compare-and-set! update-loop? false true)
    (start-incoming-thread)
    (start-outgoing-thread)))

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
