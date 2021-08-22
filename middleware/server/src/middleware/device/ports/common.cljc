(ns middleware.device.ports.common)

(defprotocol UziPort
  (close! [this])
  (write! [this data])
  (listen! [this listener-fn]))

(def constructors (atom []))

(defn register-port [& f]
  (swap! constructors
         (fn [constructors]
           (vec (distinct (into constructors f))))))

(defn open-port [name & args]
  (first (keep #(apply % name args)
               @constructors)))

(comment
 (register-port (fn [s _] (when (= "socket" s) 1))
                (fn [s _] (when (= "serial" s) 2)))

 (reset! constructors [])
 @constructors

 (open-port "serial" 9)
 (open-port "socket" 9)

 ,,)
