(ns middleware.server.udp-server
  (:require [clojure.core.async :as a]
            [middleware.utils.json :as json]
            [middleware.device.controller :as device])
  (:import (java.net InetAddress DatagramPacket DatagramSocket)))

(def udp-server (atom nil))

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
    (a/go-loop [old-state nil]
      (when @update-loop?
        (let [new-state (get-server-state)]
          (when (not= old-state new-state)
            (let [json-state (json/encode new-state)]
              (when-let [udp @udp-server]
                (.send udp
                       (DatagramPacket. (.getBytes json-state)
                                        (.length json-state)
                                        (InetAddress/getByName "localhost")
                                        3234)))))
          (a/<! (a/timeout 10))
          (recur new-state))))))

(defn stop-update-loop []
  (reset! update-loop? false))

(defn start []
  (when (nil? @udp-server)
    (start-update-loop)
    (let [s (DatagramSocket. 3232)]
      (reset! udp-server s))))

(defn stop []
  (when-let [s @udp-server]
    (stop-update-loop)
    (reset! udp-server nil)
    (.close s)))
