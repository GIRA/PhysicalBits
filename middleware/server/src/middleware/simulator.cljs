(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.compiler.compiler :as cc]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]))

(defn init-dependencies []
  (fs/register-fs! #'browser/file)
  (ports/register-constructors! #'simulator/open-port))

(defn init []
  (init-dependencies)
  (println "Controller started successfully!")
  (.then (.-ready js/Simulator)
         (fn []
           (println "READY TO CONNECT!")
           (js/Simulator.start))))

(defn chan->promise [ch]
  (js/Promise. (fn [res] (a/take! ch res))))

(defn connect! [port-name]
  (chan->promise
   (go
    (<! (dc/connect! port-name :reporting? false))
    (some? (dc/connected?)))))

(defn disconnect! []
  (chan->promise (dc/disconnect!)))

(defn compile [src]
  (js/Promise.resolve (clj->js (cc/compile-uzi-string src))))

(defn run [program]
  (js/Promise.resolve (dc/run (js->clj program :keywordize-keys true))))

(comment
  (go (<! (dc/connect! "sim"))
      (println "CONNECTED?" (dc/connected?)))

 (def p (ports/connect! "sim"))
 (ports/disconnect! p)
 (a/put! (:out p) [255 0 8])
 (a/take! (:in p) (fn [d]
                    (println "RECEIVED:" d)))


 (dc/run (cc/compile-uzi-string "task blink13() running 5/s { toggle(D13); }"))

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
 ,,)
