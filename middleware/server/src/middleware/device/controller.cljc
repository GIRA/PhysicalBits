(ns middleware.device.controller
  (:require #?(:clj [clojure.tools.logging :as log])
            [middleware.device.ports.scanner :as port-scanner]
            [clojure.core.async :as a :refer [<! >! go timeout]]
            [middleware.device.ports.common :as ports]
            [middleware.device.protocol :as p]
            [middleware.device.boards :refer [UNO get-pin-number get-pin-name]]
            [middleware.compilation.encoder :as en]
            [middleware.ast.utils :as ast]
            [middleware.program.utils :as program]
            [middleware.device.utils.ring-buffer :as rb]
            [middleware.utils.logger :as logger]
            #?(:clj [middleware.utils.config :as config])
            [middleware.utils.core :refer [millis clamp]]))

(defn- log-error [msg e]
  #?(:clj (log/error msg e)
     :cljs (println "ERROR:" msg e)))

(defn- get-config [path default-value]
  #?(:clj (config/get-in path default-value)
     ; TODO(Richo): Make the CLJS version work for real...
     :cljs default-value))

(def initial-state {:connection nil
                    :board UNO
                    :reporting {:pins #{}, :globals #{}}
                    :pins {}
                    :globals {}
                    :pseudo-vars {:timestamp nil, :data {}}
                    :scripts []
                    :profiler nil
                    :debugger {:vm nil
                               :breakpoints {:user #{}, :system #{}}}
                    :timing {:arduino nil, :middleware nil, :diffs nil}
                    :memory {:arduino nil, :uzi nil}
                    :program nil})
(def state (atom initial-state))

(def update-chan (a/chan 100))
(def updates (a/mult update-chan))

(defn- add-pseudo-var! [name value]
  (when (get-config [:device :pseudo-vars?] false)
    (let [now (or (-> @state :timing :arduino) 0)]
      (swap! state
             #(-> %
                  (assoc-in [:pseudo-vars :timestamp] now)
                  (assoc-in [:pseudo-vars :data name]
                            {:name name, :value value, :ts now})))
      (a/put! update-chan :pseudo-vars))))

(defn get-pin-value [pin-name]
  (-> @state :pins (get pin-name) :value))

(defn available-ports []
  @port-scanner/available-ports)

(defn start-port-scanning! []
  (add-watch port-scanner/available-ports ::scanner-update
             (fn [_ _ _ _] (a/put! update-chan :connection)))
  (port-scanner/start!))

(defn stop-port-scanning! []
  (port-scanner/stop!)
  (remove-watch port-scanner/available-ports ::scanner-update))

(defn connected? []
  (when-let [connection (-> @state :connection)]
    (and (not= :pending connection)
         (ports/connected? connection))))

(defn disconnect! []
  (go
    (let [[{{:keys [connected?] :as conn} :connection}]
          (swap-vals! state
                      (fn [{:keys [connection] :as state}]
                        (if (or (= :pending connection)
                                (nil? connection))
                          state
                          (assoc state :connection :pending))))]
      (when connected?
        ; HACK(Richo): Only show the connection lost message in the output console if
        ; the interactivity feature is enabled. Otherwise, every time we send a program
        ; to the robot we'll be seeing this confusing error message
        (when (get-config [:features :interactivity?] true)
          (logger/error "Connection lost!"))
        (try
          (ports/disconnect! conn)
          ; TODO(Richo): I used to think there was some sort of race condition in my
          ; code that prevented me from being able to quickly disconnect and reconnect.
          ; However, after further testing it seems to be some OS related issue. So
          ; just to be safe I'm adding the 1s delay here.
          (<! (timeout 1000))
          (catch #?(:clj Throwable :cljs :default) e
            (log-error "ERROR WHILE DISCONNECTING ->" e)))
        (start-port-scanning!)
        (reset! state initial-state)
        (a/onto-chan! update-chan [:connection :debugger] false)))))

(defn send! [bytes]
  (when-let [out (-> @state :connection :out)]
    (when-not (a/put! out bytes)
      (disconnect!)))
  bytes)

(defn- get-global-number [global-name]
  (program/index-of-variable (@state :program)
                             global-name
                             nil))

(defn set-global-value [global-name ^double value]
  (when-let [global-number (get-global-number global-name)]
    (send! (p/set-global-value global-number value))))

(defn set-global-report [global-name report?]
  (when-let [global-number (get-global-number global-name)]
    (swap! state update-in [:reporting :globals]
           (if report? conj disj) global-name)
    (send! (p/set-global-report global-number report?))))

(defn set-pin-value [pin-name ^double value]
  (when-let [pin-number (get-pin-number pin-name)]
    (send! (p/set-pin-value pin-number value))))

(defn set-pin-report [pin-name report?]
  (when-let [pin-number (get-pin-number pin-name)]
    (swap! state update-in [:reporting :pins]
           (if report? conj disj) pin-name)
    (send! (p/set-pin-report pin-number report?))))

(defn send-pins-reporting []
  (let [pins (-> @state :reporting :pins)]
    (doseq [pin-name pins]
      (set-pin-report pin-name true))))

(defn- update-reporting [program]
  ; NOTE(Richo): All pins and globals referenced in the program must be enabled
  (doseq [global (filter :name (-> program :globals))]
    (set-global-report (:name global) true))
  (doseq [{:keys [type number]} (filter ast/pin-literal?
                                        (-> (meta program)
                                            :final-ast
                                            ast/all-children))]
    (set-pin-report (str type number) true)))


(defn reset-debugger! []
  (let [[old new] (swap-vals! state assoc :debugger (-> initial-state :debugger))]
    (when (not= old new)
      (a/put! update-chan :debugger))))

(defn run [program]
  (swap! state #(-> %
                    (assoc-in [:reporting :globals] #{})
                    (assoc :program program)))
  (reset-debugger!)
  (let [bytecodes (en/encode program)
        sent (send! (p/run bytecodes))]
    (update-reporting program)
    sent))

(defn install [program]
  ; TODO(Richo): Should I store the installed program?
  (let [bytecodes (en/encode program)
        sent (send! (p/install bytecodes))]
    ; TODO(Richo): This message is actually a lie, we really don't know yet if the
    ; program was installed successfully. I think we need to add a confirmation
    ; message coming from the firmware
    (logger/success "Installed program successfully!")
    sent))

(defn start-reporting [] (send! (p/start-reporting)))
(defn stop-reporting [] (send! (p/stop-reporting)))

(defn start-profiling [] (send! (p/start-profiling)))
(defn stop-profiling [] (send! (p/stop-profiling)))

(defn set-report-interval [interval]
  (let [interval (int (clamp interval
                             (get-config [:device :report-interval-min] 0)
                             (get-config [:device :report-interval-max] 100)))]
    (when-not (= (-> @state :reporting :interval)
                 interval)
      (swap! state assoc-in [:reporting :interval] interval)
      (send! (p/set-report-interval interval)))))

(defn- keep-alive-loop []
  (go
    (loop []
      ; TODO(Richo): We shouldn't need to send a keep alive unless
      ; we haven't send anything in the last 100 ms.
      (when (connected?)
        (send! (p/keep-alive))
        (<! (timeout 100))
        (recur)))))

(defn- process-timestamp [^long timestamp]
  ; NOTE(Richo): Calculating the time since the last snapshot (both in the vm and the 
  ; middleware) and then calculating the difference between these intervals and adding 
  ; it as pseudo variable so that I can observe it in the inspector. Its value should 
  ; always be close to 0. If not, we try increasing the report interval.
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
          ^long report-interval-inc (get-config [:device :report-interval-inc] 5)
          delta-smooth (Math/abs (rb/avg timing-diffs))
          ^long delta-threshold-min (get-config [:device :delta-threshold-min] 1)
          ^long delta-threshold-max (get-config [:device :delta-threshold-max] 25)]
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
  (let [all-globals (-> @state :program program/all-globals vec)
        globals (into {}
                      (map (fn [{:keys [number] :as global}]
                             (when-let [name (:name (nth all-globals number {}))]
                               [name (assoc global :name name)]))
                           data))]
    (swap! state assoc
           :globals
           {:timestamp timestamp :data globals})))

(defn process-free-ram [{:keys [memory]}]
  (swap! state assoc :memory memory))

(defn process-running-scripts [{:keys [scripts]}]
  (let [program (@state :program)
        get-script-name (fn [i] (-> program :scripts (get i) :name))
        task? (fn [i] (-> (meta program) :final-ast :scripts (get i) ast/task?))
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

(defn process-debugger [{:keys [data]}]
  (swap! state assoc-in [:debugger :vm] data))

(defn process-error [{{:keys [code msg]} :error}]
  (go
    (logger/newline)
    (logger/warning "%1 detected. The program has been stopped" msg)
    (when (p/error-disconnect? code)
      (<! (disconnect!)))))

(defn process-trace [{:keys [msg]}]
  (logger/log "TRACE:" msg))

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
        :debugger (process-debugger cmd)
        :trace (process-trace cmd)
        :serial (process-serial-tunnel cmd)
        :error (<! (process-error cmd)) ; Special case because it calls disconnect!
        :unknown-cmd (logger/warning "Uzi - Invalid response code: %1"
                                     (:code cmd)))
      (when tag (>! update-chan tag))
      cmd)))

(defn- process-input-loop [{:keys [in]}]
  (go
    (loop []
      (when (connected?)
        (if-not (<! (process-next-message in))
          (<! (disconnect!))
          (recur))))))

(defn- clean-up-reports-loop []
  (go
    (loop []
      (when (connected?)
        ; If we have pseudo vars, remove old ones (older than 1s)
        (when-not (zero? (count (-> @state :pseudo-vars :data)))
          (if (get-config [:device :pseudo-vars?] false)
            (swap! state update-in [:pseudo-vars :data]
                   #(let [^long timestamp (or (get-in @state [:pseudo-vars :timestamp]) 0)
                          limit (- timestamp 1000)]
                      (into {} (remove (fn [[_ {^long ts :ts}]] (< ts limit)) %))))
            (swap! state assoc-in [:pseudo-vars :data] {})))
        ; Now remove pins/globals that are not being reported anymore
        (let [reporting (:reporting @state)]
          (swap! state update-in [:pins :data] #(select-keys % (:pins reporting)))
          (swap! state update-in [:globals :data] #(select-keys % (:globals reporting))))
        ; Trigger the update event
        (a/onto-chan! update-chan [:pins :globals :pseudo-vars] false)
        ; Finally wait 1s and start over
        (<! (timeout 1000))
        (recur)))))

(defn- request-connection [port-name baud-rate]
  (go
    (try
      (logger/clear)
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
      (catch #?(:clj Throwable :cljs :default) ex
        (log-error "ERROR WHILE OPENING PORT ->" ex)))))

(defn- connection-successful [connection board reporting?]
  (stop-port-scanning!)
  (swap! state assoc
         :connection connection
         :board board
         :timing {:diffs (rb/make-ring-buffer
                          (get-config [:device :timing-diffs-size] 10))
                  :arduino nil
                  :middleware nil}
         :reporting {:pins #{}
                     :globals #{}})
  (set-report-interval (get-config [:device :report-interval-min] 0))
  (process-input-loop connection)
  (clean-up-reports-loop)
  (keep-alive-loop)
  (when reporting?
    (start-reporting)
    (send-pins-reporting)))

(defn connect!
  ([] (connect! (first (available-ports))))
  ([port-name & {:keys [board baud-rate reporting?]
                 :or {board UNO, baud-rate 9600, reporting? true}}]
   (go
     (let [[{old-connection :connection}]
           (swap-vals! state
                       (fn [{:keys [connection] :as state}]
                         (if (or (= :pending connection)
                                 (ports/connected? connection))
                           state
                           (assoc state :connection :pending))))]
       (when (nil? old-connection)
         (if-let [connection (<! (request-connection port-name baud-rate))]
           (connection-successful connection board reporting?)
           (swap! state assoc :connection nil))
         (>! update-chan :connection))))))
