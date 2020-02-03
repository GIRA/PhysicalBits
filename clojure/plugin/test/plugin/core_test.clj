(ns plugin.core-test
  (:require [clojure.test :refer :all]
            [plugin.core :refer :all]))

(deftest sanity-check
  (testing "Sanity check."
    (is (= 1 1))))
