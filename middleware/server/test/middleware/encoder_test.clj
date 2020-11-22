(ns middleware.encoder-test
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.encoder :as en]
            [middleware.compiler.utils.program :refer [value-size]]
            [middleware.compiler.emitter :as emit])
  (:use [middleware.test-utils]))

(defn compile [src]
  (register-program! src)
  (:compiled (cc/compile-uzi-string src)))

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

(deftest values-have-size
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
    (is (= size (value-size value)))))

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

(deftest script-with-conditionals-2
  (let [expected [1 2 4 13 5 1 244 192 4 5 131 250 15 242 2 131 180]
        actual (encode "
                  task toggle() running 2/s {
                    if isOn(D13) {}
                    else { turnOn(D13); }
                  }")]
    (is (= expected actual))))

(deftest script-with-while-loop
  (let [expected [1 1 4 13 128 7 131 250 16 241 3 131 180 240 250 224]
        actual (encode "
                  task loop() {
                    while isOff(D13) { turnOn(D13); }
                  }")]
    (is (= expected actual))))

(deftest script-with-while-loop-2
  (let [expected [1 1 4 13 128 6 131 250 16 242 253 131 181 224]
        actual (encode "
                  task loop() {
                    while isOff(D13);
                    turnOff(D13);
                  }")]
    (is (= expected actual))))

(deftest script-with-global-variable-read-write
  (let [expected [1 3 12 0 3 4 128 11 132 133 166 147 131 129 166 147 131 162 224]
        actual (encode "
                  var a;
                  task main() {
                    a = 3 + 4;
                    a = a + 1;
                    toggle(a);
                  }")]
    (is (= expected actual))))

(deftest script-with-local-variable-read-write
  (let [expected [1 2 8 3 4 144 1 0 11 131 132 166 255 128 255 0 129 166 255 128 255 0 162 224]
        actual (encode "
                  task main() {
                    var a;
                    a = 3 + 4;
                    a = a + 1;
                    toggle(a);
                  }")]
    (is (= expected actual))))

(deftest script-with-for-loop-with-constant-step
  (let [expected [1 2 8 2 10 144 1 0 14 128 255 128 255 0 132 175 241 7 255 0 162 255 0 131 166 255 128 240 245 224]
        actual (encode "
                  task loop() {
                    for i = 0 to 10 by 2 { toggle(i); }
                  }")]
    (is (= expected actual))))

(deftest script-with-for-loop-with-variable-step
  (let [expected [1 2 8 1 10 144 2 0 0 23 128 255 128 255 0 132 131 255 129 255 1 128 245 2 175 240 1 173 241 9 131 130 165 147 255 0 255 1 166 255 128 240 236 224]
        actual (encode "
                  var step = 1;
                  task loop() {
                    for i = 0 to 10 by step { step = step * -1; }
                  }")]
    (is (= expected actual))))

(deftest calling-a-procedure
  (let [expected [2 2 4 13 5 3 232 0 2 131 162 192 4 2 192 186]
        actual (encode "
                  proc blink13() { toggle(D13); }
                  task main() running 1/s { blink13(); }
                  ")]
    (is (= expected actual))))

(deftest calling-a-procedure-with-arguments
  (let [expected [2 2 4 13 5 3 232 32 1 2 255 0 162 192 4 3 131 192 186]
        actual (encode "
                  proc blink(pin) { toggle(pin); }
                  task main() running 1/s { blink(D13); }
                  ")]
    (is (= expected actual))))

(deftest calling-a-function
  (let [expected [2 3 8 3 4 5 3 232 32 2 4 255 0 255 1 166 187 192 5 4 131 132 192 162]
        actual (encode "
                  func addition(a, b) { return a + b; }
                  task main() running 1/s { toggle(addition(3, 4)); }
                  ")]
    (is (= expected actual))))


(deftest program-with-more-than-63-globals-of-the-same-size
  (let [program (emit/program :globals (map #(emit/variable % %) (range 64)))
        expected [0 64 252 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25 26 27 28 29 30 31 32 33 34 35 36 37 38 39 40 41 42 43 44 45 46 47 48 49 50 51 52 53 54 55 56 57 58 59 60 61 62 4 63]
        actual (en/encode program)]
    (is (= expected actual))))


(deftest script-with-127-instructions
  (let [program (emit/program
                 :globals [(emit/constant 0)
                           (emit/constant 16)]
                 :scripts [(emit/script
                            :name "test"
                            :running? true
                            :instructions (concat (mapcat (fn [_] [(emit/push-value 16)
                                                                   (emit/prim-call "toggle")])
                                                          (range 63))
                                                  [(emit/stop "test")]))])
        expected [1 1 4 16 128 127 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 224]
        actual (en/encode program)]
    (is (= expected actual))))

(deftest script-with-128-instructions
  (let [program (emit/program
                 :globals [(emit/constant 0)
                           (emit/constant 16)]
                 :scripts [(emit/script
                            :name "test"
                            :running? true
                            :instructions (mapcat (fn [_] [(emit/push-value 16)
                                                           (emit/prim-call "toggle")])
                                                  (range 64)))])
        expected [1 1 4 16 128 128 128 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162 131 162
                  131 162 131 162 131 162 131 162 131 162 131 162 131 162]
        actual (en/encode program)]
    (is (= expected actual))))

(deftest script-with-large-ticking-rate
  (let [expected [1 1 7 56 81 183 24 192 3 0]
        actual (encode "task b() running 19999999/s {}")]
    (is (= expected actual))))

(deftest script-with-default-constant-written-as-float
  (let [expected [1 0 144 1 1 0]
        actual (encode "task b() running { var a = 1.0; }")]
    (is (= expected actual))))

(deftest script-with-small-constant-written-as-float
  (let [expected [1 1 4 10 144 1 3 0]
        actual (encode "task b() running { var a = 10.0; }")]
    (is (= expected actual))))
