(ns middleware.controller
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]
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
  (js/Promise.resolve (c/compile-uzi-string src)))

(defn run [program]
  (js/Promise.resolve (dc/run program)))

(init-dependencies)

(comment
 (print "Richo capo!")

(dc/connect! "COM4" :reporting? false)
(dc/disconnect!)
(dc/start-reporting)
(js/console.log @dc/state)
(cljs.pprint/pprint @dc/state)

 (def p (js/Promise. (fn [res] (res 42))))
(js/console.log p)
(.then p (fn [v] (println (inc v))))
 ,,)
