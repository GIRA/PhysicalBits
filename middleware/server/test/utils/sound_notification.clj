(ns utils.sound-notification
  (:require [clj-audio.core :refer [play ->stream]]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go]]))

(defn- play-sound! [sound-name]
  (when-let [resource (io/resource sound-name)]
    (go (play (->stream resource)))))

(defn play! [success?]
  (play-sound! (if success? "success.wav" "error.wav")))
