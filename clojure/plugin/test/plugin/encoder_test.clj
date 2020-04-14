(ns plugin.encoder-test
  (:require [clojure.test :refer :all]
            [plugin.compiler.core :as cc]
            [plugin.compiler.encoder :as en]))

(defn compile [src]
  (cc/compile-uzi-string src))

(defn encode [src]
  (-> src
      compile
      en/encode))

(deftest empty-program
  (let [expected [0 0]
        actual (encode "")]
    (is (= expected actual))))

(deftest empty-script
  (let [expected [1 1 5 3 232 192 3 0]
        actual (encode "task main() running 1/s {}")]
    (is (= expected actual))))

(deftest variables-have-size
  (doseq [[value size] {0 1,
                        16rFF 1,
                        16r100 2,
                        16rFFFF 2,
                        16r10000 3,
                        16rFFFFFF 3,
                        16r1000000 4,
                        16rFFFFFFFFFF 4,
                        0.5 4
                        -1 4}]
    (is (= size (en/variable-size value)))))

(deftest script-with-local-vars
  (let [expected [1 2 4 100 5 3 232 208 4 1 3 0]
        actual (encode "task main() running 1/s { var a = 100; }")]
    (is (= expected actual))))
