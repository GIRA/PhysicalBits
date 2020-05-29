(ns middleware.utils.json
  (:require [clojure.walk :as w]
            [cheshire.core :as json]))


(defn- fix-invalid-floats [obj]
  "Hack to be able to encode special floats that JSON doesn't support"
  (w/postwalk #(if-not (number? %)
                 %
                 (cond
                   (Double/isNaN %) {:___NAN___ 0}
                   (= ##Inf %) {:___INF___ 1}
                   (= ##-Inf %) {:___INF___ -1}
                   :else %))
              obj))

(defn encode [obj]
  (-> obj
      fix-invalid-floats
      json/generate-string))

(defn decode [str]
  (-> str
      (json/parse-string true)
      fix-invalid-floats))