(ns middleware.core
  (:refer-clojure :exclude [run!])
  (:require [clojure.core.async :as a :refer [go <!]]
            [clojure.string :as str]
            [middleware.utils.core :refer [index-of]]
            [middleware.utils.json :as json]
            [middleware.utils.async :as aa :refer [go-try <?]]
            [middleware.utils.logger :as logger]
            [middleware.utils.eventlog :as elog]
            [middleware.utils.config :as config]
            [middleware.ast.utils :as ast]
            [middleware.program.utils :as program]
            [middleware.device.controller :as dc]
            [middleware.device.debugger :as debugger]
            [middleware.compilation.parser :as p]
            [middleware.compilation.codegen :as cg]
            [middleware.compilation.compiler :as cc]
            [middleware.compilation.encoder :as en]
            [middleware.ast.nodes :as nodes]))

(def ^:private updates (atom nil))

; TODO(Richo): Rename these maybe?
(def ^:private program-atom (atom nil))
(def ^:private program-chan (a/chan (a/sliding-buffer 1)))

(defn connect! [port & args]
  (go-try
   (elog/append "CORE/CONNECT" {:port port :args args})
   (<? (apply dc/connect! port args))
   (-> @dc/state :connection :port-name)))

(defn disconnect! []
  (go-try
   (elog/append "CORE/DISCONNECT")
   (<? (dc/disconnect!))
   (nil? (dc/connected?))))

(def connected? dc/connected?)

(defn compile-json-string [str & args]
  (let [ast (-> str json/decode cg/generate-tokens)]
    (vary-meta (apply cc/compile-tree ast args)
               assoc :type :json)))

(defn compile-uzi-string [str & args]
  (let [ast (-> str p/parse ast/generate-ids)]
    (vary-meta (apply cc/compile-tree ast args)
               assoc :type :uzi)))

(defn compilation-success [program bytecodes type silent?]
  (let [ast (-> program meta :final-ast)]
    (elog/append "CORE/COMPILATION_SUCCESS"
                 {:stats {:bytecodes (count bytecodes)
                          :instructions (count (program/pcs program))
                          :globals (count (:globals ast))
                          :imports (count (:imports ast))
                          :prims (count (:primitives ast))
                          :tasks (count (filter ast/task?
                                                (:scripts ast)))
                          :procs (count (filter ast/procedure?
                                                (:scripts ast)))
                          :funcs (count (filter ast/function?
                                                (:scripts ast)))}
                  :type (keyword type) :silent? silent?})))

(defn compilation-failed [src ex type silent?]
  (elog/append "CORE/COMPILATION_FAIL"
               {:src src :type (keyword type) :silent? silent?
                :ex {:description (ex-message ex)
                     :errors (if-let [{errors :errors} (ex-data ex)]
                               (mapv :description errors)
                               [])}}))

(defn compile! [src type silent? & args]
  (go-try
   (try
     (let [compile-fn (case type
                        "json" compile-json-string
                        "uzi" compile-uzi-string)
           program (apply compile-fn src args)
           bytecodes (en/encode program)]
       (when-not silent?
         (logger/success "Compilation successful!"))
       (reset! program-atom program)
       (a/put! program-chan true)
       (compilation-success program bytecodes type silent?)
       program)
     (catch #?(:clj Throwable :cljs :default) ex
       (compilation-failed src ex type silent?)
       (when-not silent?
         (logger/error "Compilation failed!")
         ; TODO(Richo): Improve the error message for checker errors
         #_(when-let [{errors :errors} (ex-data ex)]
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
   (elog/append "CORE/COMPILE_AND_RUN")
   (let [program (<? (apply compile! src type silent? args))]
     (debugger/run-program! program)
     program)))

(defn run! [program]
  (go-try
   (elog/append "CORE/RUN")
   (debugger/run-program! program)))

(defn compile-and-install! [src type & args]
  (go-try
   (elog/append "CORE/COMPILE_AND_INSTALL")
   (let [program (<? (apply compile! src type false args))]
     (dc/install program)
     program)))

(defn set-pin-report! [pins]
  (go-try
   (elog/append "CORE/SET_PIN_REPORT" (vec pins))
   (doseq [[pin-name report?] pins]
     (dc/set-pin-report pin-name report?))))

(defn set-global-report! [globals]
  (go-try
   (elog/append "CORE/SET_GLOBAL_REPORT" (vec globals))
   (doseq [[global-name report?] globals]
     (dc/set-global-report global-name report?))))

(defn set-pin-values! [pins]
  (go-try
   (elog/append "CORE/SET_PIN_VALUES" (vec pins))
   (doseq [[pin-name value] pins]
     (dc/set-pin-value pin-name value))))

(defn set-global-values! [globals]
  (go-try
   (elog/append "CORE/SET_GLOBAL_VALUES" (vec globals))
   (doseq [[global-name value] globals]
     (dc/set-global-value global-name value))))

(defn set-profile! [enabled?]
  (go-try
   (elog/append "CORE/SET_PROFILE" enabled?)
   (if enabled?
     (dc/start-profiling)
     (dc/stop-profiling))))

(defn debugger-break! []
  (elog/append "CORE/DEBUGGER_BREAK")
  (go-try (debugger/break!)))

(defn debugger-continue! []
  (elog/append "CORE/DEBUGGER_CONTINUE")
  (go-try (debugger/continue!)))


(defn debugger-step-over! []
  (elog/append "CORE/DEBUGGER_STEP_OVER")
  (go-try (debugger/step-over!)))

(defn debugger-step-into! []
  (elog/append "CORE/DEBUGGER_STEP_INTO")
  (go-try (debugger/step-into!)))

(defn debugger-step-out! []
  (elog/append "CORE/DEBUGGER_STEP_OUT")
  (go-try (debugger/step-out!)))

(defn debugger-step-next! []
  (elog/append "CORE/DEBUGGER_STEP_NEXT")
  (go-try (debugger/step-next!)))

(defn set-breakpoints! [breakpoints]
  (go-try
   (elog/append "CORE/DEBUGGER_SET_BREAKPOINTS" (vec breakpoints))
   (let [loc->pc (program/loc->pc (@dc/state :program))]
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
  {:tasks (->> (-> state :scripts vals)
               (filter :task?)
               (remove #(str/includes? (:name %) "."))
               (mapv (fn [s] {:scriptName (:name s)
                              :isRunning (:running? s)
                              :isError (:error? s)})))})

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
  (let [public-globals (->> (-> state :program :globals)
                            (keep :name)
                            (remove #(str/includes? % "."))
                            (set))]
    {:globals {:timestamp (-> state :globals :timestamp)
               :available (mapv (fn [global-name]
                                  {:name global-name
                                   :reporting (contains? (-> state :reporting :globals)
                                                         global-name)})
                                public-globals)
               :elements (mapv (-> state :globals :data)
                               public-globals)}}))

(defn- get-pseudo-vars-data [state]
  {:pseudo-vars {:timestamp (-> state :pseudo-vars :timestamp)
                 :available (mapv (fn [[name _]] {:name name :reporting true})
                                  (-> state :pseudo-vars :data))
                 :elements (-> state :pseudo-vars :data vals)}})

(defn- get-debugger-data [state]
  {:debugger (let [{:keys [index pc fp stack] :as vm-state} (-> state :debugger :vm)
                   breakpoints (-> state :debugger :breakpoints :user)
                   program (state :program)
                   stack-frames (debugger/stack-frames program vm-state)
                   sources (vec (distinct (map :source stack-frames)))]
               {:index index
                :pc pc
                :isHalted (some? pc)
                :breakpoints (let [pc->loc (program/pc->loc program)]
                               (mapv pc->loc breakpoints))
                :sources sources
                :stackFrames (mapv (fn [{:keys [script pc fp arguments locals stack source]}]
                                     {:scriptName (:name script)
                                      :pc pc
                                      :fp fp
                                      :interval (debugger/interval-at-pc program pc)
                                      :blocks (mapv (fn [pc]
                                                      (get-in (meta (program/instruction-at-pc program pc))
                                                              [:node :id]))
                                                    (-> program
                                                        (debugger/instruction-groups)
                                                        (debugger/instruction-group-at-pc pc)
                                                        :pcs))
                                      :arguments arguments
                                      :locals locals
                                      :stack stack
                                      :source (index-of sources source)})
                                   stack-frames)})})

(def ^:private device-update-handlers
  {:connection #'get-connection-data
   :pin-value #'get-pins-data
   :global-value #'get-globals-data
   :running-scripts #'get-tasks-data
   :free-ram #'get-memory-data
   :pseudo-vars #'get-pseudo-vars-data
   :debugger #'get-debugger-data})

(defn- get-device-state [state device-events]
  (reduce (fn [update type]
            (if-let [handler (device-update-handlers type)]
              (merge update (handler state))
              update))
          {}
          device-events))

(defn- get-program-state [program]
  (let [{type :type src :source ast :original-ast} (meta program)]
    {:program {:type type
               :src src
               :ast ast
               :compiled program
               :block->token (merge-with conj
                                         (ast/id->range ast)
                                         (ast/id->loc ast))}}))

(defn- get-logger-state [logger]
  {:output (vec logger)})

(defn- get-features []
  {:features (config/get :features {})})

(defn get-server-state
  ; TODO(Richo): The empty args overload is only used to initialize the clients
  ; when they first connect. I don't know if this is actually necessary, though...
  ; NOTE(Richo): It IS actually necessary
  ([] (get-server-state {:device (keys device-update-handlers)
                         :program true
                         :features true}))
  ([{:keys [device logger program features]}]
   (merge {}
          (when logger (get-logger-state logger))
          (when device (get-device-state @dc/state (set device)))
          (when program (get-program-state @program-atom))
          (when features (get-features)))))

(defn kv-chan
  "Returns a channel that associates everything we put into it with a key"
  [key]
  (a/chan 1 (map (partial vector key))))

(defn start-update-loop! [update-fn]
  (when (compare-and-set! updates nil :pending)
    (let [update-sources [(a/tap dc/updates (kv-chan :device))
                          (a/tap logger/updates (kv-chan :logger))
                          (a/pipe program-chan (kv-chan :program))]
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
