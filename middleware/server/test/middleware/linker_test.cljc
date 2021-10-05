(ns middleware.linker-test
  #?(:clj (:use [middleware.compile-stats]))
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [middleware.test-utils :refer [equivalent? setup-fixture without-internal-ids]]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.linker :as l]
            [middleware.compiler.emitter :as emit]
            [middleware.parser.ast-nodes :as ast]
            [middleware.compiler.utils.ast :as ast-utils]))

(use-fixtures :once setup-fixture)

(def lib-dir "../../uzi/tests")

(defn compile-uzi-string [src]
  #?(:clj (register-program! src :lib-dir lib-dir))
  (:compiled (cc/compile-uzi-string src :lib-dir lib-dir)))

(defn- without-prims-and-ids [ast]
 (-> ast
     (dissoc :primitives)
     (ast-utils/transform "UziCallNode"
                          (fn [node _] (dissoc node :primitive-name)))
     without-internal-ids))

(defn link [ast]
  "HACK(Richo): I remove the :primitives key because it makes the diff hard to read"
  (-> ast
      (l/resolve-imports lib-dir)
      without-prims-and-ids))

(def core-import (ast/import-node "core.uzi"))

(deftest
  importing-library-prepends-imported-tree-with-alias-applied
  " import test from 'test_1.uzi';"
  " task main() { write(D13, test.foo()); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "test" "test_1.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/literal-pin-node "D" 13))
                                      (ast/arg-node
                                        (ast/call-node "test.foo" []))])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "test" "test_1.uzi")]
                    :scripts [(ast/function-node
                               :name "test.foo"
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/literal-number-node 42))]))
                              (ast/task-node
                               :name "main"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "write"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))
                                         (ast/arg-node
                                          (ast/call-node "test.foo" []))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-the-same-library-twice-doesnt-collide
  " import test1 from 'test_1.uzi';"
  " import test2 from 'test_1.uzi';"
  " task main() { write(test2.foo(), test1.foo()); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "test1" "test_1.uzi")
                        (ast/import-node "test2" "test_1.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/call-node "test2.foo" []))
                                      (ast/arg-node
                                        (ast/call-node "test1.foo" []))])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import
                              (ast/import-node "test1" "test_1.uzi")
                              (ast/import-node "test2" "test_1.uzi")]
                    :scripts [(ast/function-node
                               :name "test1.foo"
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/literal-number-node 42))]))
                              (ast/function-node
                               :name "test2.foo"
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/literal-number-node 42))]))
                              (ast/task-node
                               :name "main"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "write"
                                        [(ast/arg-node
                                          (ast/call-node "test2.foo" []))
                                         (ast/arg-node
                                          (ast/call-node "test1.foo" []))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-nested-libraries
  " import t2 from 'test_3.uzi';"
  " task main() { write(D13, t2.bar(1)); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t2" "test_3.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/literal-pin-node "D" 13))
                                      (ast/arg-node
                                        (ast/call-node
                                          "t2.bar"
                                          [(ast/arg-node
                                             (ast/literal-number-node 1))]))])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t2" "test_3.uzi")]
                    :globals [(ast/variable-declaration-node
                               "t2.t1.v"
                               (ast/literal-number-node 42))]
                    :scripts [(ast/function-node
                               :name "t2.t1.foo"
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/literal-number-node 42))]))
                              (ast/function-node
                               :name "t2.bar"
                               :arguments [(ast/variable-declaration-node "a")]
                               :body (ast/block-node
                                      [(ast/return-node
                                        (ast/call-node
                                         "t2.+"
                                         [(ast/arg-node
                                           (ast/call-node
                                            "t2.+"
                                            [(ast/arg-node
                                              (ast/variable-node "t2.t1.v"))
                                             (ast/arg-node
                                              (ast/variable-node "a"))]))
                                          (ast/arg-node
                                           (ast/call-node "t2.t1.foo" []))]))]))
                              (ast/task-node
                               :name "main"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "write"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))
                                         (ast/arg-node
                                          (ast/call-node
                                           "t2.bar"
                                           [(ast/arg-node
                                             (ast/literal-number-node
                                              1))]))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  stopping-imported-task
  " import t3 from 'test_4.uzi';"
  " task main() running { stop t3.blink13; }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t3" "test_4.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/stop-node ["t3.blink13"])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t3" "test_4.uzi")]
                    :scripts [(ast/task-node
                               :name "t3.blink13"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t3.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/stop-node ["t3.blink13"])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  starting-imported-task
  " import t3 from 'test_4.uzi';"
  " task main() running { start t3.blink13; }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t3" "test_4.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/start-node ["t3.blink13"])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t3" "test_4.uzi")]
                    :scripts [(ast/task-node
                               :name "t3.blink13"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t3.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/start-node ["t3.blink13"])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-stops-a-task
  " import t from 'test_5.uzi';"
  " task main() running { t.stopTask(); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t" "test_5.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/call-node "t.stopTask" [])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t" "test_5.uzi")]
                    :scripts [(ast/procedure-node
                               :name "t.stopTask"
                               :body (ast/block-node [(ast/stop-node ["t.blink"])]))
                              (ast/task-node
                               :name "t.blink"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node "t.stopTask" [])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-starts-a-task
  " import t from 'test_6.uzi';"
  " task main() running { t.startTask(); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t" "test_6.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/call-node "t.startTask" [])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t" "test_6.uzi")]
                    :scripts [(ast/procedure-node
                               :name "t.startTask"
                               :body (ast/block-node [(ast/start-node ["t.blink"])]))
                              (ast/task-node
                               :name "t.blink"
                               :state "stopped"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node "t.startTask" [])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-calls-internal-proc
  " import t from 'test_8.uzi';"
  " task main() running { t.blink(); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t" "test_8.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/call-node "t.blink" [])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t" "test_8.uzi")]
                    :scripts [(ast/procedure-node
                               :name "t.toggleAndWait"
                               :arguments [(ast/variable-declaration-node "pin")
                                           (ast/variable-declaration-node "ms")]
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node (ast/variable-node "pin"))])
                                       (ast/call-node
                                        "t.delayMs"
                                        [(ast/arg-node
                                          (ast/variable-node "ms"))])]))
                              (ast/procedure-node
                               :name "t.blink"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggleAndWait"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))
                                         (ast/arg-node
                                          (ast/literal-number-node 100))])]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node "t.blink" [])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-executes-for-loop
  " import t from 'test_9.uzi';"
  " task main() running { t.foo(); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "t" "test_9.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node [(ast/call-node "t.foo" [])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import (ast/import-node "t" "test_9.uzi")]
                    :scripts [(ast/procedure-node
                               :name "t.foo"
                               :body (ast/block-node
                                      [(ast/for-node
                                        "i"
                                        (ast/literal-number-node 1)
                                        (ast/literal-number-node 10)
                                        (ast/literal-number-node 1)
                                        (ast/block-node [(ast/yield-node)]))]))
                              (ast/task-node
                               :name "main"
                               :state "running"
                               :body (ast/block-node [(ast/call-node "t.foo" [])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  specifying-init-block-to-initialize-variables
  " import t from 'test_10.uzi' {"
  "   a = 10; b = 20; c = 30;"
  " }"
  (let [ast (ast/program-node
              :imports [(ast/import-node
                          "t"
                          "test_10.uzi"
                          (ast/block-node
                            [(ast/assignment-node
                               (ast/variable-node "a")
                               (ast/literal-number-node 10))
                             (ast/assignment-node
                               (ast/variable-node "b")
                               (ast/literal-number-node 20))
                             (ast/assignment-node
                               (ast/variable-node "c")
                               (ast/literal-number-node 30))]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import
                              (ast/import-node
                               "t"
                               "test_10.uzi"
                               (ast/block-node
                                [(ast/assignment-node
                                  (ast/variable-node "a")
                                  (ast/literal-number-node 10))
                                 (ast/assignment-node
                                  (ast/variable-node "b")
                                  (ast/literal-number-node 20))
                                 (ast/assignment-node
                                  (ast/variable-node "c")
                                  (ast/literal-number-node 30))]))]
                    :globals [(ast/variable-declaration-node
                               "t.a"
                               (ast/literal-number-node 10))
                              (ast/variable-declaration-node
                               "t.b"
                               (ast/literal-number-node 20))
                              (ast/variable-declaration-node
                               "t.c"
                               (ast/literal-number-node 30))]
                    :scripts [(ast/task-node
                               :name "t.blink12"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 12))])]))
                              (ast/task-node
                               :name "t.setup"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/assignment-node
                                        (ast/variable-node "t.a")
                                        (ast/literal-number-node 1))
                                       (ast/assignment-node
                                        (ast/variable-node "t.b")
                                        (ast/literal-number-node 2))
                                       (ast/assignment-node
                                        (ast/variable-node "t.c")
                                        (ast/literal-number-node 3))]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  specifying-init-block-to-start-and-stop-tasks
  " import t from 'test_11.uzi' {"
  " \tstart stopped1; resume stopped2;"
  " \tstop running1; pause running2;"
  " }"
  (let [ast (ast/program-node
              :imports [(ast/import-node
                          "t"
                          "test_11.uzi"
                          (ast/block-node
                            [(ast/start-node ["stopped1"])
                             (ast/resume-node ["stopped2"])
                             (ast/stop-node ["running1"])
                             (ast/pause-node ["running2"])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import
                              (ast/import-node
                               "t"
                               "test_11.uzi"
                               (ast/block-node
                                [(ast/start-node ["stopped1"])
                                 (ast/resume-node ["stopped2"])
                                 (ast/stop-node ["running1"])
                                 (ast/pause-node ["running2"])]))]
                    :scripts [(ast/task-node
                               :name "t.stopped1"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node "t.running1" [])]))
                              (ast/task-node
                               :name "t.stopped2"
                               :tick-rate (ast/ticking-rate-node 2 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node "t.running2" [])]))
                              (ast/task-node
                               :name "t.running1"
                               :tick-rate (ast/ticking-rate-node 3 "s")
                               :state "stopped"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 12))])]))
                              (ast/task-node
                               :name "t.running2"
                               :tick-rate (ast/ticking-rate-node 4 "s")
                               :state "stopped"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.toggle"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  stopping-once-task-from-init-block-should-have-no-effect
  " import t from 'test_12.uzi' { stop setup; }"
  (let [ast (ast/program-node
              :imports [(ast/import-node
                          "t"
                          "test_12.uzi"
                          (ast/block-node [(ast/stop-node ["setup"])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import
                              (ast/import-node
                               "t"
                               "test_12.uzi"
                               (ast/block-node [(ast/stop-node ["setup"])]))]
                    :scripts [(ast/task-node
                               :name "t.setup"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "t.turnOn"
                                        [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-library-with-primitive-declarations
  " import a from 'test_13.uzi';"
  " task main() { toggle(a.add(3, 4)); }"
  (let [ast (ast/program-node
              :imports [(ast/import-node "a" "test_13.uzi")]
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node
                                        (ast/call-node
                                          "a.add"
                                          [(ast/arg-node (ast/literal-number-node 3))
                                           (ast/arg-node
                                             (ast/literal-number-node 4))]))])]))])
        expected (without-prims-and-ids
                   (ast/program-node
                    :imports [core-import
                              (ast/import-node "a" "test_13.uzi")]
                    :scripts [(ast/task-node
                               :name "a.test"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/variable-declaration-node
                                        "a"
                                        (ast/call-node
                                         "a.add"
                                         [(ast/arg-node
                                           (ast/literal-number-node 3))
                                          (ast/arg-node
                                           (ast/literal-number-node 4))]))
                                       (ast/variable-declaration-node
                                        "b"
                                        (ast/call-node
                                         "a.~="
                                         [(ast/arg-node
                                           (ast/literal-number-node 3))
                                          (ast/arg-node
                                           (ast/literal-number-node 4))]))
                                       (ast/call-node
                                        "a.write"
                                        [(ast/arg-node (ast/variable-node "a"))
                                         (ast/arg-node (ast/variable-node "b"))])]))
                              (ast/task-node
                               :name "main"
                               :state "once"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/call-node
                                           "a.add"
                                           [(ast/arg-node
                                             (ast/literal-number-node 3))
                                            (ast/arg-node
                                             (ast/literal-number-node
                                              4))]))])]))]))
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  dependency-cycles-should-not-hang-the-compiler
  " import a from 'test_14_a.uzi';"
  " import b from 'test_14_b.uzi';"
  (let [ast (ast/program-node
             :imports [(ast/import-node "a" "test_14_a.uzi")
                       (ast/import-node "b" "test_14_b.uzi")])]
    (is (thrown? #?(:clj Exception :cljs js/Error)
                 (link ast)))))

(deftest
  importing-non-existing-library-should-raise-error
  " import t from 'test0_NO_EXISTE.uzi';"
  (let [ast (ast/program-node
             :imports [(ast/import-node "t" "test0_NO_EXISTE.uzi")])]
    (is (thrown? #?(:clj Exception :cljs js/Error)
                 (link ast)))))

(deftest
  importing-a-script-with-a-local-var-that-shadows-global
  (let [expected (emit/program
                   :globals #{(emit/constant 0) (emit/variable "t.a" 0)
                              (emit/constant 100) (emit/constant 10)}
                   :scripts [(emit/script
                               :name "t.foo"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "b#1" 0)
                                        (emit/variable "a#2" 100)]
                               :instructions [(emit/push-value 10)
                                              (emit/write-global "t.a")
                                              (emit/read-global "t.a")
                                              (emit/write-local "b#1")
                                              (emit/read-local "a#2")
                                              (emit/write-local "b#1")])])
        actual (compile-uzi-string "import t from 'test_15.uzi';")]
    (is (equivalent? expected actual))))
