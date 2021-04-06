(ns middleware.sound-notification
  (:require [clojure.test :refer :all]
            ;[clojure.tools.logging :as log]
            [clj-audio.core :refer :all]
            [clojure.java.io :as io]
            [clojure.core.async :refer [go]]))

(defn- play-sound! [sound-name]
  (if-let [resource (io/resource sound-name)]
    (go (play (->stream resource)))))

(defn play! [success?]
  (play-sound! (if success? "success.wav" "error.wav")))
