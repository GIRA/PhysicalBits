(ns middleware.device.ports.common)

(defprotocol UziPort
  (close! [this])
  (write! [this data])
  (make-in-chan! [this])
  (make-out-chan! [this]))

(def constructors (atom []))

(defn register-port [& f]
  (swap! constructors
         (fn [constructors]
           (vec (distinct (into constructors f))))))


(defn open-port [name & args]
  (first (keep #(apply % name args)
               @constructors)))

(defn disconnect! [{:keys [actual-port]}]
  (close! actual-port))

(comment
 (register-port (fn [s _] (when (= "socket" s) 1))
                (fn [s _] (when (= "serial" s) 2)))

 (reset! constructors [])
 @constructors

 (open-port "serial" 9)
 (open-port "socket" 9)

 ,,)
