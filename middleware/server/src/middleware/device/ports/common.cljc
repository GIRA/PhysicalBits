(ns middleware.device.ports.common
  (:require [clojure.core.async :as a]))

(defprotocol UziPort
  (close! [this])
  (make-in-chan! [this])
  (make-out-chan! [this]))

(def ^:private constructors (atom []))

(defn register-constructors! [& f]
  (reset! constructors (vec (distinct f))))

(defn connect! [name & args]
  (when-let [port (loop [[f & n] @constructors]
                    (when f
                      (if-let [port (apply f name args)]
                        port
                        (recur n))))]
    {:port port
     :port-name name
     :connected? (atom true)
     :out (make-out-chan! port)
     :in (make-in-chan! port)}))

(defn connected? [{:keys [connected?]}] @connected?)

(defn disconnect! [{:keys [port in out connected?]}]
  (when (compare-and-set! connected? true false)
    (a/close! in)
    (a/close! out)
    (close! port)
    true))
