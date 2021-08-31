(ns middleware.controller-core
  (:require ["readline" :as readline]
            [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.parser.parser :as p]
            [middleware.compiler.compiler :as c]
            [middleware.compiler.encoder :as en]))

(def rl (.createInterface readline
                          #js {:input js/process.stdin
                               :output js/process.stdout}))

(enable-console-print!)

(defn question [q]
  (let [c (a/promise-chan)]
    (.question rl q (fn [d] (a/put! c d)))
    c))

(defn main [& [port-name]]
  (ports/register-constructors! #'serial/open-port)
  (println "Richo capo!")
  (go
   (<! (timeout 500))
   (let [port-name (or port-name
                       (<! (question "Ingrese el puerto: ")))
         src (<! (question "uzi> "))
         program (c/compile-uzi-string src)]
     (<! (dc/connect! port-name))
     (dc/run program)
     (dc/set-pin-report "A0" true)
     (dc/set-pin-report "A1" true)
     (dc/set-pin-report "A2" true)
     (dc/set-pin-report "A3" true)
     (dc/set-pin-report "A4" true)
     (dc/set-pin-report "D2" true)
     (dc/set-pin-report "D3" true)
     (dc/set-pin-report "D4" true)
     (dc/set-pin-report "D5" true)
     (dc/set-pin-report "D6" true)
     (dc/set-pin-report "D7" true)
     (dc/set-pin-report "D8" true)
     (dc/set-pin-report "D9" true)
     (dc/set-pin-report "D10" true)
     (dc/set-pin-report "D11" true)
     (dc/set-pin-report "D12" true)
     (dc/set-pin-report "D13" true)
     (<! (timeout 200))
     (loop []
       (when-let [{:keys [name value]} (get-in @dc/state [:pins :data "A0"])]
         (println name "->" value))
       (<! (timeout 50))
       (recur)))
   #_(loop []
     (println)
     (try
       (let [src (<! (question "uzi> "))
             program (c/compile-uzi-string src)]
         (dc/run program))
       (catch :default ex
         (println "ERROR" ex)))
     (recur))))

(comment

(dc/set-pin-report "D13" true)
(get-in @dc/state [:pins :data "D13"])
(dc/run (c/compile-uzi-string "task blink13() running 1/s { toggle(D13); }"))

(defn write [data]
  (let [ch (a/promise-chan)]
    (.write js/process.stdout data #(a/close! ch))
    ch))

(go-loop [i 0]
  (when (< i 10)
    (.clear js/console)
    (cljs.pprint/pprint {:a 1 :b 2})
    ;(<! (write "\033c"))
    ;(<! (write (str i)))
    (<! (timeout 500))
    (recur (inc i))))

(def counter (atom 0))

(go (let [[value] (swap-vals! counter inc)]
      (<! (write "\033c"))
      (<! (write (str value)))))

(go (let [[value] (swap-vals! counter inc)]
      (write "\033c")
      (write (str value))))


 ,,)
