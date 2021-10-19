(ns middleware.encoder-test
  (:refer-clojure :exclude [compile])
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [middleware.compile-stats :refer [register-program!]]
            [clojure.string :as str]
            [middleware.test-utils :refer [setup-fixture]]
            [middleware.compilation.parser :as p]
            [middleware.compilation.compiler :as cc]
            [middleware.compilation.encoder :as en]
            [middleware.program.emitter :as emit]))

; NOTE(Richo): You'll notice that most of these tests are not really making any
; assertion. They are here for two reasons: (1) exercise the compiler and encoder
; so that we catch exceptions early, and (2) provide programs for CompileStats.csv

(use-fixtures :once setup-fixture)

(defn compile [src]
  (let [ast (p/parse src)]
    (register-program! ast)
    (cc/compile-tree ast)))

(defn check-bytecodes [bytecodes src]
  ; HACK(Richo): Basic check to make sure we're actually producing some bytecodes
  (if (empty? (str/trim src))
    (is (= [0 0] bytecodes))
    (is (not= [0 0] bytecodes)))
  bytecodes)

(defn encode [src]
  (-> src
      compile
      en/encode
      (check-bytecodes src)))

(deftest empty-program
  (encode ""))

(deftest empty-script
  (encode "task main() running 1/s {}"))

(deftest empty-script-without-delay
  (encode "task main() running {}"))

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
    (is (= size (:size (emit/constant value))))))

(deftest script-with-local-vars
  (encode "task main() running 1/s { var a = 100; }"))

(deftest script-with-local-vars-2
  (encode "task main() running 1/s { var a = -100; }"))

(deftest script-with-local-vars-3
  (encode "task main() running 1/s { var a = -100; var b = 0.5; }"))

(deftest empty-script-without-running
  (encode "task main() {}"))

(deftest pause-resume-start-and-stop-instructions
  (encode "task main() running {
            pause main; stop main;
            resume main; start main;
          }"))

(deftest multiple-scripts
  (encode "
          task foo() {}
          task bar() running {}
          task main() running 1000/s {}"))

(deftest primitive-between-0-and-15
  (encode "task main() running 1/s { toggle(D13); }"))

(deftest primitive-between-16-and-31
  (encode "task main() running 1/s { turnOn(D13); }"))

(deftest primitive-between-32-and-286
  (encode "task main() running 1/s { delayM(1); }"))

(deftest script-with-conditionals
  (encode "
          task toggle() running 2/s {
            if isOn(D13) { turnOff(D13); }
            else { turnOn(D13); }
          }"))

(deftest script-with-conditionals-2
  (encode "
          task toggle() running 2/s {
            if isOn(D13) {}
            else { turnOn(D13); }
          }"))

(deftest script-with-while-loop
  (encode "
          task loop() {
            while isOff(D13) { turnOn(D13); }
          }"))

(deftest script-with-while-loop-2
  (encode "
          task loop() {
            while isOff(D13);
            turnOff(D13);
          }"))

(deftest script-with-global-variable-read-write
  (encode "
          var a;
          task main() {
            a = 3 + 4;
            a = a + 1;
            toggle(a);
          }"))

(deftest script-with-local-variable-read-write
  (encode "
          task main() {
            var a;
            a = 3 + 4;
            a = a + 1;
            toggle(a);
          }"))

(deftest script-with-for-loop-with-constant-step
  (encode "
          task loop() {
            for i = 0 to 10 by 2 { toggle(i); }
          }"))

(deftest script-with-for-loop-with-variable-step
  (encode "
          var step = 1;
          task loop() {
            for i = 0 to 10 by step { step = step * -1; }
          }"))

(deftest calling-a-procedure
  (encode "
          proc blink13() { toggle(D13); }
          task main() running 1/s { blink13(); }
          "))

(deftest calling-a-procedure-with-arguments
  (encode "
          proc blink(pin) { toggle(pin); }
          task main() running 1/s { blink(D13); }
          "))

(deftest calling-a-function
  (encode "
          func addition(a, b) { return a + b; }
          task main() running 1/s { toggle(addition(3, 4)); }
          "))


(deftest program-with-more-than-63-globals-of-the-same-size
  (let [program (emit/program :globals (map #(emit/variable % %) (range 64)))
        actual (en/encode program)]))

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
        actual (en/encode program)]))

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
        actual (en/encode program)]))

(deftest script-with-large-ticking-rate
  (encode "task b() running 19999999/s {}"))

(deftest script-with-default-constant-written-as-float
  (encode "task b() running { var a = 1.0; }"))

(deftest script-with-small-constant-written-as-float
  (encode "task b() running { var a = 10.0; }"))
