(ns middleware.code-generator-test
  (:refer-clojure :exclude [print])
  #?(:clj (:use [middleware.compile-stats]))
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [middleware.code-generator.code-generator :as cg]
            [middleware.ast.nodes :as ast]
            [middleware.test-utils :refer [setup-fixture]]))

(use-fixtures :once setup-fixture)

(defn print [ast]
  #?(:clj (register-program! ast))
  (cg/print ast))

(deftest
  empty-program
  (testing
    "An Empty program should return an empty string"
    (let [expected "" ast (ast/program-node) actual (print ast)]
      (is (= expected actual)))))

(deftest
  uninitialized-global-declaration
  (testing
    "An uninitialized Global variable should be printed on top of the program with it's default value"
    (let [expected "var a = 0;"
          ast (ast/program-node
                :globals [(ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 0))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  multiple-global-declaration
  (testing
    "Several global variables declared should be printed in the correct order and the defined value"
    (let [expected "var a = 5;\nvar b = 3;\nvar c = 0.5;\nvar d = D13;"
          ast (ast/program-node
                :globals [(ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 5))
                          (ast/variable-declaration-node
                            "b"
                            (ast/literal-number-node 3))
                          (ast/variable-declaration-node
                            "c"
                            (ast/literal-number-node 0.5))
                          (ast/variable-declaration-node
                            "d"
                            (ast/literal-pin-node "D" 13))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  empty-script-running-once
  (testing
    "An empty script without any statements nor tickrate"
    (let [expected "task foo() {}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "foo"
                            :state "once"
                            :body (ast/block-node []))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  empty-script-stopped
  (testing
    "An empty and stopped script without any statements "
    (let [expected "task bar() stopped {}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "bar"
                            :state "stopped"
                            :body (ast/block-node []))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  empty-script-ticking
  (testing
    "An empty ticking script without any statements "
    (let [expected "task baz() running 3/s {}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "baz"
                            :tick-rate (ast/ticking-rate-node 3 "s")
                            :state "running"
                            :body (ast/block-node []))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  blink13
  (testing
    "The classic blink example"
    (let [expected "task blink() running 1/s {\n\ttoggle(D13);\n}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "blink"
                            :tick-rate (ast/ticking-rate-node 1 "s")
                            :state "running"
                            :body (ast/block-node
                                    [(ast/call-node
                                       "toggle"
                                       [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  procedure-with-argument
  (testing
    "A procedure with a single argument"
    (let [expected "proc blink(arg0) {\n\tturnOn(arg0);\n\tdelayS(1);\n\tturnOff(arg0);\n}"
          ast (ast/program-node
                :scripts [(ast/procedure-node
                            :name "blink"
                            :arguments [(ast/variable-declaration-node
                                          "arg0"
                                          (ast/literal-number-node 0))]
                            :body (ast/block-node
                                    [(ast/call-node
                                       "turnOn"
                                       [(ast/arg-node (ast/variable-node "arg0"))])
                                     (ast/call-node
                                       "delayS"
                                       [(ast/arg-node (ast/literal-number-node 1))])
                                     (ast/call-node
                                       "turnOff"
                                       [(ast/arg-node
                                          (ast/variable-node "arg0"))])]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  function-with-arguments
  (testing
    "A Function with two arguments and a return"
    (let [expected "func default(arg0, arg1) {\n\treturn (arg0 % arg1);\n}"
          ast (ast/program-node
                :scripts [(ast/function-node
                            :name "default"
                            :arguments [(ast/variable-declaration-node
                                          "arg0"
                                          (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                          "arg1"
                                          (ast/literal-number-node 0))]
                            :body (ast/block-node
                                    [(ast/return-node
                                       (ast/call-node
                                         "%"
                                         [(ast/arg-node (ast/variable-node "arg0"))
                                          (ast/arg-node
                                            (ast/variable-node "arg1"))]))]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  functions-with-calls-and-globals
  (testing
    "A program with two functions that modify a global"
    (let [expected "var global = 0;\n\nfunc forIncrease(from, to, by) {\n\tfor i = from to to by by {\n\t\tglobal = (global + 1);\n\t}\n\treturn global;\n}\n\nfunc run() {\n\tvar temp = forIncrease(1, 10, 0.5);\n}"
          ast (ast/program-node
                :globals [(ast/variable-declaration-node
                            "global"
                            (ast/literal-number-node 0))]
                :scripts [(ast/function-node
                            :name "forIncrease"
                            :arguments [(ast/variable-declaration-node
                                          "from"
                                          (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                          "to"
                                          (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                          "by"
                                          (ast/literal-number-node 0))]
                            :body (ast/block-node
                                    [(ast/for-node
                                       "i"
                                       (ast/variable-node "from")
                                       (ast/variable-node "to")
                                       (ast/variable-node "by")
                                       (ast/block-node
                                         [(ast/assignment-node
                                            (ast/variable-node "global")
                                            (ast/call-node
                                              "+"
                                              [(ast/arg-node
                                                 (ast/variable-node "global"))
                                               (ast/arg-node
                                                 (ast/literal-number-node 1))]))]))
                                     (ast/return-node (ast/variable-node "global"))]))
                          (ast/function-node
                            :name "run"
                            :body (ast/block-node
                                    [(ast/variable-declaration-node
                                       "temp"
                                       (ast/call-node
                                         "forIncrease"
                                         [(ast/arg-node (ast/literal-number-node 1))
                                          (ast/arg-node (ast/literal-number-node 10))
                                          (ast/arg-node
                                            (ast/literal-number-node 0.5))]))]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  control-structures
  (testing
    "Several tasks with the main control structures on them"
    (let [expected "task while_loop() {\n\twhile 1 {\n\t\twhile 1;\n\t}\n}\n\ntask until_loop() {\n\tuntil 1 {\n\t\tuntil 1;\n\t}\n}\n\ntask repeat_forever() {\n\tforever {\n\t\trepeat 5 {}\n\t}\n}\n\ntask conditional() {\n\tif 1 {\n\t\tif 0 {\n\t\t\tdelayS(1000);\n\t\t}\n\t} else {\n\t\tdelayMs(1000);\n\t}\n}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "while_loop"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/while-node
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/while-node
                                            (ast/literal-number-node 1)
                                            (ast/block-node []))]))]))
                          (ast/task-node
                            :name "until_loop"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/until-node
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/until-node
                                            (ast/literal-number-node 1)
                                            (ast/block-node []))]))]))
                          (ast/task-node
                            :name "repeat_forever"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/forever-node
                                       (ast/block-node
                                         [(ast/repeat-node
                                            (ast/literal-number-node 5)
                                            (ast/block-node []))]))]))
                          (ast/task-node
                            :name "conditional"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/conditional-node
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/conditional-node
                                            (ast/literal-number-node 0)
                                            (ast/block-node
                                              [(ast/call-node
                                                 "delayS"
                                                 [(ast/arg-node
                                                    (ast/literal-number-node
                                                      1000))])])
                                            (ast/block-node []))])
                                       (ast/block-node
                                         [(ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/literal-number-node
                                                 1000))])]))]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  control-structures-part-II
  (testing
    "A task with a do while, do until and a yield"
    (let [expected "task test() {\n\tdo {\n\t\tvar a = 3;\n\t} until(1);\n\tdo {\n\t\tvar a = 4;\n\t\tyield;\n\t} while(1);\n}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "test"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/do-until-node
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/variable-declaration-node
                                            "a"
                                            (ast/literal-number-node 3))]))
                                     (ast/do-while-node
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/variable-declaration-node
                                            "a"
                                            (ast/literal-number-node 4))
                                          (ast/yield-node)]))]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  motor-usage
  (testing
    "Two tasks that operate a servo and a DC. This has some imports"
    (let [expected "import motor from 'DCMotor.uzi' {\n\tenablePin = D10;\n\tforwardPin = D9;\n\treversePin = D8;\n}\n\ntask servo() {\n\tforever {\n\t\tsetServoDegrees(D3, 90);\n\t\tdelayMs(1000);\n\t\tsetServoDegrees(D3, 0);\n\t\tdelayMs(1000);\n\t}\n}\n\ntask default1() running 20/m {\n\tmotor.forward(speed: 1);\n\tdelayMs(1000);\n\tmotor.brake();\n\tdelayMs(1000);\n}"
          ast (ast/program-node
                :imports [(ast/import-node
                            "motor"
                            "DCMotor.uzi"
                            (ast/block-node
                              [(ast/assignment-node
                                 (ast/variable-node "enablePin")
                                 (ast/literal-pin-node "D" 10))
                               (ast/assignment-node
                                 (ast/variable-node "forwardPin")
                                 (ast/literal-pin-node "D" 9))
                               (ast/assignment-node
                                 (ast/variable-node "reversePin")
                                 (ast/literal-pin-node "D" 8))]))]
                :scripts [(ast/task-node
                            :name "servo"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/forever-node
                                       (ast/block-node
                                         [(ast/call-node
                                            "setServoDegrees"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 3))
                                             (ast/arg-node
                                               (ast/literal-number-node 90))])
                                          (ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/literal-number-node 1000))])
                                          (ast/call-node
                                            "setServoDegrees"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 3))
                                             (ast/arg-node
                                               (ast/literal-number-node 0))])
                                          (ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/literal-number-node 1000))])]))]))
                          (ast/task-node
                            :name "default1"
                            :tick-rate (ast/ticking-rate-node 20 "m")
                            :state "running"
                            :body (ast/block-node
                                    [(ast/call-node
                                       "motor.forward"
                                       [(ast/arg-node
                                          "speed"
                                          (ast/literal-number-node 1))])
                                     (ast/call-node
                                       "delayMs"
                                       [(ast/arg-node
                                          (ast/literal-number-node 1000))])
                                     (ast/call-node "motor.brake" [])
                                     (ast/call-node
                                       "delayMs"
                                       [(ast/arg-node
                                          (ast/literal-number-node 1000))])]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  sonar-and-button
  (testing
    "Two tasks where one handles a button to start and stop the sonar one"
    (let [expected "import sonar from 'Sonar.uzi' {\n\ttrigPin = D11;\n\techoPin = D12;\n\tmaxDistance = 200;\n\tstart reading;\n}\nimport buttons from 'Buttons.uzi' {\n\tdebounceMs = 50;\n}\n\nvar variable1 = 0;\n\ntask sonar() stopped 1/h {\n\twrite(D13, sonar.distance_cm());\n}\n\ntask button() running 1/s {\n\tif variable1 {\n\t\tbuttons.waitForRelease(D7);\n\t\tvariable1 = (!variable1);\n\t\tstart sonar;\n\t} else {\n\t\tstop sonar;\n\t}\n}"
          ast (ast/program-node
                :imports [(ast/import-node
                            "sonar"
                            "Sonar.uzi"
                            (ast/block-node
                              [(ast/assignment-node
                                 (ast/variable-node "trigPin")
                                 (ast/literal-pin-node "D" 11))
                               (ast/assignment-node
                                 (ast/variable-node "echoPin")
                                 (ast/literal-pin-node "D" 12))
                               (ast/assignment-node
                                 (ast/variable-node "maxDistance")
                                 (ast/literal-number-node 200))
                               (ast/start-node ["reading"])]))
                          (ast/import-node
                            "buttons"
                            "Buttons.uzi"
                            (ast/block-node
                              [(ast/assignment-node
                                 (ast/variable-node "debounceMs")
                                 (ast/literal-number-node 50))]))]
                :globals [(ast/variable-declaration-node
                            "variable1"
                            (ast/literal-number-node 0))]
                :scripts [(ast/task-node
                            :name "sonar"
                            :tick-rate (ast/ticking-rate-node 1 "h")
                            :state "stopped"
                            :body (ast/block-node
                                    [(ast/call-node
                                       "write"
                                       [(ast/arg-node (ast/literal-pin-node "D" 13))
                                        (ast/arg-node
                                          (ast/call-node "sonar.distance_cm" []))])]))
                          (ast/task-node
                            :name "button"
                            :tick-rate (ast/ticking-rate-node 1 "s")
                            :state "running"
                            :body (ast/block-node
                                    [(ast/conditional-node
                                       (ast/variable-node "variable1")
                                       (ast/block-node
                                         [(ast/call-node
                                            "buttons.waitForRelease"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 7))])
                                          (ast/assignment-node
                                            (ast/variable-node "variable1")
                                            (ast/call-node
                                              "!"
                                              [(ast/arg-node
                                                 (ast/variable-node "variable1"))]))
                                          (ast/start-node ["sonar"])])
                                       (ast/block-node
                                         [(ast/stop-node ["sonar"])]))]))])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  primitive-definition
  (testing
    "Creating a few primitives"
    (let [expected "prim add;\nprim ~= : notEquals;\n\ntask test() {\n\tvar a = add(3, 4);\n\tvar b = (3 ~= 4);\n}"
          ast (ast/program-node
                :scripts [(ast/task-node
                            :name "test"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/variable-declaration-node
                                       "a"
                                       (ast/call-node
                                         "add"
                                         [(ast/arg-node (ast/literal-number-node 3))
                                          (ast/arg-node
                                            (ast/literal-number-node 4))]))
                                     (ast/variable-declaration-node
                                       "b"
                                       (ast/call-node
                                         "~="
                                         [(ast/arg-node (ast/literal-number-node 3))
                                          (ast/arg-node
                                            (ast/literal-number-node 4))]))]))]
                :primitives [(ast/primitive-node "add")
                             (ast/primitive-node "~=" "notEquals")])
          actual (print ast)]
      (is (= expected actual)))))

(deftest
  uzi-syntax
  (testing
    "A code that explores all the syntax of UZI. This is based on the file syntax.uzi"
    (let [expected "import foo from 'DCMotor.uzi';\nimport bar from 'Sonar.uzi' {\n\ttrigPin = 100;\n\techoPin = 200;\n\tstart reading;\n\tstop reading;\n\tpause reading;\n\tresume reading;\n}\n\nvar a = 10;\nvar b = 0.5;\nvar c = 0;\n\ntask blink13() running 2/s {\n\ttoggle(D13);\n}\n\ntask blink12() running 1/s {\n\ttoggle(D12);\n}\n\ntask setup() {\n\tif a {\n\t\tturnOn(D11);\n\t} else {\n\t\tturnOff(D11);\n\t}\n}\n\nfunc fact(n) {\n\tif (n == 0) {\n\t\treturn 1;\n\t}\n\treturn (n * fact((n - 1)));\n}\n\nproc foo_bar_baz(a, b, c) {\n\tvar d = ((a * b) + c);\n\trepeat d {\n\t\ttoggle(A2);\n\t}\n\tforever {\n\t\tstart blink13, blink12;\n\t\tstop blink13;\n\t\tyield;\n\t\tpause blink12, blink13;\n\t\tresume blink12;\n\t\tyield;\n\t\treturn;\n\t}\n\twhile (1 && 0) {\n\t\ttoggle(D10);\n\t\tdelayMs(1000);\n\t}\n\tuntil (0 || 0) {\n\t\ttoggle(D10);\n\t\tdelayMs(1000);\n\t}\n\twhile (1 >= 0);\n\tuntil (0 <= 1);\n\tdo {\n\t\ttoggle(D9);\n\t} while((1 > 0));\n\tdo {\n\t\ttoggle(D8);\n\t} until((0 < 1));\n\tfor i = 0 to 10 by 1 {\n\t\ttoggle(A0);\n\t\tdelayMs((i * 100));\n\t}\n\tvar e = foo.getSpeed();\n\tfoo.init(fact(((1 * -2) + -3.5)), (a + (b / d)), 0);\n\tbar.init(trig: a, echo: b, maxDist: c);\n}"
          ast (ast/program-node
                :imports [(ast/import-node
                            "foo"
                            "DCMotor.uzi"
                            (ast/block-node []))
                          (ast/import-node
                            "bar"
                            "Sonar.uzi"
                            (ast/block-node
                              [(ast/assignment-node
                                 (ast/variable-node "trigPin")
                                 (ast/literal-number-node 100))
                               (ast/assignment-node
                                 (ast/variable-node "echoPin")
                                 (ast/literal-number-node 200))
                               (ast/start-node ["reading"])
                               (ast/stop-node ["reading"])
                               (ast/pause-node ["reading"])
                               (ast/resume-node ["reading"])]))]
                :globals [(ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 10))
                          (ast/variable-declaration-node
                            "b"
                            (ast/literal-number-node 0.5))
                          (ast/variable-declaration-node
                            "c"
                            (ast/literal-number-node 0))]
                :scripts [(ast/task-node
                            :name "blink13"
                            :tick-rate (ast/ticking-rate-node 2 "s")
                            :state "running"
                            :body (ast/block-node
                                    [(ast/call-node
                                       "toggle"
                                       [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))
                          (ast/task-node
                            :name "blink12"
                            :tick-rate (ast/ticking-rate-node 1 "s")
                            :state "running"
                            :body (ast/block-node
                                    [(ast/call-node
                                       "toggle"
                                       [(ast/arg-node
                                          (ast/literal-pin-node "D" 12))])]))
                          (ast/task-node
                            :name "setup"
                            :state "once"
                            :body (ast/block-node
                                    [(ast/conditional-node
                                       (ast/variable-node "a")
                                       (ast/block-node
                                         [(ast/call-node
                                            "turnOn"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 11))])])
                                       (ast/block-node
                                         [(ast/call-node
                                            "turnOff"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 11))])]))]))
                          (ast/function-node
                            :name "fact"
                            :arguments [(ast/variable-declaration-node
                                          "n"
                                          (ast/literal-number-node 0))]
                            :body (ast/block-node
                                    [(ast/conditional-node
                                       (ast/call-node
                                         "=="
                                         [(ast/arg-node (ast/variable-node "n"))
                                          (ast/arg-node (ast/literal-number-node 0))])
                                       (ast/block-node
                                         [(ast/return-node
                                            (ast/literal-number-node 1))])
                                       (ast/block-node []))
                                     (ast/return-node
                                       (ast/call-node
                                         "*"
                                         [(ast/arg-node (ast/variable-node "n"))
                                          (ast/arg-node
                                            (ast/call-node
                                              "fact"
                                              [(ast/arg-node
                                                 (ast/call-node
                                                   "-"
                                                   [(ast/arg-node
                                                      (ast/variable-node "n"))
                                                    (ast/arg-node
                                                      (ast/literal-number-node
                                                        1))]))]))]))]))
                          (ast/procedure-node
                            :name "foo_bar_baz"
                            :arguments [(ast/variable-declaration-node
                                          "a"
                                          (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                          "b"
                                          (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                          "c"
                                          (ast/literal-number-node 0))]
                            :body (ast/block-node
                                    [(ast/variable-declaration-node
                                       "d"
                                       (ast/call-node
                                         "+"
                                         [(ast/arg-node
                                            (ast/call-node
                                              "*"
                                              [(ast/arg-node (ast/variable-node "a"))
                                               (ast/arg-node
                                                 (ast/variable-node "b"))]))
                                          (ast/arg-node (ast/variable-node "c"))]))
                                     (ast/repeat-node
                                       (ast/variable-node "d")
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "A" 2))])]))
                                     (ast/forever-node
                                       (ast/block-node
                                         [(ast/start-node ["blink13" "blink12"])
                                          (ast/stop-node ["blink13"])
                                          (ast/yield-node)
                                          (ast/pause-node ["blink12" "blink13"])
                                          (ast/resume-node ["blink12"])
                                          (ast/yield-node)
                                          (ast/return-node)]))
                                     (ast/while-node
                                       (ast/logical-and-node
                                         (ast/literal-number-node 1)
                                         (ast/literal-number-node 0))
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 10))])
                                          (ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/literal-number-node 1000))])]))
                                     (ast/until-node
                                       (ast/logical-or-node
                                         (ast/literal-number-node 0)
                                         (ast/literal-number-node 0))
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 10))])
                                          (ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/literal-number-node 1000))])]))
                                     (ast/while-node
                                       (ast/call-node
                                         ">="
                                         [(ast/arg-node (ast/literal-number-node 1))
                                          (ast/arg-node (ast/literal-number-node 0))])
                                       (ast/block-node []))
                                     (ast/until-node
                                       (ast/call-node
                                         "<="
                                         [(ast/arg-node (ast/literal-number-node 0))
                                          (ast/arg-node (ast/literal-number-node 1))])
                                       (ast/block-node []))
                                     (ast/do-while-node
                                       (ast/call-node
                                         ">"
                                         [(ast/arg-node (ast/literal-number-node 1))
                                          (ast/arg-node (ast/literal-number-node 0))])
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 9))])]))
                                     (ast/do-until-node
                                       (ast/call-node
                                         "<"
                                         [(ast/arg-node (ast/literal-number-node 0))
                                          (ast/arg-node (ast/literal-number-node 1))])
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "D" 8))])]))
                                     (ast/for-node
                                       "i"
                                       (ast/literal-number-node 0)
                                       (ast/literal-number-node 10)
                                       (ast/literal-number-node 1)
                                       (ast/block-node
                                         [(ast/call-node
                                            "toggle"
                                            [(ast/arg-node
                                               (ast/literal-pin-node "A" 0))])
                                          (ast/call-node
                                            "delayMs"
                                            [(ast/arg-node
                                               (ast/call-node
                                                 "*"
                                                 [(ast/arg-node
                                                    (ast/variable-node "i"))
                                                  (ast/arg-node
                                                    (ast/literal-number-node
                                                      100))]))])]))
                                     (ast/variable-declaration-node
                                       "e"
                                       (ast/call-node "foo.getSpeed" []))
                                     (ast/call-node
                                       "foo.init"
                                       [(ast/arg-node
                                          (ast/call-node
                                            "fact"
                                            [(ast/arg-node
                                               (ast/call-node
                                                 "+"
                                                 [(ast/arg-node
                                                    (ast/call-node
                                                      "*"
                                                      [(ast/arg-node
                                                         (ast/literal-number-node 1))
                                                       (ast/arg-node
                                                         (ast/literal-number-node
                                                           -2))]))
                                                  (ast/arg-node
                                                    (ast/literal-number-node
                                                      -3.5))]))]))
                                        (ast/arg-node
                                          (ast/call-node
                                            "+"
                                            [(ast/arg-node (ast/variable-node "a"))
                                             (ast/arg-node
                                               (ast/call-node
                                                 "/"
                                                 [(ast/arg-node
                                                    (ast/variable-node "b"))
                                                  (ast/arg-node
                                                    (ast/variable-node "d"))]))]))
                                        (ast/arg-node (ast/literal-number-node 0))])
                                     (ast/call-node
                                       "bar.init"
                                       [(ast/arg-node "trig" (ast/variable-node "a"))
                                        (ast/arg-node "echo" (ast/variable-node "b"))
                                        (ast/arg-node
                                          "maxDist"
                                          (ast/variable-node "c"))])]))])
          actual (print ast)]
      (is (= expected actual)))))
