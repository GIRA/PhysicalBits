(ns user
  (:require [middleware.device.controller :as dc :refer [state]]
            [middleware.server.server :as server :refer [server]]
            [clojure.java.browse :refer [browse-url]])
  (:use [clojure.tools.namespace.repl :as repl :only [refresh-all]]))

(defn stop []
  (dc/stop-port-scan)
  (dc/disconnect)
  (server/stop))

(defn start []
  (server/start))

(defn reload []
  (stop)
  (repl/refresh :after 'dc/start-port-scan))

(defn millis [] (System/currentTimeMillis))

(defn open-browser []
  (browse-url "http://localhost:3000"))

(comment
 (require '[middleware.parser.parser :as new-parser])
 (require '[middleware.parser.old-parser :as old-parser])
 (use 'criterium.core)

 (def sources (mapv slurp
                    (filter #(.isFile %)
                            (file-seq (clojure.java.io/file "../../uzi/libraries")))))

 (def trees (time (mapv old-parser/parse sources)))

 (with-progress-reporting (bench (mapv new-parser/parse sources) :verbose))


 (def src (slurp "../../uzi/tests/syntax.uzi"))
 (with-progress-reporting (bench (new-parser/parse src) :verbose))


 ,)
