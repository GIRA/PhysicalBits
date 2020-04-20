(ns middleware.core-test
  (:require [clojure.test :refer :all]
            [middleware.core :refer :all]))

(deftest sanity-check
  (testing "Sanity check."
    (is (= 1 1))))
