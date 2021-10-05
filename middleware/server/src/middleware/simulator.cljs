(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.core :as core]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.utils.async :refer [go-try <? chan->promise]]))

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

(defn ^:export on-update [update-fn]
  (core/start-update-loop! (comp update-fn clj->js)))

(defn ^:export connect [update-fn]
  (chan->promise
   (go-try
    (<? (core/connect! "simulator")))))


(defn ^:export disconnect []
  (chan->promise
   (go-try
    (<? (core/disconnect!)))))

(defn ^:export compile [src type silent?]
  (chan->promise
   (go-try
    (clj->js (<? (core/compile! src type silent?))))))

(defn ^:export run [src type silent?]
  (chan->promise
   (go-try
    (clj->js (<? (core/compile-and-run! src type silent?))))))

(defn ^:export install [src type]
  (chan->promise
   (go-try
     (clj->js (<? (core/compile-and-install! src type))))))

(defn ^:export set-pin-report [pins report]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert pins and report are the same size
    (<? (core/set-pin-report! (map vector pins report))))))

(defn ^:export set-global-report [globals report]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert globals and report are the same size
    (<? (core/set-global-report! (map vector globals report))))))

(defn ^:export set-profile [enabled?]
  (chan->promise
   (go-try
    (<? (core/set-profile! enabled?)))))
