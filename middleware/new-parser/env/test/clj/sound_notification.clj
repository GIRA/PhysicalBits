(ns sound-notification
  (:require [clojure.test :refer :all]
            [clj-audio.core :refer :all]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go]]))

(defn- play-sound! [sound-name]
  (if-let [resource (io/resource sound-name)]
    (go (play (->stream resource)))))

(defn play! [success?]
  (play-sound! (if success? "success.wav" "error.wav")))

(defmethod report :summary [m]
  (play! (and (zero? (:fail m))
              (zero? (:error m))))
  (with-test-out
    (println "\nRan" (:test m) "tests containing"
             (+ (:pass m) (:fail m) (:error m)) "assertions.")
    (println (:fail m) "failures," (:error m) "errors.")))
