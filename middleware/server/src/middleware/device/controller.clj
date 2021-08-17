(ns middleware.device.controller
  (:refer-clojure :exclude [send compile])
  (:require [clojure.tools.logging :as log]
            [serial.core :as s]
            [serial.util :as su]
            [clojure.string :as str]
            [clojure.core.async :as a :refer [<! <!! >! >!! go go-loop timeout]]
            [middleware.utils.async :refer :all]
            [middleware.device.protocol :as p]
            [middleware.device.boards :refer :all]
            [middleware.utils.conversions :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.compiler.utils.ast :as ast]
            [middleware.compiler.utils.program :as program]
            [middleware.output.logger :as logger]
            [middleware.config :as config]
            [middleware.device.utils.ring-buffer :as rb])
  (:import (java.net Socket)))

(comment

 (start-profiling)
 (stop-profiling)

 (-> @state :profiler :ticks (* 10))

 (-> @state :globals)
 (-> @state :pins)
 (-> @state :reporting)
 (rb/avg (-> @state :timing :diffs))
 (millis)
 (< (get-in @state [:pseudo-vars :data "juan" :ts])
    (- (-> @state :pseudo-vars :timestamp) 1000))

 (millis)
 (add-pseudo-var! "richo" 42)

 (-> @state :timing)
 (swap! state update :timing
        #(assoc %
                :arduino 1
                :middleware 2))
 (or (get (get @state :timing) :arduino2) 1)

 (set-report-interval 5)
 ,)

(defn millis ^long [] (System/currentTimeMillis))
(defn constrain [^long val ^long lower ^long upper] (max lower (min upper val)))

(defprotocol UziPort
  (close! [this])
  (write! [this data])
  (listen! [this listener-fn]))

(extend-type serial.core.Port
  UziPort
  (close! [port]
          (s/close! port)
          ; TODO(Richo): There seems to be some race condition if I disconnect/reconnect
          ; quickly. I suspect the problem is that I need to wait until all threads are
          ; finished or maybe I should close the channels and properly clean up the
          ; resources. However, for now a 1s delay seems to work...
          (<!! (timeout 1000)))
  (write! [port data] (s/write port data))
  (listen! [port listener-fn]
           (s/listen! port
                      (fn [^java.io.InputStream input-stream]
                        (listener-fn (.read input-stream))))))

(extend-type java.net.Socket
  UziPort
  (close! [socket]
          (.close socket)
          ; TODO(Richo): There seems to be some race condition if I disconnect/reconnect
          ; quickly. I suspect the problem is that I need to wait until all threads are
          ; finished or maybe I should close the channels and properly clean up the
          ; resources. However, for now a 1s delay seems to work...
          (<!! (timeout 1000)))
  (write! [socket data]
          (let [out (.getOutputStream socket)
                bytes (if (number? data) [data] data)]
            (.write out (byte-array bytes))))
  (listen! [socket listener-fn]
           (let [buffer-size 1000
                 buffer (byte-array buffer-size)
                 in (.getInputStream socket)]
             (go-loop []
               (when-not (.isClosed socket)
                 (let [bytes-read (.read in buffer 0 buffer-size)]
                   (dotimes [i bytes-read]
                            (listener-fn (bit-and (int (nth buffer i)) 16rFF))))
                 (recur))))))

(def initial-state {:port-name nil
                    :port nil
                    :connected? false
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
                    :program {:current nil, :running nil}
                    :available-ports []})
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
  (vec (sort (su/get-port-names))))

(defn connected? []
  (not (nil? (:port @state))))

(defn disconnect []
  (when-let [port (@state :port)]
    (swap! state
           #(-> initial-state
                ; Keep the current program
                (assoc-in [:program :current]
                           (-> % :program :current))))
    (try
      (close! port)
      (catch Throwable e
        (log/error "ERROR WHILE DISCONNECTING ->" e)))
    (logger/error "Connection lost!")))

(defn send [bytes]
  (when-let [port (@state :port)]
    (try
      (write! port bytes)
      (catch Throwable e
        (log/error "ERROR WHILE SENDING ->" e)
        (disconnect))))
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

; TODO(Richo): Extract incoming handshake messages?
(defn- request-connection [port in]
  (go
   (<! (timeout 2000)) ; NOTE(Richo): Needed in Mac
   (write! port (p/request-connection))
   (logger/log "Requesting connection...")
   ;(<! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
   (if-let [n1 (<? in 1000)]
     (let [n2 (p/confirm-handshake n1)]
       (write! port n2)
       ;(<! (timeout 500)) ; TODO(Richo): Not needed in Mac/Windows
       (if (= n2 (<? in 1000))
         (do
           (logger/success "Connection accepted!")
           true)
         (do
           (logger/error "Connection rejected")
           false)))
     (do
       (logger/error "Connection timeout")
       false))))

(defn- keep-alive-loop []
  (loop []
    (when (connected?)
      (send (p/keep-alive))
      (<!! (timeout 100))
      (recur))))

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
    (disconnect)))

(defn process-trace [{:keys [msg]}]
  (log/info "TRACE:" msg))

(defn process-serial-tunnel [{:keys [data]}]
  (logger/log "SERIAL: %1" data))

(defn process-next-message [in]
  (when-let [{:keys [tag timestamp] :or {timestamp nil} :as cmd}
             (p/process-next-message in)]
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
                                   (:code cmd)))))

(defn- process-input [in]
  (loop []
    (when (connected?)
      (try
        (process-next-message in)
        (catch Throwable e
          (log/error "ERROR WHILE PROCESSING INPUT ->" e)))
      (recur))))

(defn- clean-up-reports []
  (go-loop []
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
      (recur))))

(defn- extract-socket-data [port-name]
  (try
    (when-let [match (re-matches #"((\d+)\.(\d+)\.(\d+)\.(\d+)|localhost)\:(\d+)"
                                 (str/trim port-name))]
      (let [address (nth match 1)
            port (Integer/parseInt (nth match 6))]
        (assert (< 0 port 0x10000))
        (when-not (= address "localhost")
          (assert (every? #(<= 0 % 255)
                          (map #(Integer/parseInt %)
                               (->> match (drop 2) (take 4))))))
        [address port]))
    (catch Throwable ex
      false)))

(defn- open-port [port-name baud-rate]
  (try
    (logger/newline)
    (if-let [[^String address ^int port] (extract-socket-data port-name)]
      (do
        (logger/log "Connecting on socket...")
        (logger/log "Opening port: %1" port-name)
        (Socket. address port))
      (do
        (logger/log "Connecting on serial...")
        (logger/log "Opening port: %1" port-name)
        (s/open port-name :baud-rate baud-rate)))
    (catch Exception e
      (logger/error "Opening port failed!")
      (log/error "ERROR WHILE OPENING PORT ->" e) ; TODO(Richo): Exceptions should be logged but not sent to the client
      nil)))

(defn connect
  ([] (connect (first (available-ports))))
  ([port-name & {:keys [board baud-rate]
                 :or {board UNO, baud-rate 9600}}]
   (if (connected?)
     (log/error "The board is already connected")
     (when-let [port (open-port port-name baud-rate)]
       (let [in (a/chan 1000)]
         (listen! port #(>!! in %))
         (if-not (<?? (request-connection port in) 15000)
           (close! port)
           (do ; Connection successful
             (swap! state assoc
                    :port port
                    :port-name port-name
                    :connected? true
                    :board board
                    :timing {:diffs (rb/make-ring-buffer
                                     (config/get-in [:device :timing-diffs-size] 10))
                             :arduino nil
                             :middleware nil}
                    :reporting {:pins #{}
                                :globals #{}})
             (set-report-interval (config/get-in [:device :report-interval-min] 0))
             (a/thread (keep-alive-loop))
             (a/thread (process-input in))
             (start-reporting)
             (send-pins-reporting)
             (clean-up-reports))))))))

(defonce ^:private port-scanning? (atom false))

(defn stop-port-scan []
  (reset! port-scanning? false))

(defn start-port-scan []
  (when (compare-and-set! port-scanning? false true)
    (go-loop []
      (when @port-scanning?
        (when-not (connected?)
          (swap! state assoc :available-ports (available-ports)))
        (<! (timeout 1000))
        (recur)))))

(start-port-scan)
