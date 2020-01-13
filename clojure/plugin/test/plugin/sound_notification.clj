(ns plugin.sound-notification
  (:require [clojure.test :refer :all]
            ;[clojure.tools.logging :as log]
            [clj-audio.core :refer :all]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go]]))

(defn- play-sound [sound-name]
  (if-let [resource (io/resource sound-name)]
    (go (play (->stream resource)))
    #_(log/error "The resource named" sound-name "was not found.")))

(defmethod report :summary [m]
  (play-sound (if (and (zero? (:fail m))
                       (zero? (:error m)))
                "success.wav"
                "error.wav"))
  (with-test-out
    (println "\nRan" (:test m) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (:fail m) "failures," (:error m) "errors.")))
