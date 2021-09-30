(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.core :as core]
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

(defn ^:export on-update [update-fn]
  (reset! core/update* (comp update-fn clj->js))
  (core/start-update-loop!))

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

(defn ^:export set-pin-report [pins report]
  (chan->promise
   (go-try
    (doseq [pin-name pins
            report? report]
      (dc/set-pin-report pin-name report?))
    "OK")))

(defn ^:export set-global-report [globals report]
  (chan->promise
   (go-try
    (doseq [global-name globals
            report? report]
      (dc/set-global-report global-name report?))
    "OK")))

(defn ^:export set-profile [enabled?]
  (chan->promise
   (go-try
    (if enabled?
      (dc/start-profiling)
      (dc/stop-profiling))
    "OK")))

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
