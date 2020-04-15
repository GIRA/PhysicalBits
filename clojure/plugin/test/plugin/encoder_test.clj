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

(deftest empty-script-without-delay
  (let [expected [1 0 128 0]
        actual (encode "task main() running {}")]
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

(deftest script-with-local-vars-2
  (let [expected [1 2 5 3 232 7 194 200 0 0 208 3 1 4 0]
        actual (encode "task main() running 1/s { var a = -100; }")]
    (is (= expected actual))))

(deftest script-with-local-vars-3
  (let [expected [1 3 5 3 232 11 194 200 0 0 63 0 0 0 208 3 2 4 5 0]
        actual (encode "task main() running 1/s { var a = -100; var b = 0.5; }")]
    (is (= expected actual))))

(deftest empty-script-without-running
  (let [expected [1 0 128 1 224]
        actual (encode "task main() {}")]
    (is (= expected actual))))

(deftest pause-resume-start-and-stop-instructions
  (let [expected [1 0 128 4 232 224 216 208]
        actual (encode "task main() running {
                          pause main; stop main;
                          resume main; start main;
                        }")]
    (is (= expected actual))))

(deftest multiple-scripts
  (let [expected [3 0 128 1 224 128 0 192 1 0]
        actual (encode "
                  task foo() {}
                  task bar() running {}
                  task main() running 1000/s {}")]
    (is (= expected actual))))

(deftest primitive-between-0-and-15
  (let [expected [1 2 4 13 5 3 232 192 4 2 131 162]
        actual (encode "task main() running 1/s { toggle(D13); }")]
    (is (= expected actual))))

(deftest primitive-between-16-and-31
  (let [expected [1 2 4 13 5 3 232 192 4 2 131 180]
        actual (encode "task main() running 1/s { turnOn(D13); }")]
    (is (= expected actual))))

(deftest primitive-between-32-and-286
  (let [expected [1 1 5 3 232 192 3 2 129 250 30]
        actual (encode "task main() running 1/s { delayM(1); }")]
    (is (= expected actual))))

(deftest script-with-conditionals
  (let [expected [1 2 4 13 5 1 244 192 4 8 131 250 15 241 3 131 181 240 2 131 180]
        actual (encode "
                  task toggle() running 2/s {
                    if isOn(D13) { turnOff(D13); }
                    else { turnOn(D13); }
                  }")]
    (is (= expected actual))))
