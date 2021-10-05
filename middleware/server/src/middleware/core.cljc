(ns middleware.core
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [middleware.device.controller :as dc]
            [middleware.output.logger :as logger]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.compiler.utils.program :as program]))

; TODO(Richo): Rename these maybe?
(def ^:private program-atom (atom nil))
(def ^:private program-chan (a/chan (a/sliding-buffer 1)))

(defn compile! [src type silent? & args]
  (try
    (let [compile-fn (case type
                       "json" cc/compile-json-string
                       "uzi" cc/compile-uzi-string)
          program (-> (apply compile-fn src args)
                      (update :compiled program/sort-globals)
                      (assoc :type type))
          bytecodes (en/encode (:compiled program))]
      (when-not silent?
        (logger/newline)
        (logger/log "Program size (bytes): %1" (count bytecodes))
        (logger/log (str bytecodes))
        (logger/success "Compilation successful!"))
      (reset! program-atom program)
      (a/put! program-chan true)
      program)
    (catch #?(:clj Throwable :cljs :default) ex
      (when-not silent?
        (logger/newline)
        (logger/exception ex)
        ; TODO(Richo): Improve the error message for checker errors
        (when-let [{errors :errors} (ex-data ex)]
          (doseq [[^long i {:keys [description node src]}]
                  (map-indexed (fn [i e] [i e])
                               errors)]
            (logger/error (str "├─ " (inc i) ". " description))
            (if src
              (logger/error (str "|     ..." src "..."))
              (when-let [id (:id node)]
                (logger/error (str "|     Block ID: " id)))))
          (logger/error (str "└─ Compilation failed!"))))
      (throw ex))))


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

(def ^:private device-event-handlers
  {:connection #'get-connection-data
   :pin-value #'get-pins-data
   :global-value #'get-globals-data
   :running-scripts #'get-tasks-data
   :free-ram #'get-memory-data
   :pseudo-vars #'get-pseudo-vars-data})

(defn- get-device-state [state device-events]
  (reduce (fn [update type]
            (if-let [handler (device-event-handlers type)]
              (merge update (handler state))
              update))
          {}
          device-events))

(defn- get-program-state [program]
  ; TODO(Richo): This sucks, the IDE should take the program without modification.
  ; Do we really need the final-ast? It would be simpler if we didn't have to make
  ; this change.
  {:program (-> program
                (select-keys [:type :src :compiled])
                (assoc :ast (:original-ast program)))})

(defn get-server-state
  ; TODO(Richo): The empty args overload is only needed to initialize the clients
  ; when they first connect. I don't know if this is actually necessary, though...
  ([] (get-server-state {:device (keys device-event-handlers)
                         :program true}))
  ([{:keys [device logger program]}]
   (merge {}
          (when logger {:output (vec logger)})
          (when device (get-device-state @dc/state (set device)))
          (when program (get-program-state @program-atom)))))

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
          program-updates (a/pipe program-chan
                                  (a/chan 1 (map (partial vector :program))))
          update-sources [device-updates logger-updates program-updates]
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
  ; TODO(Richo): If we try to stop while the value is :pending, we won't close the channels!!
  ; TODO(Richo): On second thought, I think we're safe because if the value is :pending, then
  ; the channel is not initialized yet and it will be set shortly after. Which means this won't
  ; have any effect. I think that's fine, stop is a NOP if the update-loop hasn't started yet.
  ; However, I will leave this comment here until I'm sure this is correct.
  (let [[ch _] (reset-vals! updates nil)]
    (when (and ch (not= :pending ch))
      (a/close! ch))))
