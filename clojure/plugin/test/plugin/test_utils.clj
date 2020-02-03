(ns plugin.test-utils
  (:require [clojure.test :refer :all]
            [clojure.data :as data]))

(defmethod assert-expr 'equivalent? [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           result# (apply ~pred values#)
           expected#  (first values#)
           actual# (second values#)]
       (do-report {:type (if result# :pass :fail)
                   :message ~msg
                   :expected expected#
                   :actual actual#})
       result#)))

(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))
