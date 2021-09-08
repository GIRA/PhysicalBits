(ns middleware.controller
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.compiler.compiler :as cc]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.node :as node]))

(defn init-dependencies []
  (fs/register-fs! #'node/file)
  (ports/register-constructors! #'serial/open-port))

(defn chan->promise [ch]
  (js/Promise. (fn [res] (a/take! ch res))))

(defn connect! [port-name]
  (chan->promise (dc/connect! port-name
                             :reporting? false)))

(defn disconnect! []
  (chan->promise (dc/disconnect!)))

(defn compile [src]
  (js/Promise.resolve (clj->js (cc/compile-uzi-string src))))

(defn run [program]
  (js/Promise.resolve (dc/run (js->clj program :keywordize-keys true))))

(init-dependencies)
