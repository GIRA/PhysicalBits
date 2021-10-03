(ns middleware.core
  (:require [clojure.core.async :as a :refer [go go-loop <! >! timeout]]
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

(defn get-server-state
  ([] (get-server-state
       {:device-events (keys device-event-handlers)
        :logger-entries []}))
  ([{:keys [device-events logger-entries]}]
   (let [device-state @dc/state
         result (reduce (fn [update type]
                          (if-let [handler (device-event-handlers type)]
                            (merge update (handler device-state))
                            update))
                        {}
                        device-events)]
     (if-not (empty? logger-entries)
       (assoc result :output logger-entries)
       result))))

(def ^:private update-signal (atom nil))

(defn start-update-loop! [update-fn]
  (when (compare-and-set! update-signal
                          nil (a/chan (a/dropping-buffer 1)))
    (let [awake @update-signal
          pending (atom {:device-events #{}
                         :logger-entries []})
          dc-updates (a/tap dc/updates (a/chan))
          logger-updates (a/tap logger/updates (a/chan))
          chan->key {dc-updates :device-events
                     logger-updates :logger-entries}]
      (go
       (loop []
         (let [[val ch] (a/alts! (keys chan->key))]
           (when val
             (swap! pending update (chan->key ch) conj val)
             (when (>! awake true)
               (recur))))))
      (go
       (loop []
         (when (<! awake)
           (let [[changes _] (reset-vals! pending {:device-events #{}
                                                   :logger-entries []})
                 update-data (get-server-state changes)]
             (update-fn update-data)
             (<! (timeout 50))
             (recur))))
       (doseq [ch (keys chan->key)]
         (a/close! ch))))))

(defn stop-update-loop! []
  (let [[ch _] (reset-vals! update-signal nil)]
    (when ch (a/close! ch))))
