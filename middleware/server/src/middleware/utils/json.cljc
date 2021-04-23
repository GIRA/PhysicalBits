(ns middleware.utils.json
  (:require [clojure.walk :as w]
            #?(:clj [cheshire.core :as json])))


(defn- fix-outgoing-floats [obj]
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
      fix-outgoing-floats
      json/generate-string))

(defn- fix-incoming-floats [obj]
  "Hack to be able to decode special floats that JSON doesn't support"
  (w/postwalk #(if-not (map? %)
                 %
                 (condp = (keys %)
                   [:___INF___] (if (= 1 (:___INF___ %)) ##Inf ##-Inf)
                   [:___NAN___] ##NaN
                   %))
              obj))

(defn decode [str]
  (-> str
      (json/parse-string true)
      fix-incoming-floats))
