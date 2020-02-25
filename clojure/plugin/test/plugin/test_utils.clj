(ns plugin.test-utils
  (:require [clojure.test :refer :all]
            [clojure.data :as data]
            [ultra.test :as ultra-test]))

(defmethod assert-expr 'equivalent? [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           result# (apply ~pred values#)
           expected#  (first values#)
           actual# (second values#)]
       (if result#
         (do-report {:type :pass
                     :message ~msg
                     :expected expected#
                     :actual actual#})
         (do-report {:type :fail
                     :message ~msg
                     :expected expected#
                     :actual actual#
                     :diffs (ultra-test/generate-diffs expected# [actual#])}))
       result#)))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))
