(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.utils.program :as program]
            [middleware.compiler.encoder :as en]
            [middleware.output.logger :as logger]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.utils.async :refer-macros [go-try <?]]))

(defn init-dependencies []
  (fs/register-fs! #'browser/file)
  (ports/register-constructors! #'simulator/open-port))

(defn init []
  (init-dependencies)
  (println "Controller started successfully!")
  (.then (.-ready js/Simulator)
         (fn []
           (println "READY TO CONNECT!")
           (js/Simulator.start 16))))

(defn chan->promise [ch]
  (js/Promise. (fn [res rej]
                 (a/take! ch #(if (instance? js/Error %)
                                (rej %)
                                (res %))))))

;;;;;;;;;;;;;;;; BEGIN server stuff
; TODO(Richo): Most of this code was copied from server/http.clj

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

(defn- get-server-state []
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

(def update* (atom nil))

; TODO(Richo): The update-loop could be started/stopped automatically
(def ^:private update-loop? (atom false))

; TODO(Richo): This loop sucks, it would be better if we could subscribe
; to the update events coming from the device.
(defn start-update-loop! []
  (when (compare-and-set! update-loop? false true)
    (go-loop [old-state nil]
      (when @update-loop?
        (let [new-state (get-server-state)
              diff (get-state-diff old-state new-state)]
          (when-not (empty? diff)
            (when-let [update-fn @update*]
              (update-fn (clj->js diff))))
          (<! (a/timeout 16))
          (recur new-state))))))

(defn stop-update-loop! []
  (reset! update-loop? false))

(comment
 (stop-update-loop!)
 (start-update-loop!)
 )

;;;;;;;;;;;;;;;; END server stuff

(defn ^:export on-update [update-fn]
  (reset! update* update-fn)
  (start-update-loop!))

(defn ^:export connect [update-fn]
  (chan->promise
   (go-try
    (<? (dc/connect! "simulator"))
    (let [port-name (@dc/state :port-name)]
      {:port-name port-name}))))


(defn ^:export disconnect []
  (chan->promise
   (go-try
    (<? (dc/disconnect!))
    "OK")))

(defn ^:export compile [src type silent?]
  (chan->promise
   (go-try
    (clj->js (dc/compile! src type silent?)))))

(defn ^:export run [src type silent?]
  (chan->promise
   (go-try
    (let [program (dc/compile! src type silent?)]
      (dc/run program)
      (clj->js program)))))

(defn ^:export install [src type]
  (chan->promise
   (go-try
     (let [program (dc/compile! src type true)]
       (dc/install program)
       (clj->js program)))))

(comment

  (def listener (fn [a] (println a)))
  (def listener2 listener)

  (= listener listener2)

 (doto (chan->promise
        (go-try (do
                  (println "1")
                  (<! (timeout 1000))
                  #_(throw "RICHO!")
                  (throw (ex-info "Richo capo" {})))))
       (.then (fn [result] (println "SUCCESS!" result)))
       (.catch (fn [reason] (println "ERROR!" reason))))


 (go (<! (dc/connect! "sim"))
     (println "CONNECTED?" (dc/connected?)))

 (def ex
   (try
     (throw (ex-info "RICHO" {}))
     ;(throw "Richo")
     (catch js/Error ex ex)
     (catch :default ex (ex-info (str ex) {:error ex}))))
 (def ex *1)
 ex
 (instance? js/Error ex)

 (def err (js/Error. "RICHO!"))
 (def err (ex-info "RICHO" {:a 1}))
 (ex-message err)


 (def p (ports/connect! "sim"))
 (ports/disconnect! p)
 (a/put! (:out p) [255 0 8])
 (a/take! (:in p) (fn [d]
                    (println "RECEIVED:" d)))


 (dc/run (cc/compile-uzi-string "task blink13() running 5/s { write(D13, 0.5); }"))

(set! (.-onDataAvailable js/Serial))
(.-onDataAvailable js/Serial)
(js/Serial.onDataAvailable)
(set! js/Serial.onDataAvailable println)
 (js/Simulator.start)
 (connect! "sim")
 (disconnect!)
 (go (set! (.-innerText js/document.body) "RICHO!!!"))
 (js/GPIO.getPinValue 13)
 (def interval (js/setInterval #(set!
                                  (.-innerText js/document.body)
                                  (str (js/GPIO.getPinValue 13)))
                               10))
 (js/clearInterval interval)

  (time (let [stack #js []]
          (loop [count 0]
            (when (< count 15000000)
              (.push stack 69)
              (.pop stack)
              (recur (inc count))))))

  (time (let [stack (volatile! (list))]
          (loop [count 0]
            (when (< count 15000000)
              (vswap! stack conj 69)
              (vswap! stack pop)
              (recur (inc count))))))

  (time (let [stack (atom (list))]
          (loop [count 0]
            (when (< count 15000000)
              (swap! stack conj 69)
              (swap! stack pop)
              (recur (inc count))))))

 (def stack #js [])
 (.push stack 2)
 (.pop stack)
 stack
 (pop (conj (list) 1))

 ,,)
