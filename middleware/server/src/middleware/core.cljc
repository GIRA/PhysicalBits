(ns middleware.core
  (:refer-clojure :exclude [run!])
  (:require [clojure.core.async :as a :refer [go go-loop <! >!]]
            [clojure.string :as str]
            [middleware.utils.json :as json]
            [middleware.utils.async :as aa :refer [go-try <?]]
            [middleware.utils.logger :as logger]
            [middleware.program.utils :as program]
            [middleware.device.controller :as dc]
            [middleware.device.debugger :as debugger]
            [middleware.compilation.parser :as p]
            [middleware.compilation.codegen :as cg]
            [middleware.compilation.compiler :as cc]
            [middleware.compilation.encoder :as en]))

; TODO(Richo): Rename these maybe?
(def ^:private program-atom (atom nil))
(def ^:private program-chan (a/chan (a/sliding-buffer 1)))

(defn connect! [port & args]
  (go-try
   (<? (apply dc/connect! port args))
   (-> @dc/state :connection :port-name)))

(defn disconnect! []
  (go-try
   (<? (dc/disconnect!))
   (nil? (dc/connected?))))

(def connected? dc/connected?)

(defn compile-json-string [str & args]
  (let [ast (-> str json/decode cg/generate-tokens)]
    (vary-meta (apply cc/compile-tree ast args)
               assoc :type :json)))

(defn compile-uzi-string [str & args]
  (let [ast (p/parse str)]
    (vary-meta (apply cc/compile-tree ast args)
               assoc :type :uzi)))

(defn compile! [src type silent? & args]
  (go-try
   (try
     (let [compile-fn (case type
                        "json" compile-json-string
                        "uzi" compile-uzi-string)
           program (apply compile-fn src args)
           bytecodes (en/encode program)]
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
       (throw ex)))))

(defn compile-and-run! [src type silent? & args]
  (go-try
   (let [program (<? (apply compile! src type silent? args))]
     (dc/run program)
     program)))

(defn run! [program]
  (go-try (dc/run program)))

(defn compile-and-install! [src type & args]
  (go-try
   (let [program (<? (apply compile! src type false args))]
     (dc/install program)
     program)))

(defn set-pin-report! [pins]
  (go-try
   (doseq [[pin-name report?] pins]
     (dc/set-pin-report pin-name report?))))

(defn set-global-report! [globals]
  (go-try
   (doseq [[global-name report?] globals]
     (dc/set-global-report global-name report?))))

(defn set-profile! [enabled?]
  (go-try
   (if enabled?
     (dc/start-profiling)
     (dc/stop-profiling))))

(defn debugger-break! []
  (go-try (debugger/break!)))

(defn debugger-continue! []
  (go-try (debugger/continue!)))

(defn debugger-step-over! []
  (go-try (debugger/step-over!)))

(defn debugger-step-into! []
  (go-try (debugger/step-into!)))

(defn debugger-step-out! []
  (go-try (debugger/step-out!)))

(defn debugger-step-next! []
  (go-try (debugger/step-next!)))

(defn set-breakpoints! [breakpoints]
  (go-try
   (let [loc->pc (program/loc->pc (-> @dc/state :program :running))]
     ; TODO(Richo): If loc->pc returns nil try the next loc?
     (debugger/set-user-breakpoints! (keep loc->pc breakpoints)))))

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
                                      (-> state :program :running :globals)))
             :elements (filterv (fn [global] (contains? (-> state :reporting :globals)
                                                        (:name global)))
                                (-> state :globals :data vals))}})

(defn- get-pseudo-vars-data [state]
  {:pseudo-vars {:timestamp (-> state :pseudo-vars :timestamp)
                 :available (mapv (fn [[name _]] {:name name :reporting true})
                                  (-> state :pseudo-vars :data))
                 :elements (-> state :pseudo-vars :data vals)}})

(defn- get-debugger-data [state]
  {:debugger (let [{:keys [index pc fp stack] :as vm-state} (-> state :debugger :vm)
                   breakpoints (-> state :debugger :breakpoints :user)
                   program (-> state :program :running)
                   stack-frames (debugger/stack-frames program vm-state)]
               {
                ; TODO(Richo): Added just for debugging
                :_breakpoints {:usr (-> @dc/state :debugger :breakpoints :user sort)
                               :sys (-> @dc/state :debugger :breakpoints :system sort)
                               :step-over (debugger/estimate-breakpoints debugger/step-over)
                               :step-into (debugger/estimate-breakpoints debugger/step-into)
                               :step-out (debugger/estimate-breakpoints debugger/step-out)}
                :index index
                :isHalted (some? pc)
                :breakpoints (let [pc->loc (program/pc->loc program)]
                               (mapv pc->loc breakpoints))
                :stackFrames (mapv (fn [{:keys [script pc fp arguments locals stack]}]
                                      {:scriptName (:name script)
                                       :pc pc
                                       :fp fp
                                       :interval (debugger/interval-at-pc program pc)
                                       :arguments arguments
                                       :locals locals
                                       :stack stack})
                                    stack-frames)})})

(def ^:private device-event-handlers
  {:connection #'get-connection-data
   :pin-value #'get-pins-data
   :global-value #'get-globals-data
   :running-scripts #'get-tasks-data
   :free-ram #'get-memory-data
   :pseudo-vars #'get-pseudo-vars-data
   :debugger #'get-debugger-data})

(defn- get-device-state [state device-events]
  (reduce (fn [update type]
            (if-let [handler (device-event-handlers type)]
              (merge update (handler state))
              update))
          {}
          device-events))

(defn- get-program-state [program]
  (let [{type :type src :source ast :original-ast} (meta program)]
    {:program {:type type
               :src src
               :ast ast
               :compiled program}}))

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
                (->> (<! (aa/reduce-until-timeout! conj [update] updates* timeout))
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
