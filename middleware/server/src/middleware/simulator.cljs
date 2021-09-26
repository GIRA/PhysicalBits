(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.utils.program :as program]
            [middleware.compiler.encoder :as en]
            [middleware.output.logger :as logger]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.utils.async :refer-macros [go-try]]))

(defn init-dependencies []
  (fs/register-fs! #'browser/file)
  (ports/register-constructors! #'simulator/open-port))

(defn init []
  (init-dependencies)
  (println "Controller started successfully!")
  (.then (.-ready js/Simulator)
         (fn []
           (println "READY TO CONNECT!")
           (js/Simulator.start))))

(defn chan->promise [ch]
  (js/Promise. (fn [res rej]
                 (a/take! ch #(if (instance? js/Error %)
                                (rej %)
                                (res %))))))

(defn ^:export connect []
  (chan->promise
   (go
    (<! (dc/connect! "sim"))
    (some? (dc/connected?)))))

(defn ^:export disconnect []
  (chan->promise (dc/disconnect!)))

(defn ^:export compile [src type silent? update-fn]
  (chan->promise
   (go-try
    ; TODO(Richo): This code was copied (and modified slightly) from the controller/compile function!
    (try
      (let [compile-fn (case type
                         "json" cc/compile-json-string
                         "uzi" cc/compile-uzi-string)
            temp-program (-> (compile-fn src)
                             (update :compiled program/sort-globals)
                             (assoc :type type))
            ; TODO(Richo): This sucks, the IDE should take the program without modification.
            ; Do we really need the final-ast? It would be simpler if we didn't have to make
            ; this change. Also, this code is duplicated here and in the server...
            program (-> temp-program
                        (select-keys [:type :src :compiled])
                        (assoc :ast (:original-ast temp-program)))
            bytecodes (en/encode (:compiled program))
            output (when-not silent?
                     (logger/read-entries!) ; discard old entries
                     (logger/newline)
                     (logger/log "Program size (bytes): %1" (count bytecodes))
                     (logger/log (str bytecodes))
                     (logger/success "Compilation successful!")
                     (logger/read-entries!))]
        (update-fn (clj->js {:program program
                             :output (or output [])}))
        program)
      (catch :default ex
        (when-not silent?
          (logger/read-entries!) ; discard old entries
          (logger/newline)
          (logger/exception ex)
          ; TODO(Richo): Improve the error message for checker errors
          (when-let [{errors :errors} (ex-data ex)]
            (doseq [[^long i {:keys [description node src]}]
                    (map-indexed (fn [i e] [i e])
                                 errors)]
              (logger/error (str "├─ " (inc i) ". " description))
              (if src
                (logger/error (str "|     ..." src "..."))
                (when-let [id (:id node)]
                  (logger/error (str "|     Block ID: " id)))))
            (logger/error (str "└─ Compilation failed!")))
          (update-fn (clj->js {:output (logger/read-entries!)})))
        (throw ex))))))

(defn ^:export run [program]
  (js/Promise.resolve (dc/run (js->clj program :keywordize-keys true))))

(defn ^:export install [] "ACAACA")

(comment

 (doto (chan->promise
        (go-try (do
                  (println "1")
                  (<! (timeout 1000))
                  #_(throw "RICHO!")
                  (throw (ex-info "Richo capo" {})))))
       (.then (fn [result] (println "SUCCESS!" result)))
       (.catch (fn [reason] (println "ERROR!" reason))))


  (go (<! (dc/connect! "sim"))
      (println "CONNECTED?" (dc/connected?)))

  (def ex
    (try
      (throw (ex-info "RICHO" {}))
      ;(throw "Richo")
      (catch js/Error ex ex)
      (catch :default ex (ex-info (str ex) {:error ex}))))
 (def ex *1)
 ex
 (instance? js/Error ex)

(def err (js/Error. "RICHO!"))
(def err (ex-info "RICHO" {:a 1}))
 (ex-message err)


 (def p (ports/connect! "sim"))
 (ports/disconnect! p)
 (a/put! (:out p) [255 0 8])
 (a/take! (:in p) (fn [d]
                    (println "RECEIVED:" d)))


 (dc/run (c/compile-uzi-string "task blink13() running 5/s { toggle(D13); }"))

 (js/Simulator.start)
  (connect! "sim")
  (disconnect!)
  (go (set! (.-innerText js/document.body) "RICHO!!!"))
 (js/GPIO.getPinValue 13)
  (def interval (js/setInterval #(set!
                                  (.-innerText js/document.body)
                                  (str (js/GPIO.getPinValue 13)))
                                10))
 (js/clearInterval interval)
 ,,)
