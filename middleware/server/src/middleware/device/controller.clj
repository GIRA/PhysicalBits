(ns middleware.device.controller
  (:refer-clojure :exclude [send compile])
  (:require [clojure.tools.logging :as log]
            [middleware.device.ports.scanner :as port-scanner]
            [clojure.string :as str]
            [clojure.core.async :as a :refer [<! go go-loop timeout]]
            [middleware.device.ports.common :as ports]
            [middleware.device.protocol :as p]
            [middleware.device.boards :refer :all]
            [middleware.utils.conversions :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.compiler.utils.ast :as ast]
            [middleware.compiler.utils.program :as program]
            [middleware.output.logger :as logger]
            [middleware.config :as config]
            [middleware.device.utils.ring-buffer :as rb]))

(comment

 (start-profiling)
 (stop-profiling)

 (set-report-interval 5)
 ,)

(defn millis ^long [] (System/currentTimeMillis))
(defn constrain [^long val ^long lower ^long upper] (max lower (min upper val)))

(def initial-state {:connection nil
                    :board UNO
                    :reporting {:pins #{}, :globals #{}}
                    :pins {}
                    :globals {}
                    :pseudo-vars {:timestamp nil, :data {}}
                    :scripts []
                    :profiler nil
                    :debugger nil
                    :timing {:arduino nil, :middleware nil, :diffs nil}
                    :memory {:arduino nil, :uzi nil}
                    :program {:current nil, :running nil}})
(def state (atom initial-state)) ; TODO(Richo): Make it survive reloads

(defn- add-pseudo-var! [name value]
  (if (config/get-in [:device :pseudo-vars?] false)
    (let [now (or (-> @state :timing :arduino) 0)]
      (swap! state
             #(-> %
                  (assoc-in [:pseudo-vars :timestamp] now)
                  (assoc-in [:pseudo-vars :data name]
                            {:name name, :value value, :ts now}))))))

(defn get-pin-value [pin-name]
  (-> @state :pins (get pin-name) :value))

(defn available-ports []
  @port-scanner/available-ports)

(defn connected? []
  (when-let [connection (-> @state :connection)]
    (ports/connected? connection)))

(defn disconnect! []
  (when-let [connection (@state :connection)]
    (swap! state
           #(-> initial-state
                ; Keep the current program
                (assoc-in [:program :current]
                          (-> % :program :current))))
    (logger/error "Connection lost!")
    (try
      (ports/disconnect! connection)
      (catch Throwable e
        (log/error "ERROR WHILE DISCONNECTING ->" e)))
    (port-scanner/start!)))

(defn send [bytes]
  (when-let [out (-> @state :connection :out)]
    (when-not (a/put! out bytes)
      (disconnect!)))
  bytes)


(defn- get-global-number [global-name]
  (program/index-of-variable (-> @state :program :running :compiled)
                             global-name
                             nil))

(defn set-global-value [global-name ^double value]
  (when-let [global-number (get-global-number global-name)]
    (send (p/set-global-value global-number value))))

(defn set-global-report [global-name report?]
  (when-let [global-number (get-global-number global-name)]
    (swap! state update-in [:reporting :globals]
           (if report? conj disj) global-name)
    (send (p/set-global-report global-number report?))))

(defn set-pin-value [pin-name ^double value]
  (when-let [pin-number (get-pin-number pin-name)]
    (send (p/set-pin-value pin-number value))))

(defn set-pin-report [pin-name report?]
  (when-let [pin-number (get-pin-number pin-name)]
    (swap! state update-in [:reporting :pins]
           (if report? conj disj) pin-name)
    (send (p/set-pin-report pin-number report?))))

(defn send-pins-reporting []
  (let [pins (-> @state :reporting :pins)]
    (doseq [pin-name pins]
      (set-pin-report pin-name true))))

; TODO(Richo): This function makes no sense here!
(defn compile [src type silent? & args]
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
      (swap! state assoc-in [:program :current] program)
      program)
    (catch Throwable ex
      (when-not silent?
        (logger/newline)
        (logger/error (.getMessage ex))
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


(defn- update-reporting [program]
  "All pins and globals referenced in the program must be enabled"
  (doseq [global (filter :name (-> program :compiled :globals))]
    (set-global-report (:name global) true))
  (doseq [{:keys [type number]} (filter ast/pin-literal? (-> program :final-ast ast/all-children))]
    (set-pin-report (str type number) true)))

(defn run [program]
  (swap! state assoc-in [:reporting :globals] #{})
  (swap! state assoc-in [:program :running] program)
  (let [bytecodes (en/encode (:compiled program))
        sent (send (p/run bytecodes))]
    (update-reporting program)
    sent))

(defn install [program]
  ; TODO(Richo): Should I store the installed program?
  (let [bytecodes (en/encode (:compiled program))
        sent (send (p/install bytecodes))]
    ; TODO(Richo): This message is actually a lie, we really don't know yet if the
    ; program was installed successfully. I think we need to add a confirmation
    ; message coming from the firmware
    (logger/success "Installed program successfully!")))

(defn start-reporting [] (send (p/start-reporting)))
(defn stop-reporting [] (send (p/stop-reporting)))

(defn start-profiling [] (send (p/start-profiling)))
(defn stop-profiling [] (send (p/stop-profiling)))

(defn set-report-interval [interval]
  (let [interval (int (constrain interval
                                 (config/get-in [:device :report-interval-min] 0)
                                 (config/get-in [:device :report-interval-max] 100)))]
    (when-not (= (-> @state :reporting :interval)
                 interval)
      (swap! state assoc-in [:reporting :interval] interval)
      (send (p/set-report-interval interval)))))

(defn set-all-breakpoints [] (send (p/set-all-breakpoints)))
(defn clear-all-breakpoints [] (send (p/clear-all-breakpoints)))
(defn send-continue []
  (swap! state assoc :debugger nil)
  (send (p/continue)))

(defn- start-keep-alive-loop [r]
  (go
   (loop [i 0]
     ; TODO(Richo): We shouldn't need to send a keep alive unless
     ; we haven't send anything in the last 100 ms.
     (when (connected?)
       (log/info "KEEP ALIVE (" r "): " i)
       (send (p/keep-alive))
       (<! (timeout 100))
       (recur (inc i))))
   (log/info "BYE KEEP ALIVE (" r ")")))

(defn- process-timestamp [^long timestamp]
  "Calculating the time since the last snapshot (both in the vm and the middleware)
  and then calculating the difference between these intervals and adding it as
  pseudo variable so that I can observe it in the inspector. Its value should always
  be close to 0. If not, we try increasing the report interval."
  (let [arduino-time timestamp
        middleware-time (millis)
        timing (-> @state :timing)
        ^long previous-arduino-time (or (get timing :arduino) arduino-time)
        ^long previous-middleware-time (or (get timing :middleware) middleware-time)
        delta-arduino (- arduino-time previous-arduino-time)
        delta-middleware (- middleware-time previous-middleware-time)
        delta (- delta-arduino delta-middleware)
        timing-diffs (:diffs timing)]
    (rb/push! timing-diffs delta)
    (swap! state update :timing
           #(assoc %
                   :arduino arduino-time
                   :middleware middleware-time))
    (add-pseudo-var! "__delta" delta)
    (let [^long report-interval (-> @state :reporting :interval)
          ^long report-interval-inc (config/get-in [:device :report-interval-inc] 5)
          delta-smooth (Math/abs (rb/avg timing-diffs))
          ^long delta-threshold-min (config/get-in [:device :delta-threshold-min] 1)
          ^long delta-threshold-max (config/get-in [:device :delta-threshold-max] 25)]
      (add-pseudo-var! "__delta_smooth" delta-smooth)
      (add-pseudo-var! "__report_interval" report-interval)
      ; If the delta-smooth goes below the min we decrement the report-interval
      (when (< delta-smooth delta-threshold-min)
        (set-report-interval (- report-interval report-interval-inc)))
      ; If the delta-smooth goes above the max we increment the report-interval
      (when (> delta-smooth delta-threshold-max)
        (set-report-interval (+ report-interval report-interval-inc))))))

(defn process-pin-value [{:keys [timestamp data]}]
  (let [pins (into {}
                   (map (fn [pin]
                          (when-let [name (get-pin-name (:number pin))]
                            [name (assoc pin :name name)]))
                        data))]
    (swap! state assoc
           :pins {:timestamp timestamp :data pins})))

(defn process-global-value [{:keys [timestamp data]}]
  (let [globals (vec (program/all-globals
                      (-> @state :program :running :compiled)))]
    (let [globals (into {}
                        (map (fn [{:keys [number] :as global}]
                               (when-let [name (:name (nth globals number {}))]
                                 [name (assoc global :name name)]))
                             data))]
      (swap! state assoc
             :globals
             {:timestamp timestamp :data globals}))))

(defn process-free-ram [{:keys [memory]}]
  (swap! state assoc :memory memory))

(defn process-running-scripts [{:keys [scripts]}]
  (let [program (-> @state :program :running)
        get-script-name (fn [i] (-> program :compiled :scripts (get i) :name))
        task? (fn [i] (-> program :final-ast :scripts (get i) ast/task?))
        [old new] (swap-vals! state assoc
                              :scripts
                              (into {} (map-indexed
                                        (fn [i script]
                                          (let [name (get-script-name i)]
                                            [name
                                             (assoc script
                                                    :index i
                                                    :name name
                                                    :task? (task? i))]))
                                        scripts)))]
    (doseq [script (filter :error? (sort-by :index (-> new :scripts vals)))]
      (when-not (= (-> old :scripts (get (:name script)) :error-code)
                   (:error-code script))
        (logger/warning "%1 detected on script \"%2\". The script has been stopped."
                        (:error-msg script)
                        (:name script))))))

(defn process-profile [{:keys [data]}]
  (swap! state assoc :profiler data)
  (add-pseudo-var! "__tps" (* 10 (:ticks data)))
  (add-pseudo-var! "__vm-report-interval" (:report-interval data)))

(defn process-coroutine-state [{:keys [data]}]
  (swap! state assoc :debugger data))

(defn process-error [{{:keys [code msg]} :error}]
  (logger/newline)
  (logger/warning "%1 detected. The program has been stopped" msg)
  (if (p/error-disconnect? code)
    (disconnect!)))

(defn process-trace [{:keys [msg]}]
  (log/info "TRACE:" msg))

(defn process-serial-tunnel [{:keys [data]}]
  (logger/log "SERIAL: %1" data))

(defn process-next-message [in]
  (go
   (when-let [{:keys [tag timestamp] :or {timestamp nil} :as cmd}
              (<! (p/process-next-message in))]
     (when timestamp
       (process-timestamp timestamp))
     (case tag
       :pin-value (process-pin-value cmd)
       :global-value (process-global-value cmd)
       :running-scripts (process-running-scripts cmd)
       :free-ram (process-free-ram cmd)
       :profile (process-profile cmd)
       :coroutine-state (process-coroutine-state cmd)
       :error (process-error cmd)
       :trace (process-trace cmd)
       :serial (process-serial-tunnel cmd)
       :unknown-cmd (logger/warning "Uzi - Invalid response code: %1"
                                    (:code cmd)))
     cmd)))

(defn- start-process-input-loop [{:keys [in]} r]
  (go
   (loop [i 0]
     (when (connected?)
       ;(log/info "ACAACA PROCESS INPUT (" r "):" i)
       (if-not (<! (process-next-message in))
         (do
           (logger/log "TIMEOUT!")
           (disconnect!))
         (recur (inc i)))))
   (log/info "BYE PROCESS INPUT (" r ")")))

(defn- clean-up-reports [r]
  (go
   (loop []
     (when (connected?)
       ; If we have pseudo vars, remove old ones (older than 1s)
       (if-not (zero? (count (-> @state :pseudo-vars :data)))
         (if (config/get-in [:device :pseudo-vars?] false)
           (swap! state update-in [:pseudo-vars :data]
                  #(let [^long timestamp (or (get-in @state [:pseudo-vars :timestamp]) 0)
                         limit (- timestamp 1000)]
                     (into {} (remove (fn [[_ {^long ts :ts}]] (< ts limit)) %))))
           (swap! state assoc-in [:pseudo-vars :data] {})))
       ; Now remove pins/globals that are not being reported anymore
       (let [reporting (:reporting @state)]
         (swap! state update-in [:pins :data] #(select-keys % (:pins reporting)))
         (swap! state update-in [:globals :data] #(select-keys % (:globals reporting))))
       ; Finally wait 1s and start over
       (<! (timeout 1000))
       (recur)))
   (log/info "BYE CLEAN UP REPORTS! (" r ")")))


(defn- request-connection [port-name baud-rate]
  (go
   (try
     (logger/newline)
     (logger/log "Opening port: %1" port-name)
     (if-let [connection (ports/connect! port-name baud-rate)]
       (do
         (<! (timeout 2000)) ; NOTE(Richo): Needed in Mac
         (logger/log "Requesting connection...")
         (let [handshake (p/perform-handshake connection)
               timeout (a/timeout 1000)]
           (if (a/alt! handshake ([success?]
                                  (if success?
                                    (logger/success "Connection accepted!")
                                    (logger/error "Connection rejected"))
                                  success?)
                       timeout (do
                                 (logger/error "Connection timeout")
                                 false)
                       :priority true)
             connection
             (do
               (ports/disconnect! connection)
               nil))))
       (do
         (logger/error "Opening port failed!")
         nil))
     (catch Exception ex
       (log/error "ERROR WHILE OPENING PORT ->" ex)))))

(defn connect!
  ([] (connect! (first (available-ports))))
  ([port-name & {:keys [board baud-rate]
                 :or {board UNO, baud-rate 9600}}]
   (go
    (if (connected?)
      (log/error "The board is already connected")
      (when-let [connection (<! (request-connection port-name baud-rate))]
        (port-scanner/stop!)
        (swap! state assoc
               :connection connection
               :board board
               :timing {:diffs (rb/make-ring-buffer
                                (config/get-in [:device :timing-diffs-size] 10))
                        :arduino nil
                        :middleware nil}
               :reporting {:pins #{}
                           :globals #{}})
        (set-report-interval (config/get-in [:device :report-interval-min] 0))
        (let [r (rand-int 1000)]
          (start-keep-alive-loop r)
          (start-process-input-loop connection r)
          (clean-up-reports r))
        (start-reporting)
        (send-pins-reporting))))))


(comment

 (do
   (a/<!! (connect! "tty.usbmodem14201"))
   (disconnect!)
   (a/<!! (connect! "tty.usbmodem14201")))



 )
