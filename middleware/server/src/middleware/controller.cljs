(ns middleware.controller
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.utils.async :refer [go-try <? chan->promise]]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.node :as node]
            [middleware.core :as core]))

(defn init-dependencies []
  (fs/register-fs! #'node/file)
  (ports/register-constructors! #'serial/open-port))

(defn on-update [update-fn]
  (core/start-update-loop! (comp update-fn clj->js)))

(defn connect! [port-name]
  (chan->promise
   (go-try
    (<? (core/connect! port-name :reporting? false))
    (some? (core/connected?)))))

(defn disconnect! []
  (chan->promise
   (go-try
    (<? (core/disconnect!)))))

(defn compile [src]
  (chan->promise
   (go-try
    (clj->js (<? (core/compile! src "uzi" true))))))

(defn run [program]
  (chan->promise
   (go-try
    (<? (core/run! (js->clj program :keywordize-keys true))))))

(init-dependencies)
