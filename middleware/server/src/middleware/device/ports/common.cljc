(ns middleware.device.ports.common)

(defprotocol UziPort
  (close! [this])
  (make-in-chan! [this])
  (make-out-chan! [this]))

(def ^:private constructors (atom []))

(defn register-port [& f]
  (swap! constructors
         (fn [constructors]
           (vec (distinct (into constructors f))))))

(defn connect! [name & args]
  (let [port (first (keep #(apply % name args) @constructors))
        in-chan (make-in-chan! port)
        out-chan (make-out-chan! port)]
    {:port port
     :port-name name
     :connected? (atom true)
     :out out-chan
     :in in-chan}))

(defn connected? [{:keys [connected?]}] @connected?)

(defn disconnect! [{:keys [actual-port connected?]}]
  (when (compare-and-set! connected? true false)
    ; TODO(Richo): Should I also close the channels?
    (close! actual-port)))
