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
  (when-let [port (first (keep #(apply % name args) @constructors))]
    {:port port
     :port-name name
     :connected? (atom true)
     :out (make-out-chan! port)
     :in (make-in-chan! port)}))

(defn connected? [{:keys [connected?]}] @connected?)

(defn disconnect! [{:keys [port connected?]}]
  (when (compare-and-set! connected? true false)
    ; TODO(Richo): Should I also close the channels?
    (close! port)))


(comment
 (def p (connect! "cu.usbmodem14201" 9600))
 usbmodem14101

 ,,)
