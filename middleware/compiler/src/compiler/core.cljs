(ns compiler.core
    (:require [compiler.parser.parser :as pp]))

(enable-console-print!)

(println "This text is printed from src/compiler/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))


(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)


(defn ^:export parse [src]
  (clj->js (pp/parse src)))