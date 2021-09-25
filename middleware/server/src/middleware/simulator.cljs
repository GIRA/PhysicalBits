(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            ;[middleware.device.ports.serial :as serial]
            [middleware.compiler.compiler :as cc]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]))

(defn init-dependencies []
  (fs/register-fs! #'browser/file)
  #_(ports/register-constructors! #'serial/open-port))

(defn init []
  (init-dependencies)
  (println "Controller started successfully!")
  (.then (.-ready js/Simulator)
         #(println "READY TO CONNECT!")))

(defn chan->promise [ch]
  (js/Promise. (fn [res] (a/take! ch res))))

(comment
 (js/test_blink)
 (.then (.-ready js/Simulator)
        #(println "READY!"))
 ,,)
