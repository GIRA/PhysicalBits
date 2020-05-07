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

; TODO(Richo): I don't know if this is needed
(def primitives [(ast/primitive-node "turnOn")
                 (ast/primitive-node "turnOff")
                 (ast/primitive-node "read")
                 (ast/primitive-node "write")
                 (ast/primitive-node "getPinMode")
                 (ast/primitive-node "setPinMode")
                 (ast/primitive-node "toggle")
                 (ast/primitive-node "getServoDegrees")
                 (ast/primitive-node "setServoDegrees")
                 (ast/primitive-node "servoWrite")
                 (ast/primitive-node "+" "add")
                 (ast/primitive-node "-" "subtract")
                 (ast/primitive-node "*" "multiply")
                 (ast/primitive-node "/" "divide")
                 (ast/primitive-node "sin")
                 (ast/primitive-node "cos")
                 (ast/primitive-node "tan")
                 (ast/primitive-node "==" "equals")
                 (ast/primitive-node "!=" "notEquals")
                 (ast/primitive-node ">" "greaterThan")
                 (ast/primitive-node ">=" "greaterThanOrEquals")
                 (ast/primitive-node "<" "lessThan")
                 (ast/primitive-node "<=" "lessThanOrEquals")
                 (ast/primitive-node "!" "negate")
                 (ast/primitive-node "delayMs")
                 (ast/primitive-node "&" "bitwiseAnd")
                 (ast/primitive-node "|" "bitwiseOr")
                 (ast/primitive-node "millis")
                 (ast/primitive-node "coroutine")
                 (ast/primitive-node "serialWrite")
                 (ast/primitive-node "round")
                 (ast/primitive-node "ceil")
                 (ast/primitive-node "floor")
                 (ast/primitive-node "sqrt")
                 (ast/primitive-node "abs")
                 (ast/primitive-node "ln")
                 (ast/primitive-node "log10")
                 (ast/primitive-node "exp")
                 (ast/primitive-node "pow10")
                 (ast/primitive-node "asin")
                 (ast/primitive-node "acos")
                 (ast/primitive-node "atan")
                 (ast/primitive-node "atan2")
                 (ast/primitive-node "**" "power")
                 (ast/primitive-node "isOn")
                 (ast/primitive-node "isOff")
                 (ast/primitive-node "%" "remainder")
                 (ast/primitive-node "constrain")
                 (ast/primitive-node "randomInt")
                 (ast/primitive-node "random")
                 (ast/primitive-node "isEven")
                 (ast/primitive-node "isOdd")
                 (ast/primitive-node "isPrime")
                 (ast/primitive-node "isWhole")
                 (ast/primitive-node "isPositive")
                 (ast/primitive-node "isNegative")
                 (ast/primitive-node "isDivisibleBy")
                 (ast/primitive-node "seconds")
                 (ast/primitive-node "isCloseTo")
                 (ast/primitive-node "delayS")
                 (ast/primitive-node "delayM")
                 (ast/primitive-node "minutes")
                 (ast/primitive-node "mod" "modulo")
                 (ast/primitive-node "startTone" "tone")
                 (ast/primitive-node "stopTone" "noTone")])

(deftest block-should-only-contain-statements
  (is (valid? "task foo() {}"))
  (is (valid? "task foo() running { toggle(D13); }"))
  (is (invalid? "task foo() stopped {4;}")))

#_(

  (deftest Test001BlockOnlyContainsStatements
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

  (deftest Test002AssignmentsAreNotExpressions
    (is (invalid? "var a; proc foo() { write(D13, a = 5); }"))
    (is (invalid? "var a; var b; proc foo() { b = (a = 5); }"))
    (is (invalid? "var a; proc foo() { if (a = 5) { turnOff(D13); }}")))

  (deftest Test003ConditionalsAreNotExpressions
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "nil"
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

  (deftest Test004AssignmentValueShouldBeAnExpression
    (is (invalid? (ast/program-node
      :globals [(ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))]
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/assignment-node
                          (ast/variable-node "a")
                          (ast/call-node "turnOff"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))])))
    (is (invalid? (ast/program-node
      :globals [(ast/variable-declaration-node
              "b"
              (ast/literal-number-node 0))]
      :primitives primitives
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

  (deftest Test005ConditionalBranchesShouldBeBlocks
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "nil"
              :body (ast/block-node
                  [(ast/conditional-node
                          (ast/call-node "read"
                              [(ast/arg-node (ast/literal-number-node 13))])
                          (ast/call-node "turnOff"
                              [(ast/arg-node (ast/literal-number-node 13))])
                          (ast/block-node []))]))])))
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "nil"
              :body (ast/block-node
                  [(ast/conditional-node
                          (ast/call-node "read"
                              [(ast/arg-node (ast/literal-number-node 13))])
                          (ast/block-node [])
                          (ast/call-node "turnOn"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

  (deftest Test006ConditionsShouldBeExpressions
    (is (invalid? "task foo() stopped { if turnOn(D13) { turnOff(D13); }}"))
    (is (invalid? "var a; task foo() stopped { if a = 3 { turnOff(D13); }}")))

  (deftest Test007LoopBodyShouldContainOnlyStatements
    (is (invalid? "task foo() stopped { while read(D13) { read(D13); }}"))
    (is (invalid? "task foo() stopped { do { read(D13); } while read(D13); }")))

  (deftest Test008LoopConditionOnlyAllowsOneExpression
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "nil"
              :body (ast/block-node
                  [(ast/until-node
                          (ast/block-node
                              [(ast/literal-number-node 1)
                                  (ast/call-node "read"
                                      [(ast/arg-node (ast/literal-number-node 13))])])
                          (ast/block-node []))]))]))))

  (deftest Test009ScriptStartReceiverIsAScriptRef
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/start-node ["an UziNumberLiteralNode"])]))]))))

  (deftest Test010ScriptStopReceiverIsAScriptRef
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/stop-node ["an UziNumberLiteralNode"])]))]))))

  (deftest Test011ScriptReferenceShouldReferenceExistingScript
    (is (invalid? "task foo() stopped { stop main; }")))

  (deftest Test012ScriptCallShouldEitherProvideNamedOrAnonymousArgumentsButNotBoth
    (is (invalid? "
    		func foo(a, b, c) { return a * b + c; }
    		task main() running {
    			foo(1, 2, c: 3);
    		}")))

  (deftest Test013ScriptArgumentsAndLocalsMustNotCollide
    (is (invalid? "task foo(a) stopped { var a; turnOff(D13); }")))

  (deftest Test014StartingAndStoppingCanOnlyBePerformedOnTasks
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
    		task main() stopped { stop foo; }")))

  (deftest Test015LoopConditionShouldBeAnExpression
    (is (invalid? "task foo() stopped { while turnOn(D13) { turnOff(D13); }}"))
    (is (invalid? "var a; task foo() stopped { while a = 3 { turnOff(D13); }}"))
    (is (invalid? (ast/program-node
      :globals [(ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))]
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "nil"
              :body (ast/block-node
                  [(ast/do-until-node
                          (ast/block-node
                              [(ast/variable-node "a")])
                          (ast/block-node []))]))]))))

  (deftest Test017PrimitiveCallArgumentsShouldBeExpressions
    (is (invalid? "var a; task foo() stopped { turnOn(a = 5); }"))
    (is (invalid? "task foo() stopped { turnOn(turnOn(D13)); }"))
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/call-node "turnOn"
                          [(ast/block-node
                              [(ast/call-node "turnOn"
                                      [(ast/arg-node (ast/literal-number-node 13))])])])]))]))))

  (deftest Test018RepeatTimesShouldBeAnExpression
    (is (invalid? "task foo() running {
    			repeat turnOn(13) {
    				toggle(D13);
    			}
    		}")))

  (deftest Test019RepeatBodyShouldBeABlock
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node
                  [(ast/repeat-node
                          (ast/literal-number-node 5)
                          (ast/call-node "turnOn"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

  (deftest Test020ScriptPauseReceiverIsAScriptRef
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/pause-node ["an UziNumberLiteralNode"])]))]))))

  (deftest Test021ScriptResumeReceiverIsAScriptRef
    (is (invalid? (ast/program-node
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node
                  [(ast/resume-node ["an UziNumberLiteralNode"])]))]))))

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
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node []))

          (ast/task-node
              :name "foo"
              :state "nil"
              :body (ast/block-node []))]))))

  (deftest Test025DuplicateGlobalsAreNotValid
    (is (invalid? (ast/program-node
      :globals [(ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))
          (ast/variable-declaration-node
              "a"
              (ast/literal-number-node 0))]
      :primitives primitives
      :scripts [(ast/procedure-node
              :name "foo"
              :body (ast/block-node []))]))))

  (deftest Test026DuplicateLocalsAreNotValid
    (is (invalid? (ast/program-node
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "running"
              :body (ast/block-node
                  [(ast/forever-node
                          (ast/call-node "turnOn"
                              [(ast/arg-node (ast/literal-number-node 13))]))]))]))))

  (deftest Test029ForBodyShouldBeABlock
    (is (invalid? (ast/program-node
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
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
      :primitives primitives
      :scripts [(ast/task-node
              :name "foo"
              :state "once"
              :tick-rate (ast/ticking-rate-node 1 "s")
              :body (ast/block-node []))]))))

  (deftest Test040TickingRateValueShouldAlwaysBePositive
    (is (invalid? "task foo() running 0/s {}"))
    (is (invalid? (ast/program-node
      :primitives primitives
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
      :primitives primitives
      :scripts []))))

  (deftest Test044AttemptingToInitializeANonExistingGlobalShouldFail
    (is (invalid? (ast/program-node
      :imports [(ast/import-node "t13" "test13.uzi"
              (ast/block-node
                  [(ast/assignment-node
                          (ast/variable-node "d")
                          (ast/literal-number-node 10))]))]
      :primitives primitives
      :scripts []))))

  (deftest Test045AttemptingToStartAnExistingTaskShouldWork
    (is (valid? (ast/program-node
      :imports [(ast/import-node "t13" "test13.uzi"
              (ast/block-node
                  [(ast/start-node ["foo"])]))]
      :primitives primitives
      :scripts []))))

  (deftest Test046ProgramWithPrimitiveDeclaration
    (is (valid? "prim add;"))
    (is (invalid? "prim unaPrimitivaQueNoExisteEnElSpec;")))

   )
