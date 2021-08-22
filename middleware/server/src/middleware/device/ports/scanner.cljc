(ns middleware.device.ports.scanner
  (:require [clojure.core.async :refer [chan <! >! go-loop timeout alts! close!]]
            [serial.util :as su]))

(defn- get-available-ports []
  (vec (sort (su/get-port-names))))

(defonce ^:private cancel-token (atom nil))

(def available-ports (atom []))

(defn stop! []
  (when-let [token @cancel-token]
    (close! token)))

(defn start! []
  (let [[old _] (reset-vals! cancel-token
                             (let [token (chan)]
                               (go-loop []
                                 (reset! available-ports (get-available-ports))
                                 (let [[_ ch] (alts! [token (timeout 1000)]
                                                       :priority true)]
                                   (when-not (= ch token)
                                     (recur))))
                               token))]
    (when old (close! old))))
