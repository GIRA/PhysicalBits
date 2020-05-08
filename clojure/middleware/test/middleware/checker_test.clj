(ns middleware.checker-test
  (:refer-clojure :exclude [compile])
  (:require [clojure.test :refer :all]
            [middleware.compiler.ast-utils :as ast-utils]
            [middleware.parser.parser :as pp]
            [middleware.parser.ast-nodes :as ast]
            [middleware.compiler.linker :as linker]
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
        (linker/resolve-imports "../../uzi/tests")
        checker/check-tree)))

(def invalid? check)
(def valid? check)

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

#_(


  (deftest Test012ScriptCallShouldEitherProvideNamedOrAnonymousArgumentsButNotBoth
    (is (invalid? "
    		func foo(a, b, c) { return a * b + c; }
    		task main() running {
    			foo(1, 2, c: 3);
    		}")))

  (deftest Test013ScriptArgumentsAndLocalsMustNotCollide
    (is (invalid? "task foo(a) stopped { var a; turnOff(D13); }")))


  (deftest Test018RepeatTimesShouldBeAnExpression
    (is (invalid? "task foo() running {
    			repeat turnOn(13) {
    				toggle(D13);
    			}
    		}")))

  (deftest Test019RepeatBodyShouldBeABlock
    (is (invalid? (ast/program-node
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node
                  [(ast/repeat-node
                          (ast/literal-number-node 5)
                          (ast/call-node "turnOn"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))]))))


  (deftest Test022FunctionArgumentShouldNotBeRegisteredAsGlobal
    (is (invalid? "
    		task main() running 1 / s{
    			toggle(getPin(pin));
    		}

    		func getPin(pin){
    			return pin;
    		}")))

  (deftest Test023YieldShouldNotBeUsedAsExpression
    (is (invalid? "
    		func yield () {
    			return yield;
    		}")))

  (deftest Test024DuplicateScriptNamesAreNotValid
    (is (invalid? "
    		func test1 () {}
    		proc test1() {}"))
    (is (invalid? (ast/program-node
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node []))

          (ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node []))]))))

  (deftest Test025DuplicateGlobalsAreNotValid
    (is (invalid? (ast/program-node
      :globals [(ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))
          (ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))]
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node []))]))))

  (deftest Test026DuplicateLocalsAreNotValid
    (is (invalid? (ast/program-node
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/variable-declaration-node
                          "a"
                          (ast/literal-number-node 0))
                      (ast/variable-declaration-node
                          "a"
                          (ast/literal-number-node 0))]))]))))

  (deftest Test027DuplicateArgsAreNotValid
    (is (invalid? (ast/program-node
      :scripts [(ast/procedure-node
              :name "foo"
              :arguments [(ast/variable-declaration-node
                      "a"
                      (ast/literal-number-node 0))
                  (ast/variable-declaration-node
                      "a"
                      (ast/literal-number-node 0))]
              :body (ast/block-node []))]))))

  (deftest Test028ForeverBodyShouldBeABlock
    (is (invalid? (ast/program-node
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node
                  [(ast/forever-node
                          (ast/call-node "turnOn"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

  (deftest Test029ForBodyShouldBeABlock
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

  (deftest Test030ForCounterShouldBeAVariable
    (is (invalid? (ast/program-node
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node
                  [(ast/for-node
                          "an UziNumberLiteralNode"
                          (ast/literal-number-node 1)
                          (ast/literal-number-node 10)
                          (ast/literal-number-node 1)
                          (ast/block-node
                              [(ast/call-node "turnOn"
                                      [(ast/arg-node (ast/literal-number-node 13))])]))]))]))))

  (deftest Test031ForStartShouldBeAnExpression
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

  (deftest Test032ForStopShouldBeAnExpression
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

  (deftest Test033ForStepShouldBeAnExpression
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

  (deftest Test034AVariableShouldBeDeclaredBeforeItsFirstUse
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

  (deftest Test035VariablesDeclaredInsideABlockShouldNotBeAccessibleOutside
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

  (deftest Test036VariablesDeclaredInsideABlockShouldNotCollideWithVariablesAlreadyDeclaredOutside
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

  (deftest Test037VariablesDeclaredInsideABlockShouldNotCollideWithVariablesDeclaredOutsideAfterTheBlock
    (is (valid? "task foo() running {
    			if 1 { var a = 1; }
    			var a = 2;
    		}")))

  (deftest Test038LocalsAndArgsWithTheSameNameAreNotValid
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

  (deftest Test039TickingRateIsOnlyAllowedIfTaskStateIsSpecified
    (is (invalid? "task foo() 1/s {}"))
    (is (invalid? (ast/program-node
      :scripts [(ast/task-node
              :name "foo"
              :state "once"
              :tick-rate (ast/ticking-rate-node 1 "s")
              :body (ast/block-node []))]))))

  (deftest Test040TickingRateValueShouldAlwaysBePositive
    (is (invalid? "task foo() running 0/s {}"))
    (is (invalid? (ast/program-node
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :tick-rate (ast/ticking-rate-node -1 "s")
              :body (ast/block-node []))]))))

  (deftest Test041GlobalDeclarationsOnlyAllowLiterals
    (is (invalid? "var a = 3+4; task foo() {}"))
    (is (valid? "var a = 3; task foo() {}"))
    (is (valid? "var a = A3; task foo() {}"))
    (is (valid? "var a = D3; task foo() {}")))

  (deftest Test042ExpressionsAreNotAllowedInsideImportInitBlocks
    (is (invalid? "import a from 'A.uzi' { a = 3 + 4; }"))
    (is (valid? "import a from 'A.uzi' {}"))
    (is (valid? "import a from 'A.uzi';"))
    (is (valid? "import a from 'A.uzi' { a = 4; }")))

  (deftest Test043AttemptingToStartANonExistingTaskShouldFail
    (is (invalid? (ast/program-node
      :imports [(ast/import-node "t13" "test13.uzi"
              (ast/block-node
                  [(ast/start-node ["bar"])]))]
      :scripts []))))

  (deftest Test044AttemptingToInitializeANonExistingGlobalShouldFail
    (is (invalid? (ast/program-node
      :imports [(ast/import-node "t13" "test13.uzi"
              (ast/block-node
                  [(ast/assignment-node
                          (ast/variable-node "d")
                          (ast/literal-number-node 10))]))]
      :scripts []))))

  (deftest Test045AttemptingToStartAnExistingTaskShouldWork
    (is (valid? (ast/program-node
      :imports [(ast/import-node "t13" "test13.uzi"
              (ast/block-node
                  [(ast/start-node ["foo"])]))]
      :scripts []))))

  (deftest Test046ProgramWithPrimitiveDeclaration
    (is (valid? "prim add;"))
    (is (invalid? "prim unaPrimitivaQueNoExisteEnElSpec;")))

   )
