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

(defn ^:export set-pin-values [pins values]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert pins and values are the same size
    (<? (core/set-pin-values! (map vector pins values))))))

(defn ^:export set-global-values [globals values]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert globals and values are the same size
    (<? (core/set-global-values! (map vector globals values))))))

(defn ^:export set-profile [enabled?]
  (chan->promise
   (go-try
    (<? (core/set-profile! enabled?)))))

(defn ^:export debugger-set-breakpoints [breakpoints]
  (chan->promise
   (go-try
    (<? (core/set-breakpoints! breakpoints)))))

(defn ^:export debugger-break []
  (chan->promise
   (go-try
    (<? (core/debugger-break!)))))

(defn ^:export debugger-continue []
  (chan->promise
   (go-try
    (<? (core/debugger-continue!)))))

(defn ^:export debugger-step-over []
  (chan->promise
   (go-try
    (<? (core/debugger-step-over!)))))

(defn ^:export debugger-step-into []
  (chan->promise
   (go-try
    (<? (core/debugger-step-into!)))))

(defn ^:export debugger-step-out []
  (chan->promise
   (go-try
    (<? (core/debugger-step-out!)))))

(defn ^:export debugger-step-next []
  (chan->promise
   (go-try
    (<? (core/debugger-step-next!)))))
