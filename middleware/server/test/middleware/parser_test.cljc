(ns middleware.parser-test
  #?(:clj (:use [middleware.compile-stats]))
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [middleware.test-utils :refer [test-name equivalent? setup-fixture without-internal-ids]]
            [middleware.parser.parser :as pp]
            [middleware.parser.ast-nodes :as ast]))

(use-fixtures :once setup-fixture)

(def exclusions #{'custom-operator-precedence})

(defn parse [src]
  #?(:clj (if-not (contains? exclusions (symbol (test-name)))
          (register-program! src)))
  (without-internal-ids (pp/parse src)))

; HACK(Richo): The following function will create a program-node and then remove
; the internal ids so that it can be easily compared
(def program-node (comp without-internal-ids ast/program-node))

(deftest return-should-not-be-confused-with-call
  (let [src "task foo() { return (3) + 4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/literal-number-node 3))
                                        (ast/arg-node (ast/literal-number-node 4))]))]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest
  empty-program
  (testing
    "An Empty Program"
    (let [src ""
          expected (program-node)
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  parsing-negative-numbers-larger-than-minus-1
  (let [src "task foo() { return -0.5; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.5))]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest
  blink13
  (testing
    "A Task that blinks the pin 13"
    (let [src "task default() running 1/s {\n\ttoggle(D13);\n}"
          expected (program-node
                    :scripts [(ast/task-node
                               :name "default"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  procedure-with-argument
  (testing
    "A procedure with a single argument"
    (let [src "proc blink(arg0) {\n\tturnOn(arg0);\n\tdelayS(1);\n\tturnOff(arg0);\n}"
          expected (program-node
                    :scripts [(ast/procedure-node
                               :name "blink"
                               :arguments [(ast/variable-declaration-node "arg0")]
                               :body (ast/block-node
                                      [(ast/call-node
                                        "turnOn"
                                        [(ast/arg-node
                                          (ast/variable-node "arg0"))])
                                       (ast/call-node
                                        "delayS"
                                        [(ast/arg-node
                                          (ast/literal-number-node 1))])
                                       (ast/call-node
                                        "turnOff"
                                        [(ast/arg-node
                                          (ast/variable-node "arg0"))])]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  function-with-arguments
  (testing
    "A Function with two arguments and a return"
    (let [src "func default(arg0, arg1) {\n\treturn (arg0 % arg1);\n}"
          expected (program-node
                    :scripts [(ast/function-node
                               :name "default"
                               :arguments [(ast/variable-declaration-node "arg0")
                                           (ast/variable-declaration-node "arg1")]
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/call-node
                                         "%"
                                         [(ast/arg-node
                                           (ast/variable-node "arg0"))
                                          (ast/arg-node
                                           (ast/variable-node "arg1"))]))]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  functions-with-calls-and-globals
  (testing
    "A program with two functions that modify a global"
    (let [src "var global;\n\nfunc forIncrease(from, to, by) {\n\tfor i = from to to by by {\n\t\tglobal = (global + 1);\n\t}\n\treturn global;\n}\n\nfunc run() {\n\tvar temp = forIncrease(1, 10, 0.5);\n}"
          expected (program-node
                    :globals [(ast/variable-declaration-node "global")]
                    :scripts [(ast/function-node
                               :name "forIncrease"
                               :arguments [(ast/variable-declaration-node "from")
                                           (ast/variable-declaration-node "to")
                                           (ast/variable-declaration-node "by")]
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
                                              (ast/literal-number-node
                                               1))]))]))
                                       (ast/return-node
                                        (ast/variable-node "global"))]))
                              (ast/function-node
                               :name "run"
                               :body (ast/block-node
                                      [(ast/variable-declaration-node
                                        "temp"
                                        (ast/call-node
                                         "forIncrease"
                                         [(ast/arg-node
                                           (ast/literal-number-node 1))
                                          (ast/arg-node
                                           (ast/literal-number-node 10))
                                          (ast/arg-node
                                           (ast/literal-number-node
                                            0.5))]))]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  operator-precedence
  (testing
    "A Function with some of the operators to check if the correct precedence is being built"
    (let [src "func operate(arg0, arg1) {\n\treturn arg0 + arg1**2*3;\n}"
          expected (program-node
                    :scripts [(ast/function-node
                               :name "operate"
                               :arguments [(ast/variable-declaration-node "arg0")
                                           (ast/variable-declaration-node "arg1")]
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/call-node
                                         "+"
                                         [(ast/arg-node
                                           (ast/variable-node "arg0"))
                                          (ast/arg-node
                                           (ast/call-node
                                            "*"
                                            [(ast/arg-node
                                              (ast/call-node
                                               "**"
                                               [(ast/arg-node
                                                 (ast/variable-node "arg1"))
                                                (ast/arg-node
                                                 (ast/literal-number-node
                                                  2))]))
                                             (ast/arg-node
                                              (ast/literal-number-node
                                               3))]))]))]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  custom-operator-precedence
  (testing
    "a more complex squence of operators, including user defined ones"
    (let [src "\ntask blink13() running 2/s {\nreturn a * b/c**d+n ~ j ** 3;\n } "
          expected (program-node
                    :scripts [(ast/task-node
                               :name "blink13"
                               :tick-rate (ast/ticking-rate-node 2 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/call-node
                                         "~"
                                         [(ast/arg-node
                                           (ast/call-node
                                            "+"
                                            [(ast/arg-node
                                              (ast/call-node
                                               "/"
                                               [(ast/arg-node
                                                 (ast/call-node
                                                  "*"
                                                  [(ast/arg-node
                                                    (ast/variable-node
                                                     "a"))
                                                   (ast/arg-node
                                                    (ast/variable-node
                                                     "b"))]))
                                                (ast/arg-node
                                                 (ast/call-node
                                                  "**"
                                                  [(ast/arg-node
                                                    (ast/variable-node
                                                     "c"))
                                                   (ast/arg-node
                                                    (ast/variable-node
                                                     "d"))]))]))
                                             (ast/arg-node
                                              (ast/variable-node "n"))]))
                                          (ast/arg-node
                                           (ast/call-node
                                            "**"
                                            [(ast/arg-node
                                              (ast/variable-node "j"))
                                             (ast/arg-node
                                              (ast/literal-number-node
                                               3))]))]))]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  control-structures
  (testing
    "Several tasks with the main control structures on them"
    (let [src "task while_loop() {\n\twhile 1 {\n\t\twhile 1;\n\t}\n}\n\ntask until_loop() {\n\tuntil 1 {\n\t\tuntil 1;\n\t}\n}\n\ntask repeat_forever() {\n\tforever {\n\t\trepeat 5 {}\n\t}\n}\n\ntask conditional() {\n\tif 1 {\n\t\tif 0 {\n\t\t\tdelayS(1000);\n\t\t}\n\t} else {\n\t\tdelayMs(1000);\n\t}\n}"
          expected (program-node
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
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  control-structures-2
  (testing
    "A task with a do while, do until and a yield"
    (let [src "task test()\n{\n\tdo{var a = 3;}\n\tuntil(1);\n\tdo{\n\t\tvar a= 4;\n\t\tyield;\n\t}while(1);\n}"
          expected (program-node
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
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  motor-usage
  (testing
    "Two tasks that operate a servo and a DC. This has some imports"
    (let [src "import motor from 'DCMotor.uzi' {\n\tenablePin = D10;\n\tforwardPin = D9;\n\treversePin = D8;\n}\n\ntask servo() {\n\tforever {\n\t\tsetServoDegrees(D3, 90);\n\t\tdelayMs(1000);\n\t\tsetServoDegrees(D3, 0);\n\t\tdelayMs(1000);\n\t}\n}\n\ntask default1() running 20/m {\n\tmotor.forward(speed: 1);\n\tdelayMs(1000);\n\tmotor.brake();\n\tdelayMs(1000);\n}"
          expected (program-node
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
                                             (ast/literal-number-node
                                              1000))])]))]))
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
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  sonar-and-button
  (testing
    "Two tasks where one handles a button to start and stop the sonar one"
    (let [src "import sonar from 'Sonar.uzi' {\n\ttrigPin = D11;\n\techoPin = D12;\n\tmaxDistance = 200;\n\tstart reading;\n}\nimport buttons from 'Buttons.uzi' {\n\tdebounceMs = 50;\n}\n\nvar variable1;\n\ntask sonar() stopped 1/h {\n\twrite(D13, sonar.distance_cm());\n}\n\ntask button() running 1/s {\n\tif variable1 {\n\t\tbuttons.waitForRelease(D7);\n\t\tvariable1 = !variable1;\n\t\tstart sonar;\n\t} else {\n\t\tstop sonar;\n\t}\n}"
          expected (program-node
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
                    :globals [(ast/variable-declaration-node "variable1")]
                    :scripts [(ast/task-node
                               :name "sonar"
                               :tick-rate (ast/ticking-rate-node 1 "h")
                               :state "stopped"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "write"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))
                                         (ast/arg-node
                                          (ast/call-node
                                           "sonar.distance_cm"
                                           []))])]))
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
                                              (ast/variable-node
                                               "variable1"))]))
                                          (ast/start-node ["sonar"])])
                                        (ast/block-node
                                         [(ast/stop-node ["sonar"])]))]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  primitive-definition
  (testing
    "Creating a few primitives"
    (let [src "\nprim add;\nprim ~= : notEquals;\n\ntask test() {\n\tvar a = add(3, 4);\n\tvar b = 3 ~= 4;\n}"
          expected (program-node
                    :scripts [(ast/task-node
                               :name "test"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/variable-declaration-node
                                        "a"
                                        (ast/call-node
                                         "add"
                                         [(ast/arg-node
                                           (ast/literal-number-node 3))
                                          (ast/arg-node
                                           (ast/literal-number-node 4))]))
                                       (ast/variable-declaration-node
                                        "b"
                                        (ast/call-node
                                         "~="
                                         [(ast/arg-node
                                           (ast/literal-number-node 3))
                                          (ast/arg-node
                                           (ast/literal-number-node 4))]))]))]
                    :primitives [(ast/primitive-node "add")
                                 (ast/primitive-node "~=" "notEquals")])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  uzi-syntax
  (testing
    "A code that explores all the syntax of UZI. This is based on the file syntax.uzi"
    (let [src "\"This is just an example of code that uses all the available syntax\nin the language.\"\n\"I wrote it to help me create a syntax highlighter for the \"\"Ace\"\" editor\"\n\nimport foo from 'DCMotor.uzi';\nimport bar from 'Sonar.uzi' {\n  trigPin = 100;\n  echoPin = 200;\n  start reading;\n  stop reading;\n  pause reading;\n  resume reading;\n}\n\nvar a = 10;\nvar b = 0.5;\nvar c;\n\ntask blink13() running 2/s { toggle(D13); }\ntask blink12() running 1/s { toggle(D12); }\n\ntask setup() {\n    if a { turnOn(D11); }\n    else { turnOff(D11); }\n}\n\nfunc fact(n) {\n    if n == 0 { return 1; }\n    return n * fact(n - 1);\n}\n\nproc foo_bar_baz(a, b, c) {\n    var d = a * b + c;\n    repeat d { toggle(A2); }\n    forever {\n        start blink13, blink12;\n        stop blink13;\n        yield;\n        pause blink12, blink13;\n        resume blink12; yield;\n        return;\n    }\n    while 1 && 0 { toggle(D10); delayMs(1000); }\n    until 0 || 0 { toggle(D10); delayMs(1000); }\n    while 1 >= 0; \"Body is optional\"\n    until 0 <= 1; \"Body is optional\"\n    do { toggle(D9); } while 1 > 0;\n    do { toggle(D8); } until 0 < 1;\n    for i = 0 to 10 by 1 {\n        toggle(A0);\n        delayMs(i * 100);\n    }\n\tvar e = foo.getSpeed();\n\tfoo.init(fact(1 * -2 + -3.5), a + b/d, 0);\n\tbar.init(trig: a, echo: b, maxDist: c);\n}\n"
          expected (program-node
                    :imports [(ast/import-node
                               "foo"
                               "DCMotor.uzi")
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
                                             (ast/literal-pin-node
                                              "D"
                                              11))])]))]))
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
                                          (ast/arg-node
                                           (ast/literal-number-node 0))])
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
                                            [(ast/arg-node
                                              (ast/variable-node "a"))
                                             (ast/arg-node
                                              (ast/variable-node "b"))]))
                                          (ast/arg-node
                                           (ast/variable-node "c"))]))
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
                                             (ast/literal-number-node
                                              1000))])]))
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
                                             (ast/literal-number-node
                                              1000))])]))
                                       (ast/while-node
                                        (ast/call-node
                                         ">="
                                         [(ast/arg-node
                                           (ast/literal-number-node 1))
                                          (ast/arg-node
                                           (ast/literal-number-node 0))])
                                        (ast/block-node []))
                                       (ast/until-node
                                        (ast/call-node
                                         "<="
                                         [(ast/arg-node
                                           (ast/literal-number-node 0))
                                          (ast/arg-node
                                           (ast/literal-number-node 1))])
                                        (ast/block-node []))
                                       (ast/do-while-node
                                        (ast/call-node
                                         ">"
                                         [(ast/arg-node
                                           (ast/literal-number-node 1))
                                          (ast/arg-node
                                           (ast/literal-number-node 0))])
                                        (ast/block-node
                                         [(ast/call-node
                                           "toggle"
                                           [(ast/arg-node
                                             (ast/literal-pin-node "D" 9))])]))
                                       (ast/do-until-node
                                        (ast/call-node
                                         "<"
                                         [(ast/arg-node
                                           (ast/literal-number-node 0))
                                          (ast/arg-node
                                           (ast/literal-number-node 1))])
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
                                                   (ast/literal-number-node
                                                    1))
                                                  (ast/arg-node
                                                   (ast/literal-number-node
                                                    -2))]))
                                               (ast/arg-node
                                                (ast/literal-number-node
                                                 -3.5))]))]))
                                         (ast/arg-node
                                          (ast/call-node
                                           "+"
                                           [(ast/arg-node
                                             (ast/variable-node "a"))
                                            (ast/arg-node
                                             (ast/call-node
                                              "/"
                                              [(ast/arg-node
                                                (ast/variable-node "b"))
                                               (ast/arg-node
                                                (ast/variable-node
                                                 "d"))]))]))
                                         (ast/arg-node
                                          (ast/literal-number-node 0))])
                                       (ast/call-node
                                        "bar.init"
                                        [(ast/arg-node
                                          "trig"
                                          (ast/variable-node "a"))
                                         (ast/arg-node
                                          "echo"
                                          (ast/variable-node "b"))
                                         (ast/arg-node
                                          "maxDist"
                                          (ast/variable-node "c"))])]))])
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest
  spaces-1
  (let [src "task default() running 1/s {\n\ttoggle(D13 );\n}"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "default"
                             :tick-rate (ast/ticking-rate-node 1 "s")
                             :state "running"
                             :body (ast/block-node
                                    [(ast/call-node
                                      "toggle"
                                      [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest
  spaces-2
  (let [src "task default() running 1/s {\n\ttoggle (D13);\n}"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "default"
                             :tick-rate (ast/ticking-rate-node 1 "s")
                             :state "running"
                             :body (ast/block-node
                                    [(ast/call-node
                                      "toggle"
                                      [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest
  spaces-3
  (let [src "task default () running 1/s {\n\ttoggle(D13);\n}"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "default"
                             :tick-rate (ast/ticking-rate-node 1 "s")
                             :state "running"
                             :body (ast/block-node
                                    [(ast/call-node
                                      "toggle"
                                      [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest global-variables-can-be-declared-after-tasks-that-use-them
  (let [expected (parse "
                 var a = 0;
                 var b = 1;
                 task blink13() running 1/s { toggle(D13); }
                 task loop() { a = a + b; }
                 ")
        actual (parse "
                 task blink13() running 1/s { toggle(D13); }
                 var a = 0;
                 task loop() { a = a + b; }
                 var b = 1;
                 ")]
    (is (= expected actual))))

(deftest parsing-numbers-in-scientific-notation
  (let [src "task foo() { return 1.0E-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1.0e-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.0E-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.0e-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))

  (let [src "task foo() { return 1.0E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1.0e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.0E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.0e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))

  (let [src "task foo() { return 1E-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1e-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1E-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1e-4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.0001))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))

  (let [src "task foo() { return 1E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))

  (let [src "task foo() { return 1.E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1.e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.E4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1.e4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))

  (let [src "task foo() { return 1E+4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return 1e+4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1E+4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual)))
  (let [src "task foo() { return -1e+4; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -10000.0))]))])
        actual (parse src)]
    (is (equivalent? expected actual))))

(deftest large-integers-are-parsed-as-floats
  (let [src "task foo() { return 1000000000000; }"
        expected (program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node 1.0e12))]))])
        actual (parse src)]
    (is (equivalent? expected actual))))
