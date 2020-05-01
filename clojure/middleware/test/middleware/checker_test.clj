(ns middleware.checker-test
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [middleware.compiler.ast-utils :as ast-utils]
            [middleware.parser.parser :as pp]
            [middleware.compiler.checker :as checker])
  (:use [middleware.test-utils]))

(defmethod report :should-be-invalid [m]
  (with-test-out
    (inc-report-counter :fail)
    (println "\nFAIL in" (testing-vars-str m))
    (when (seq *testing-contexts*) (println (testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "should be invalid:" (pr-str (:program m)))
    (println "errors:" (pr-str (:errors m)))))

(defmethod report :should-be-valid [m]
  (with-test-out
    (inc-report-counter :fail)
    (println "\nFAIL in" (testing-vars-str m))
    (when (seq *testing-contexts*) (println (testing-contexts-str)))
    (when-let [message (:message m)] (println message))
    (println "should be valid:" (pr-str (:program m)))
    (println "errors:" (pr-str (:errors m)))))

(defmethod assert-expr 'invalid? [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           errors# (apply ~pred values#)
           result# (not (empty? errors#))
           expected#  (first values#)
           actual# (second values#)]
       (if result#
         (do-report {:type :pass
                     :message ~msg
                     :program (first values#)
                     :errors errors#})
         (do-report {:type :should-be-invalid
                     :message ~msg
                     :program (first values#)
                     :errors errors#}))
       result#)))

(defmethod assert-expr 'valid? [msg form]
  (let [args (rest form)
        pred (first form)]
    `(let [values# (list ~@args)
           errors# (apply ~pred values#)
           result# (empty? errors#)
           expected#  (first values#)
           actual# (second values#)]
       (if result#
         (do-report {:type :pass
                     :message ~msg
                     :program (first values#)
                     :errors errors#})
         (do-report {:type :should-be-valid
                     :message ~msg
                     :program (first values#)
                     :errors errors#}))
       result#)))

(defn check [src]
  (-> src pp/parse checker/check-tree))

(def invalid? check)
(def valid? check)

(deftest block-should-only-contain-statements
  (is (valid? "task foo() {}"))
  (is (valid? "task foo() running { toggle(D13); }"))
  (is (invalid? "task foo() stopped {4;}")))
