(ns middleware.core
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.output.logger :as logger]))

; TODO(Richo): The update-loop could be started/stopped automatically
(def ^:private update-loop? (atom false))

(defn- get-connection-data [{:keys [connection]}]
  {; TODO(Richo): The server should already receive the data correctly formatted...
   :isConnected (when (and (not= :pending connection)
                           (some? connection))
                  @(:connected? connection))
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
    ; TODO(Richo): This sucks, the IDE should take the program without modification.
    ; Do we really need the final-ast? It would be simpler if we didn't have to make
    ; this change.
    (-> program
        (select-keys [:type :src :compiled])
        (assoc :ast (:original-ast program)))))

(defn- get-output-data []
  (logger/read-entries!))

(defn get-server-state []
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

; TODO(Richo): This loop sucks, it would be better if we could subscribe
; to the update events coming from the device.
(defn start-update-loop! [update-fn]
  (when (compare-and-set! update-loop? false true)
    (go-loop [old-state nil]
      (when @update-loop?
        (let [new-state (get-server-state)
              diff (get-state-diff old-state new-state)]
          (when-not (empty? diff)
            (update-fn diff))
          (<! (a/timeout 50))
          (recur new-state))))))

(defn stop-update-loop! []
  (reset! update-loop? false))
