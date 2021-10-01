(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.core :as core]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.utils.async :refer-macros [go-try <?]]))

(comment
 (println "Richo")

 (def c (a/chan))
 (def m (a/mult c))

 (go-loop [i 0]
   (when (< i 100)
     ;(println "Putting" i)
     (>! c i)
     (recur (inc i))))

 (doseq [c-name ["C1" "C2" "C3"]]
   (let [c* (a/chan)]
     (a/tap m c*)
     (go-loop []
       (if-let [data (<! c*)]
         (do
           (println c-name "->" data)
           (recur))
         (println "Bye from" c-name)))))

 (a/put! c "RICHO CAPO")

 (a/close! c)
 ,)


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
  (core/start-update-loop! (comp update-fn clj->js)))

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
