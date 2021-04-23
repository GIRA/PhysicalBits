(ns middleware.checker-test
  (:refer-clojure :exclude [compile])
  #?(:clj (:use [middleware.compile-stats]))
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [middleware.compiler.utils.ast :as ast-utils]
            [middleware.parser.parser :as pp]
            [middleware.parser.ast-nodes :as ast]
            [middleware.compiler.linker :as linker]
            [middleware.compiler.checker :as checker]))

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
    (println "--------------------------")
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
  (let [ast (if (string? src)
              (pp/parse src)
              src)]
    (-> ast
        ast-utils/assign-internal-ids
        (linker/resolve-imports "../../uzi/tests")
        checker/check-tree)))

(def invalid? check)
(defn valid? [src]
  (register-program! src :lib-dir "../../uzi/tests")
  (check src))

(deftest block-should-only-contain-statements
  (is (valid? "task foo() {}"))
  (is (valid? "task foo() running { toggle(D13); }"))
  (is (invalid? "task foo() stopped {4;}"))
  (is (invalid? "var a; task foo() stopped {a;}"))
  (is (invalid? "task foo() stopped { read(4); }"))
  (is (invalid? "task foo() stopped { 3 > 4; }"))
  (is (invalid? "task foo() stopped {D13;}"))
  (is (invalid? "task foo() stopped {sin(5);}"))
  (is (invalid? "task foo() stopped { if 3 > 4 {4;}}"))
  (is (invalid? "task foo() stopped { if 3 > 4 {} else{4;}}"))
  (is (invalid? "task foo() stopped { if 3 > 4 {3;} else {turnOn(3);}}"))
  (is (invalid? "task foo() stopped { if 3 > 4 { turnOff(3);} else { 3;}}")))

(deftest call-args-should-be-expressions
  (is (valid? "task foo() { write(D9, read(A1)); }"))
  (is (invalid? "task foo() stopped { turnOn(turnOn(D13)); }"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/call-node "turnOn"
                                                   [(ast/block-node
                                                     [(ast/call-node "turnOn"
                                                                     [(ast/arg-node (ast/literal-number-node 13))])])])]))])))
  (is (invalid? "proc foo(a) { toggle(a); } task bar() stopped { foo(turnOn(D13)); }"))

  ; TODO(Richo): The following should fail but we are currently treating a script
  ; call as both an expresion and argument. We should change it to check if the
  ; script being called is a procedure or a function.
  #_(is (invalid? "proc foo(a) { toggle(a); } task bar() stopped { foo(foo(D13)); }")))

(deftest conditions-should-be-expressions
  (is (valid? "task foo() { if 1 { toggle(D13); }}"))
  (is (invalid? "task foo() stopped { if turnOn(D13) { turnOff(D13); }}"))
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node "a")]
                 :scripts [(ast/task-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/conditional-node
                                     (ast/assignment-node
                                      (ast/variable-node "a")
                                      (ast/literal-number-node 4))
                                     (ast/call-node
                                      "toggle"
                                      [(ast/arg-node (ast/literal-pin-node "D" 13))]))]))])))

  ; NOTE(Richo): The following test is not treating the condition as an assignment
  ; because the parser doesn't allow assignments in that place.
  (is (invalid? "var a; task foo() stopped { if a = 3 { turnOff(D13); }}")))

(deftest assignments-are-not-expressions
  (is (valid? "var a; proc foo() { a = 5; }"))
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node "a")]
                 :scripts [(ast/task-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/call-node "write"
                                                   [(ast/arg-node (ast/literal-pin-node "D" 13))
                                                    (ast/arg-node (ast/assignment-node
                                                                   (ast/variable-node "a")
                                                                   (ast/literal-number-node 5)))])]))])))

  ; NOTE(Richo): The following are not checking the assignment because the parser
  ; doesn't allow assignment in these places. The parser is treating the '=' as
  ; a call to a non existing script.
  (is (invalid? "var a; proc foo() { write(D13, a = 5); }"))
  (is (invalid? "var a; var b; proc foo() { b = (a = 5); }"))
  (is (invalid? "var a; proc foo() { if (a = 5) { turnOff(D13); }}")))

(deftest conditionals-are-not-expressions
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/call-node "write"
                                                   [(ast/arg-node (ast/literal-number-node 13))
                                                    (ast/arg-node (ast/conditional-node
                                                                   (ast/call-node "greaterThan"
                                                                                  [(ast/arg-node (ast/literal-number-node 3))
                                                                                   (ast/arg-node (ast/literal-number-node 4))])
                                                                   (ast/block-node
                                                                    [(ast/call-node "turnOff"
                                                                                    [(ast/arg-node (ast/literal-number-node 13))])])
                                                                   (ast/block-node [])))])]))]))))

(deftest assignment-value-should-be-an-expression
  (is (valid? "var a = 0; task foo() { a = 3 + 4; }"))
  (is (invalid? "var a = 0; task foo() { a = turnOff(D13); }"))
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node
                            "b"
                            (ast/literal-number-node 0))]
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/assignment-node
                                     (ast/variable-node "b")
                                     (ast/conditional-node
                                      (ast/call-node "<"
                                                     [(ast/arg-node (ast/literal-number-node 3))
                                                      (ast/arg-node (ast/literal-number-node 4))])
                                      (ast/block-node
                                       [(ast/literal-number-node 3)])
                                      (ast/block-node
                                       [(ast/literal-number-node 4)])))]))]))))

(deftest assignment-left-should-be-a-variable
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node
                            "b"
                            (ast/literal-number-node 0))]
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/assignment-node
                                     (ast/literal-number-node 3)
                                     (ast/literal-number-node 5))]))]))))


(deftest conditional-branches-should-be-blocks
  (is (valid? "task foo() { if 1 { turnOn(D13); } else { turnOff(D13); }}"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/conditional-node
                                     (ast/call-node "read"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/call-node "turnOff"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/block-node []))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/conditional-node
                                     (ast/call-node "read"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/block-node [])
                                     (ast/call-node "turnOn"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

(deftest loop-body-should-contain-only-statements
  (is (valid? "task foo() { while read(D13) { toggle(D12); delayS(1); }}"))
  (is (invalid? "task foo() stopped { while read(D13) { read(D13); }}"))
  (is (invalid? "task foo() stopped { do { read(D13); } while read(D13); }"))
  (is (invalid? "task foo() stopped { until read(D13) { read(D13); }}"))
  (is (invalid? "task foo() stopped { do { read(D13); } until read(D13); }")))

(deftest loop-condition-should-be-an-expression
  (is (invalid? "task foo() stopped { while turnOn(D13) { turnOff(D13); }}"))
  (is (invalid? "var a; task foo() stopped { while a = 3 { turnOff(D13); }}"))
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 0))]
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/do-until-node
                                     (ast/block-node
                                      [(ast/variable-node "a")])
                                     (ast/block-node []))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/while-node
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])
                                       (ast/call-node "turnOff"
                                                      [(ast/arg-node (ast/literal-number-node 13))])])
                                     (ast/block-node []))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/until-node
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])
                                       (ast/call-node "turnOff"
                                                      [(ast/arg-node (ast/literal-number-node 13))])])
                                     (ast/block-node []))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/do-while-node
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])
                                       (ast/call-node "turnOff"
                                                      [(ast/arg-node (ast/literal-number-node 13))])])
                                     (ast/block-node []))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/do-until-node
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])
                                       (ast/call-node "turnOff"
                                                      [(ast/arg-node (ast/literal-number-node 13))])])
                                     (ast/block-node []))]))]))))

(deftest loop-body-should-be-a-block
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/while-node
                                     (ast/literal-number-node 1)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/until-node
                                     (ast/literal-number-node 1)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/do-while-node
                                     (ast/literal-number-node 1)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/do-until-node
                                     (ast/literal-number-node 1)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

(deftest script-control-nodes-should-refer-to-existing-scripts
  (is (valid? "task foo() stopped { toggle(D13); }
               task main() { start foo; }"))
  (is (invalid? "task foo() stopped { stop main; }"))
  (is (invalid? "
         func foo() { return 42; }
         task main() stopped { start foo; }"))
  (is (invalid? "
         func foo() { return 42; }
         task main() stopped { stop foo; }"))
  (is (invalid? "
         proc foo() { turnOn(D13); }
         task main() stopped { start foo; }"))
  (is (invalid? "
         proc foo() { turnOn(D13); }
         task main() stopped { stop foo; }"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/start-node [(ast/literal-number-node 42)])]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/stop-node [(ast/literal-number-node 42)])]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/pause-node [(ast/literal-number-node 42)])]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/resume-node [(ast/literal-number-node 42)])]))]))))

(deftest variable-names-should-not-collide
  (is (valid? "task foo(a) stopped { var b; turnOff(D13); }"))
  (is (valid? "var a = 100; task foo() { var a = 10; turnOff(a); }"))
  (is (valid? "task foo() {
                 if 1 { var a = 100; }
                 else { var a = 200; }
               }"))
  (is (invalid? "task foo(a, b, a) {}"))
  (is (invalid? "proc foo(a, b, a) {}"))
  (is (invalid? "func foo(a, b, a) {}"))
  (is (invalid? "task foo(a) stopped { var a; turnOff(D13); }"))
  (is (invalid? (ast/program-node
                 :globals [(ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 0))
                           (ast/variable-declaration-node
                            "a"
                            (ast/literal-number-node 0))]
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node []))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/block-node
                                   [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 0))
                                    (ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 0))]))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :arguments [(ast/variable-declaration-node
                                         "a"
                                         (ast/literal-number-node 0))
                                        (ast/variable-declaration-node
                                         "a"
                                         (ast/literal-number-node 0))]
                            :body (ast/block-node []))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :arguments [(ast/variable-declaration-node
                                         "a"
                                         (ast/literal-number-node 0))]
                            :body (ast/block-node
                                   [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 0))]))]))))

(deftest function-argument-should-not-be-registered-as-global
  (is (invalid? "
        task main() running 1 / s{
          toggle(getPin(pin));
        }

        func getPin(pin){
          return pin;
        }")))

(deftest script-names-should-not-collide
  (is (invalid? "
          func test1 () {}
          proc test1() {}")))

(deftest variables-should-be-declared-before-first-use
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node "+"
                                                    [(ast/arg-node (ast/variable-node "a"))
                                                     (ast/arg-node (ast/literal-number-node 1))]))
                                    (ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 5))]))]))))

(deftest variables-declared-inside-a-block-should-not-be-accessible-outside-of-it
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/conditional-node
                                     (ast/call-node "greaterThan"
                                                    [(ast/arg-node (ast/literal-number-node 3))
                                                     (ast/arg-node (ast/literal-number-node 4))])
                                     (ast/block-node
                                      [(ast/variable-declaration-node
                                        "a"
                                        (ast/literal-number-node 5))])
                                     (ast/block-node []))
                                    (ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node "+"
                                                    [(ast/arg-node (ast/variable-node "a"))
                                                     (ast/arg-node (ast/literal-number-node 1))]))]))]))))

(deftest variables-declared-inside-a-block-should-not-collide-with-variables-already-declared-outside
  (is (valid? "task foo() running {
                if 1 { var a = 1; }
                var a = 2;
              }"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 5))
                                    (ast/conditional-node
                                     (ast/call-node "greaterThan"
                                                    [(ast/arg-node (ast/literal-number-node 3))
                                                     (ast/arg-node (ast/literal-number-node 4))])
                                     (ast/block-node
                                      [(ast/variable-declaration-node
                                        "a"
                                        (ast/literal-number-node 5))])
                                     (ast/block-node []))
                                    (ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node "+"
                                                    [(ast/arg-node (ast/variable-node "a"))
                                                     (ast/arg-node (ast/literal-number-node 1))]))]))]))))


(deftest repeat-times-should-be-an-expression
  (is (valid? "task foo() { repeat 4 * 2 { toggle(D13); }}"))
  (is (valid? "var a = 100; task foo() { repeat a { toggle(D13); }}"))
  (is (invalid? "task foo() running {
                   repeat turnOn(13) {
                     toggle(D13);
                   }
                 }")))

(deftest repeat-body-should-be-a-block
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/repeat-node
                                     (ast/literal-number-node 5)
                                     (ast/call-node "turnOn"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

(deftest forever-body-should-be-a-block
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/forever-node
                                     (ast/call-node "turnOn"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

(deftest for-body-should-be-a-block
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/for-node
                                     "a"
                                     (ast/literal-number-node 1)
                                     (ast/literal-number-node 10)
                                     (ast/literal-number-node 1)
                                     (ast/call-node "turnOn"
                                                    [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

(deftest for-counter-should-be-a-variable
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(assoc (ast/for-node
                                            "?"
                                            (ast/literal-number-node 1)
                                            (ast/literal-number-node 10)
                                            (ast/literal-number-node 1)
                                            (ast/block-node
                                             [(ast/call-node "turnOn"
                                                             [(ast/arg-node (ast/literal-number-node 13))])]))
                                           :counter (ast/literal-number-node 42))]))]))))

(deftest for-start-should-be-an-expression
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/for-node
                                     "a"
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/literal-number-node 10)
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])]))]))]))))

(deftest for-stop-should-be-an-expression
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/for-node
                                     "a"
                                     (ast/literal-number-node 1)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])]))]))]))))

(deftest for-step-should-be-an-expression
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/for-node
                                     "a"
                                     (ast/literal-number-node 1)
                                     (ast/literal-number-node 10)
                                     (ast/call-node "toggle"
                                                    [(ast/arg-node (ast/literal-number-node 13))])
                                     (ast/block-node
                                      [(ast/call-node "turnOn"
                                                      [(ast/arg-node (ast/literal-number-node 13))])]))]))]))))


(deftest return-value-should-either-be-nil-or-an-expression
  (is (valid? "proc a() { return; }"))
  (is (valid? "func a() { return 1; }"))
  (is (invalid? "proc a() { return toggle(D13); }"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :body (ast/block-node
                                   [(ast/return-node (ast/yield-node))]))]))))


(deftest ticking-rate-is-only-allowed-if-state-is-specified
  (is (invalid? "task foo() 1/s {}"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "once"
                            :tick-rate (ast/ticking-rate-node 1 "s")
                            :body (ast/block-node []))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :tick-rate (ast/ticking-rate-node 1 "s")
                            :body (ast/block-node []))]))))

(deftest ticking-rate-value-should-always-be-positive
  (is (invalid? "task foo() running 0/s {}"))
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :state "running"
                            :tick-rate (ast/ticking-rate-node -1 "s")
                            :body (ast/block-node []))]))))

(deftest global-declarations-only-allow-literals
  (is (invalid? "var a = 3+4; task foo() {}"))
  (is (valid? "var a = 3; task foo() {}"))
  (is (valid? "var a = A3; task foo() {}"))
  (is (valid? "var a = D3; task foo() {}")))

(deftest script-call-should-either-provide-named-or-anonymous-arguments-but-not-both
  (is (valid? "func foo(a,b,c) { return a * b + c; }
               task main() { foo(1, 2, 3); }"))
  (is (valid? "func foo(a,b,c) { return a * b + c; }
               task main() { foo(c: 1, a: 2, b: 3); }"))
  (is (invalid? "
        func foo(a, b, c) { return a * b + c; }
        task main() running {
          foo(1, 2, c: 3);
        }")))

(deftest program-with-primitive-declaration
  (is (valid? "prim add;"))
  (is (invalid? "prim unaPrimitivaQueNoExisteEnElSpec;")))

(deftest only-compile-time-expressions-are-allowed-inside-import-init-blocks
  (is (invalid? "import a from 'test_16.uzi' { a = 3 + 4; }"))
  (is (valid? "import a from 'test_16.uzi' {}"))
  (is (valid? "import a from 'test_16.uzi';"))
  (is (valid? "import a from 'test_16.uzi' { a = 4; }")))

(deftest attempting-to-initialize-a-non-existing-global-should-fail
  (is (invalid? (ast/program-node
                 :imports [(ast/import-node "t" "test_17.uzi"
                                            (ast/block-node
                                             [(ast/assignment-node
                                               (ast/variable-node "d")
                                               (ast/literal-number-node 10))]))]
                 :scripts []))))

(deftest attempting-to-start-or-stop-a-non-existing-task-should-fail
  (is (valid? "import t from 'test_17.uzi' { start foo; }"))
  (is (valid? "import t from 'test_17.uzi' { stop foo; }"))
  (is (valid? "import t from 'test_17.uzi' { resume foo; }"))
  (is (valid? "import t from 'test_17.uzi' { pause foo; }"))
  (is (invalid? "import t from 'test_17.uzi' { start bar; }"))
  (is (invalid? "import t from 'test_17.uzi' { stop bar; }"))
  (is (invalid? "import t from 'test_17.uzi' { resume bar; }"))
  (is (invalid? "import t from 'test_17.uzi' { pause bar; }")))

(deftest import-alias-should-not-collide
  (is (invalid? "import t from 'test_18.uzi';
                 import t from 'test_18.uzi';"))
  (is (invalid? "import t from 'test_17.uzi';
                 import t from 'test_18.uzi';")))

(deftest script-body-should-be-a-block
  (is (invalid? (ast/program-node
                 :scripts [(ast/task-node
                            :name "foo"
                            :body (ast/literal-number-node 13))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/procedure-node
                            :name "foo"
                            :body (ast/literal-number-node 13))])))
  (is (invalid? (ast/program-node
                 :scripts [(ast/function-node
                            :name "foo"
                            :body (ast/literal-number-node 13))]))))

(deftest primitive-calls-should-have-the-correct-number-of-args
  (is (invalid? "task foo() { write(D13); }")))

(deftest logical-operators-should-have-expression-operands
  (is (valid? "task foo() { if 1 && 0 { toggle(D13); }}"))
  (is (valid? "task foo() { if 1 || 0 { toggle(D13); }}"))
  (is (invalid? "task foo() { if 1 && turnOn(13) { toggle(D13); }}"))
  (is (invalid? "task foo() { if 1 || turnOn(13) { toggle(D13); }}")))
