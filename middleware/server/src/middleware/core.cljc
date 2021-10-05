(ns middleware.core
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [middleware.device.controller :as dc]
            [middleware.output.logger :as logger]))

(defn- get-connection-data [{:keys [connection]}]
  {:connection {; TODO(Richo): The server should already receive the data correctly formatted...
                :isConnected (when (and (not= :pending connection)
                                        (some? connection))
                               @(:connected? connection))
                :portName (:port-name connection)
                :availablePorts (dc/available-ports)}})

(defn- get-memory-data [state]
  {:memory (:memory state)})

(defn- get-tasks-data [state]
  {:tasks (mapv (fn [s] {:scriptName (:name s)
                         :isRunning (:running? s)
                         :isError (:error? s)})
                (filter :task? (-> state :scripts vals)))})

(defn- get-pins-data [state]
  {:pins {:timestamp (-> state :pins :timestamp)
          :available (mapv (fn [pin-name]
                             {:name pin-name
                              :reporting (contains? (-> state :reporting :pins)
                                                    pin-name)})
                           (-> state :board :pin-names))
          :elements (filterv (fn [pin] (contains? (-> state :reporting :pins)
                                                  (:name pin)))
                             (-> state :pins :data vals))}})

(defn- get-globals-data [state]
  {:globals {:timestamp (-> state :globals :timestamp)
             :available (mapv (fn [{global-name :name}]
                                {:name global-name
                                 :reporting (contains? (-> state :reporting :globals)
                                                       global-name)})
                              (filter :name
                                      (-> state :program :running :compiled :globals)))
             :elements (filterv (fn [global] (contains? (-> state :reporting :globals)
                                                        (:name global)))
                                (-> state :globals :data vals))}})

(defn- get-pseudo-vars-data [state]
  {:pseudo-vars {:timestamp (-> state :pseudo-vars :timestamp)
                 :available (mapv (fn [[name _]] {:name name :reporting true})
                                  (-> state :pseudo-vars :data))
                 :elements (-> state :pseudo-vars :data vals)}})

(defn- get-program-data [state]
  {:program (let [program (-> state :program :current)]
              ; TODO(Richo): This sucks, the IDE should take the program without modification.
              ; Do we really need the final-ast? It would be simpler if we didn't have to make
              ; this change.
              (-> program
                  (select-keys [:type :src :compiled])
                  (assoc :ast (:original-ast program))))})

(def ^:private device-event-handlers
  {:connection #'get-connection-data
   :program #'get-program-data
   :pin-value #'get-pins-data
   :global-value #'get-globals-data
   :running-scripts #'get-tasks-data
   :free-ram #'get-memory-data})

(defn- get-device-state [state device-events]
  (reduce (fn [update type]
            (if-let [handler (device-event-handlers type)]
              (merge update (handler state))
              update))
          {}
          device-events))

(defn get-server-state
  ([] (get-server-state {:device (keys device-event-handlers)}))
  ([{:keys [device logger]}]
   (merge {}
          (when logger {:output (vec logger)})
          (when device (get-device-state @dc/state (set device))))))

(defn reduce-until-timeout!
  "Take from channel ch while data is available (without blocking/parking) until
  the timeout or no more data is immediately available.
  Results are accumulated using the reducer function f and the initial value init."
  [f init ch t]
  (go (loop [ret init]
        (let [[val _] (a/alts! [t (go (a/poll! ch))])]
          (if val
            (recur (f ret val))
            ret)))))

(def ^:private updates (atom nil))

(defn start-update-loop! [update-fn]
  (when (compare-and-set! updates nil :pending)
    (let [device-updates (a/tap dc/updates
                                (a/chan 1 (map (partial vector :device))))
          logger-updates (a/tap logger/updates
                                (a/chan 1 (map (partial vector :logger))))
          update-sources [device-updates logger-updates]
          updates* (reset! updates (a/merge update-sources))]
      (go (loop []
            (when-some [update (<! updates*)] ; Park until first update
              (let [timeout (a/timeout 50)]
                (<! (a/timeout 10)) ; Wait a bit before collecting data
                (->> (<! (reduce-until-timeout! conj [update] updates* timeout))
                     (group-by first)
                     (reduce-kv #(assoc %1 %2 (map second %3)) {})
                     get-server-state
                     update-fn)
                (<! timeout) ; Wait remaining timeout
                (recur))))
          (doseq [ch update-sources]
            (a/close! ch))))))

(defn stop-update-loop! []
  (let [[ch _] (reset-vals! updates nil)]
    (when (and ch (not= :pending ch))
      (a/close! ch))))
