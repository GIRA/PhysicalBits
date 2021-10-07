(ns middleware.compiler-test
  (:refer-clojure :exclude [compile])
  #?(:clj (:use [middleware.compile-stats]))
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [middleware.test-utils :refer [equivalent? setup-fixture without-internal-ids]]
            [clojure.string :as str]
            [clojure.walk :as w]
            [middleware.ast.nodes :as ast]
            [middleware.ast.utils :as ast-utils]
            [middleware.compiler.compiler :as cc]
            [middleware.program.emitter :as emit]))

(use-fixtures :once setup-fixture)

(defn compile [ast]
  #?(:clj (register-program! ast))
  (cc/compile-tree ast ""))

(defn- NaN? [n] (not (== n n)))

(deftest constants-support-special-floats
  (is (= ##Inf (:value (emit/constant ##Inf))))
  (is (= ##-Inf (:value (emit/constant ##-Inf))))
  (is (NaN? (:value (emit/constant ##NaN)))))

(deftest
  empty-program-test
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "empty"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 1000)}
                   :scripts [(emit/script
                               :name "empty"
                               :delay 1000
                               :running? true)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-global-variable-test
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "counter")]
              :scripts [(ast/task-node
                          :name "empty"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "counter")
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "counter"))
                                        (ast/arg-node
                                          (ast/literal-number-node 1))]))]))])
        expected (emit/program
                   :globals #{(emit/variable "counter" 0) (emit/constant 1000)
                              (emit/constant 1)}
                   :scripts [(emit/script
                               :name "empty"
                               :delay 1000
                               :running? true
                               :instructions [(emit/read-global "counter")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-global "counter")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  task-without-ticking-rate
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "foo"
                          :state "running"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script :name "foo" :delay 0 :running? true)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  task-with-once
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "foo"
                          :state "once"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 0
                               :running? true
                               :once? true)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-local-variable
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "foo"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node
                                     "pin"
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/literal-number-node 1))
                                        (ast/arg-node
                                          (ast/literal-pin-node "D" 13))]))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "pin"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "pin#1" 0)]
                               :instructions [(emit/push-value 1)
                                              (emit/push-value 13)
                                              (emit/prim-call "add")
                                              (emit/write-local "pin#1")
                                              (emit/read-local "pin#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  ast-transform-test
  (let [original (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "empty"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])
                                       (ast/call-node
                                        "turnOn"
                                        [(ast/arg-node
                                          (ast/literal-number-node 12))])]))]))
        expected (w/prewalk
                  (fn [node] (if (and (map? node)
                                      (not (ast-utils/task? node))
                                      (not (ast-utils/call? node))
                                      (not (ast-utils/number-literal? node)))
                               (assoc node :__foo__ 5)
                               node))
                  (without-internal-ids
                    (ast/program-node
                     :scripts [(ast/task-node
                                :name "EMPTY"
                                :tick-rate (ast/ticking-rate-node 1 "s")
                                :state "running"
                                :body (ast/block-node
                                       [(ast/call-node
                                         "TOGGLE"
                                         [(ast/arg-node
                                           (ast/literal-number-node 14))])
                                        (ast/call-node
                                         "TURNON"
                                         [(ast/arg-node
                                           (ast/literal-number-node 13))])]))])))
        actual (ast-utils/transform
                 original
                 "UziTaskNode"
                 (fn [node _] (assoc node :name "EMPTY"))
                 "UziCallNode"
                 (fn [node _] (update node :selector str/upper-case))
                 "UziNumberLiteralNode"
                 (fn [node _] (update node :value inc))
                 :default
                 (fn [node _] (assoc node :__foo__ 5)))]
    (is (= expected actual))))

(deftest
  ast-transform-without-default-clause
  (let [original (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "empty"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])
                                       (ast/call-node
                                        "turnOn"
                                        [(ast/arg-node
                                          (ast/literal-number-node 12))])]))]))
        expected (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "EMPTY"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "TOGGLE"
                                        [(ast/arg-node
                                          (ast/literal-number-node 14))])
                                       (ast/call-node
                                        "TURNON"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])]))]))
        actual (ast-utils/transform
                 original
                 "UziTaskNode"
                 (fn [node _] (update node :name str/upper-case))
                 "UziCallNode"
                 (fn [node _] (update node :selector str/upper-case))
                 "UziNumberLiteralNode"
                 (fn [node _] (update node :value inc)))]
    (is (= expected actual))))

(deftest
  ast-transform-pred-test
  (let [original (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "empty"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])
                                       (ast/call-node
                                        "turnOn"
                                        [(ast/arg-node
                                          (ast/literal-number-node 12))])]))]))
        expected (w/prewalk
                  (fn [node] (if (and (map? node)
                                      (not (ast-utils/task? node))
                                      (not (ast-utils/call? node))
                                      (not (ast-utils/number-literal? node)))
                               (assoc node :__foo__ 5)
                               node))
                  (without-internal-ids
                    (ast/program-node
                     :scripts [(ast/task-node
                                :name "EMPTY"
                                :tick-rate (ast/ticking-rate-node 1 "s")
                                :state "running"
                                :body (ast/block-node
                                       [(ast/call-node
                                         "TOGGLE"
                                         [(ast/arg-node
                                           (ast/literal-number-node 14))])
                                        (ast/call-node
                                         "TURNON"
                                         [(ast/arg-node
                                           (ast/literal-number-node 13))])]))])))
        actual (ast-utils/transformp
                 original

                 (fn [node _] (ast-utils/task? node))
                 (fn [node _] (update node :name str/upper-case))

                 (fn [node _] (ast-utils/call? node))
                 (fn [node _] (update node :selector str/upper-case))

                 (fn [node _] (ast-utils/number-literal? node))
                 (fn [node _] (update node :value inc))

                 :default
                 (fn [node _] (assoc node :__foo__ 5)))]
    (is (= expected actual))))

(deftest
  ast-transform-pred-without-default-clause
  (let [original (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "empty"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "toggle"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])
                                       (ast/call-node
                                        "turnOn"
                                        [(ast/arg-node
                                          (ast/literal-number-node 12))])]))]))
        expected (without-internal-ids
                   (ast/program-node
                    :scripts [(ast/task-node
                               :name "EMPTY"
                               :tick-rate (ast/ticking-rate-node 1 "s")
                               :state "running"
                               :body (ast/block-node
                                      [(ast/call-node
                                        "TOGGLE"
                                        [(ast/arg-node
                                          (ast/literal-number-node 14))])
                                       (ast/call-node
                                        "TURNON"
                                        [(ast/arg-node
                                          (ast/literal-number-node 13))])]))]))
        actual (ast-utils/transformp
                 original

                 (fn [node _] (ast-utils/task? node))
                 (fn [node _] (update node :name str/upper-case))

                 (fn [node _] (ast-utils/call? node))
                 (fn [node _] (update node :selector str/upper-case))

                 (fn [node _] (ast-utils/number-literal? node))
                 (fn [node _] (update node :value inc)))]
    (is (= expected actual))))

(deftest
  program-with-local-variable-whose-value-is-a-compile-time-constant
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 0))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "a"))])
                                   (ast/variable-declaration-node
                                     "b"
                                     (ast/literal-number-node 0))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "b"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "a#1" 0) (emit/variable "b#2" 0)]
                               :instructions [(emit/read-local "a#1")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "b#2")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-local-variable-whose-value-is-a-compile-time-constant-different-than-zero
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 1))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "a"))])
                                   (ast/variable-declaration-node
                                     "b"
                                     (ast/literal-number-node 2))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "b"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 2)
                              (emit/constant 1)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "a#1" 1) (emit/variable "b#2" 2)]
                               :instructions [(emit/read-local "a#1")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "b#2")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-equal-nodes-referencing-different-variables
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "b")]
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 1))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "b"))])
                                   (ast/variable-declaration-node
                                     "b"
                                     (ast/literal-number-node 2))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "b"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 2)
                              (emit/constant 1) (emit/variable "b" 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "a#1" 1) (emit/variable "b#2" 2)]
                               :instructions [(emit/read-global "b")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "b#2")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-assignment-to-global
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "temp")]
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "temp")
                                     (ast/literal-number-node 0))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/variable "temp" 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 0)
                                              (emit/write-global "temp")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-assignment-to-local
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node
                                     "temp"
                                     (ast/literal-number-node 0))
                                   (ast/assignment-node
                                     (ast/variable-node "temp")
                                     (ast/literal-number-node 0))]))])
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "temp#1" 0)]
                               :instructions [(emit/push-value 0)
                                              (emit/write-local "temp#1")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-procedure
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node [(ast/call-node "default1" [])]))
                        (ast/procedure-node
                          :name "default1"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/script-call "default1")
                                              (emit/prim-call "pop")])
                             (emit/script :name "default1" :delay 0)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-procedure-call-before-the-end-of-block
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node "default1" [])
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))
                        (ast/procedure-node
                          :name "default1"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/script-call "default1")
                                              (emit/prim-call "pop")
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])
                             (emit/script :name "default1" :delay 0)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  procedure-with-one-argument
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "default1"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))
                        (ast/procedure-node
                          :name "default1"
                          :arguments [(ast/variable-declaration-node "arg0")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "arg0"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/script-call "default1")
                                              (emit/prim-call "pop")])
                             (emit/script
                               :name "default1"
                               :arguments [(emit/variable "arg0#1" 0)]
                               :delay 0
                               :instructions [(emit/read-local "arg0#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-function
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "temp")]
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "temp")
                                     (ast/call-node "default1" []))]))
                        (ast/function-node
                          :name "default1"
                          :body (ast/block-node []))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/variable "temp" 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/script-call "default1")
                                              (emit/write-global "temp")])
                             (emit/script :name "default1" :delay 0)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  function-with-one-arg
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "temp")]
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "temp")
                                     (ast/call-node
                                       "default1"
                                       [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))]))]))
                        (ast/function-node
                          :name "default1"
                          :arguments [(ast/variable-declaration-node "arg0")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "arg0"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)
                              (emit/variable "temp" 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/script-call "default1")
                                              (emit/write-global "temp")])
                             (emit/script
                               :name "default1"
                               :arguments [(emit/variable "arg0#1" 0)]
                               :delay 0
                               :instructions [(emit/read-local "arg0#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  the-order-of-the-keys-in-a-program-map-should-not-matter
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "counter")]
              :scripts [(ast/task-node
                          :name "empty"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "counter")
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "counter"))
                                        (ast/arg-node
                                          (ast/literal-number-node 1))]))]))])
        expected (emit/program
                   :globals #{(emit/variable "counter" 0) (emit/constant 1000)
                              (emit/constant 1)}
                   :scripts [(emit/script
                               :name "empty"
                               :delay 1000
                               :running? true
                               :instructions [(emit/read-global "counter")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-global "counter")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  the-order-of-the-keys-in-a-procedure-map-should-not-matter
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "default1"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))
                        (ast/procedure-node
                          :name "default1"
                          :arguments [(ast/variable-declaration-node "arg0")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "arg0"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/script-call "default1")
                                              (emit/prim-call "pop")])
                             (emit/script
                               :name "default1"
                               :arguments [(emit/variable "arg0#1" 0)]
                               :delay 0
                               :instructions [(emit/read-local "arg0#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  the-order-of-the-keys-in-a-function-map-should-not-matter
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node "temp")]
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "temp")
                                     (ast/call-node
                                       "default1"
                                       [(ast/arg-node
                                          (ast/literal-pin-node "D" 13))]))]))
                        (ast/function-node
                          :name "default1"
                          :arguments [(ast/variable-declaration-node "arg0")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "arg0"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)
                              (emit/variable "temp" 0)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/script-call "default1")
                                              (emit/write-global "temp")])
                             (emit/script
                               :name "default1"
                               :arguments [(emit/variable "arg0#1" 0)]
                               :delay 0
                               :instructions [(emit/read-local "arg0#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  return-without-value
  (let [ast (ast/program-node
              :scripts [(ast/procedure-node
                          :name "default"
                          :body (ast/block-node [(ast/return-node)]))
                        (ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node [(ast/call-node "default" [])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :instructions [(emit/prim-call "ret")])
                             (emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :instructions [(emit/script-call "default")
                                              (emit/prim-call "pop")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  return-with-value
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "addition"
                          :arguments [(ast/variable-declaration-node "x")
                                      (ast/variable-declaration-node "y")]
                          :body (ast/block-node
                                  [(ast/return-node
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "x"))
                                        (ast/arg-node (ast/variable-node "y"))]))]))
                        (ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/literal-pin-node "D" 13))
                                      (ast/arg-node
                                        (ast/call-node
                                          "addition"
                                          [(ast/arg-node
                                             (ast/literal-number-node 0.25))
                                           (ast/arg-node
                                             (ast/literal-number-node 0.75))]))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 0.75)
                              (emit/constant 1000) (emit/constant 0.25)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "addition"
                               :arguments [(emit/variable "x#1" 0) (emit/variable "y#2" 0)]
                               :delay 0
                               :instructions [(emit/read-local "x#1")
                                              (emit/read-local "y#2")
                                              (emit/prim-call "add")
                                              (emit/prim-call "retv")])
                             (emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 13)
                                              (emit/push-value 0.25)
                                              (emit/push-value 0.75)
                                              (emit/script-call "addition")
                                              (emit/prim-call "write")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  task-control-nodes
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "toggle"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))
                        (ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/start-node ["loop"])
                                   (ast/stop-node ["loop"])
                                   (ast/resume-node ["loop"])
                                   (ast/pause-node ["loop"])
                                   (ast/call-node "loop" [])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 13) (emit/prim-call "toggle")])
                             (emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/start "loop")
                                              (emit/stop "loop")
                                              (emit/resume "loop")
                                              (emit/pause "loop")
                                              (emit/script-call "loop")
                                              (emit/prim-call "pop")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  script-overriding-primitive
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "default"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "turnOn"
                                     [(ast/arg-node (ast/literal-number-node 0))])]))
                        (ast/procedure-node
                          :name "turnOn"
                          :arguments [(ast/variable-declaration-node "pin")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/variable-node "pin"))
                                      (ast/arg-node
                                        (ast/literal-number-node 1))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)}
                   :scripts [(emit/script
                               :name "default"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 0)
                                              (emit/script-call "turnOn")
                                              (emit/prim-call "pop")])
                             (emit/script
                               :name "turnOn"
                               :arguments [(emit/variable "pin#1" 0)]
                               :delay 0
                               :instructions [(emit/read-local "pin#1")
                                              (emit/push-value 1)
                                              (emit/prim-call "write")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  program-with-yield-statement
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "turnOn"
                                     [(ast/arg-node (ast/literal-pin-node "D" 13))])
                                   (ast/yield-node)
                                   (ast/call-node
                                     "turnOff"
                                     [(ast/arg-node (ast/literal-pin-node "D" 13))])
                                   (ast/yield-node)]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "turnOn")
                                              (emit/prim-call "yield")
                                              (emit/push-value 13)
                                              (emit/prim-call "turnOff")
                                              (emit/prim-call "yield")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  script-call-with-keyword-arguments
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "foo"
                                     [(ast/arg-node "a" (ast/literal-pin-node "D" 9))
                                      (ast/arg-node "c" (ast/literal-number-node 0.5))
                                      (ast/arg-node
                                        "b"
                                        (ast/literal-number-node 0.75))])]))
                        (ast/procedure-node
                          :name "foo"
                          :arguments [(ast/variable-declaration-node "a")
                                      (ast/variable-declaration-node "b")
                                      (ast/variable-declaration-node "c")]
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/variable-node "a"))
                                      (ast/arg-node
                                        (ast/call-node
                                          "+"
                                          [(ast/arg-node (ast/variable-node "b"))
                                           (ast/arg-node
                                             (ast/variable-node "c"))]))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 0.75)
                              (emit/constant 9) (emit/constant 0.5)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :instructions [(emit/push-value 9)
                                              (emit/push-value 0.75)
                                              (emit/push-value 0.5)
                                              (emit/script-call "foo")
                                              (emit/prim-call "pop")])
                             (emit/script
                               :name "foo"
                               :arguments [(emit/variable "a#1" 0)
                                           (emit/variable "b#2" 0)
                                           (emit/variable "c#3" 0)]
                               :delay 0
                               :instructions [(emit/read-local "a#1")
                                              (emit/read-local "b#2")
                                              (emit/read-local "c#3")
                                              (emit/prim-call "add")
                                              (emit/prim-call "write")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  full-conditional
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 13))])
                                     (ast/block-node
                                       [(ast/call-node
                                          "turnOff"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node
                                       [(ast/call-node
                                          "turnOn"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 1000) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "isOn")
                                              (emit/jz 3)
                                              (emit/push-value 13)
                                              (emit/prim-call "turnOff")
                                              (emit/jmp 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "turnOn")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  conditional-with-only-true-branch
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 13))])
                                     (ast/block-node
                                       [(ast/call-node
                                          "turnOff"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 1000) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "isOn")
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "turnOff")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  conditional-with-only-false-branch
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 13))])
                                     (ast/block-node [])
                                     (ast/block-node
                                       [(ast/call-node
                                          "turnOn"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 1000) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "isOn")
                                              (emit/jnz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "turnOn")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  forever-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/forever-node
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/jmp -3)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  while-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/while-node
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 1)
                                              (emit/jz 3)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/jmp -5)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  until-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/until-node
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 1)
                                              (emit/jnz 3)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/jmp -5)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  do-while-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/do-while-node
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/push-value 1)
                                              (emit/jnz -4)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  do-until-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/do-until-node
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/push-value 1)
                                              (emit/jz -4)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  wait-while-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/while-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 9))])
                                     (ast/block-node []))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 9)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 9)
                                              (emit/prim-call "isOn")
                                              (emit/jnz -3)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  wait-until-loop
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/until-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 9))])
                                     (ast/block-node []))
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 9)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/push-value 9)
                                              (emit/prim-call "isOn")
                                              (emit/jz -3)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  for-with-constant-step-and-constant-counter
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/for-node
                                     "i"
                                     (ast/literal-number-node 1)
                                     (ast/literal-number-node 10)
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/variable-node "i"))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 10)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "i#1" 0)]
                               :instructions [(emit/push-value 1)
                                              (emit/write-local "i#1")
                                              (emit/read-local "i#1")
                                              (emit/push-value 10)
                                              (emit/prim-call "lessThanOrEquals")
                                              (emit/jz 7)
                                              (emit/read-local "i#1")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "i#1")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-local "i#1")
                                              (emit/jmp -11)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  for-with-constant-step
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "start"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 1))]))
                        (ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/for-node
                                     "i"
                                     (ast/call-node "start" [])
                                     (ast/literal-number-node 10)
                                     (ast/literal-number-node 1)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/variable-node "i"))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 10)}
                   :scripts [(emit/script
                               :name "start"
                               :delay 0
                               :instructions [(emit/push-value 1) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "i#1" 0)]
                               :instructions [(emit/script-call "start")
                                              (emit/write-local "i#1")
                                              (emit/read-local "i#1")
                                              (emit/push-value 10)
                                              (emit/prim-call "lessThanOrEquals")
                                              (emit/jz 7)
                                              (emit/read-local "i#1")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "i#1")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-local "i#1")
                                              (emit/jmp -11)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  for-with-constant-negative-step
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :state "running"
                          :body (ast/block-node
                                  [(ast/for-node
                                     "i"
                                     (ast/literal-number-node 100)
                                     (ast/literal-number-node 0)
                                     (ast/literal-number-node -10)
                                     (ast/block-node
                                       [(ast/call-node
                                          "write"
                                          [(ast/arg-node (ast/literal-pin-node "D" 9))
                                           (ast/arg-node
                                             (ast/call-node
                                               "/"
                                               [(ast/arg-node (ast/variable-node "i"))
                                                (ast/arg-node
                                                  (ast/literal-number-node
                                                    100))]))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 9)
                              (emit/constant 100) (emit/constant -10)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :locals [(emit/variable "i#1" 0)]
                               :instructions [(emit/push-value 100)
                                              (emit/write-local "i#1")
                                              (emit/read-local "i#1")
                                              (emit/push-value 0)
                                              (emit/prim-call "greaterThanOrEquals")
                                              (emit/jz 10)
                                              (emit/push-value 9)
                                              (emit/read-local "i#1")
                                              (emit/push-value 100)
                                              (emit/prim-call "divide")
                                              (emit/prim-call "write")
                                              (emit/read-local "i#1")
                                              (emit/push-value -10)
                                              (emit/prim-call "add")
                                              (emit/write-local "i#1")
                                              (emit/jmp -14)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  for-without-constant-step
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "step"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 1))]))
                        (ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/for-node
                                     "i"
                                     (ast/literal-number-node 1)
                                     (ast/literal-number-node 10)
                                     (ast/call-node "step" [])
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/variable-node "i"))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1)
                              (emit/constant 10)}
                   :scripts [(emit/script
                               :name "step"
                               :delay 0
                               :instructions [(emit/push-value 1) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "i#1" 0) (emit/variable "@1" 0)]
                               :instructions [(emit/push-value 1)
                                              (emit/write-local "i#1")
                                              (emit/read-local "i#1")
                                              (emit/push-value 10)
                                              (emit/script-call "step")
                                              (emit/write-local "@1")
                                              (emit/read-local "@1")
                                              (emit/push-value 0)
                                              (emit/jlte 2)
                                              (emit/prim-call "lessThanOrEquals")
                                              (emit/jmp 1)
                                              (emit/prim-call "greaterThanOrEquals")
                                              (emit/jz 7)
                                              (emit/read-local "i#1")
                                              (emit/prim-call "toggle")
                                              (emit/read-local "i#1")
                                              (emit/read-local "@1")
                                              (emit/prim-call "add")
                                              (emit/write-local "i#1")
                                              (emit/jmp -18)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  repeat-loop
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "step"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 100))]))
                        (ast/task-node
                          :name "main"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/repeat-node
                                     (ast/call-node "step" [])
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])
                                        (ast/call-node
                                          "delayMs"
                                          [(ast/arg-node
                                             (ast/literal-number-node 1000))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 100)
                              (emit/constant 1000) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "step"
                               :delay 0
                               :instructions [(emit/push-value 100) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "@1" 0)]
                               :instructions [(emit/push-value 0)
                                              (emit/write-local "@1")
                                              (emit/read-local "@1")
                                              (emit/script-call "step")
                                              (emit/prim-call "lessThan")
                                              (emit/jz 9)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/push-value 1000)
                                              (emit/prim-call "delayMs")
                                              (emit/read-local "@1")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-local "@1")
                                              (emit/jmp -13)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  repeat-loop-declares-0-and-1-as-global-constants
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/repeat-node
                                     (ast/literal-number-node 5)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)
                              (emit/constant 5)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :locals [(emit/variable "@1" 0)]
                               :instructions [(emit/push-value 0)
                                              (emit/write-local "@1")
                                              (emit/read-local "@1")
                                              (emit/push-value 5)
                                              (emit/prim-call "lessThan")
                                              (emit/jz 7)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/read-local "@1")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-local "@1")
                                              (emit/jmp -11)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  for-loop-declares-0-as-global-constant
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/for-node
                                     "i"
                                     (ast/literal-number-node 1)
                                     (ast/literal-number-node 10)
                                     (ast/literal-number-node 2)
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 2)
                              (emit/constant 1000) (emit/constant 1)
                              (emit/constant 13) (emit/constant 10)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :locals [(emit/variable "i#1" 0)]
                               :instructions [(emit/push-value 1)
                                              (emit/write-local "i#1")
                                              (emit/read-local "i#1")
                                              (emit/push-value 10)
                                              (emit/prim-call "lessThanOrEquals")
                                              (emit/jz 7)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")
                                              (emit/read-local "i#1")
                                              (emit/push-value 2)
                                              (emit/prim-call "add")
                                              (emit/write-local "i#1")
                                              (emit/jmp -11)])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  local-values-are-registered-as-global-constants
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/variable-declaration-node "a")
                                   (ast/call-node
                                     "toggle"
                                     [(ast/arg-node (ast/variable-node "a"))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :locals [(emit/variable "a#1" 0)]
                               :instructions [(emit/read-local "a#1")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-and-without-short-circuit
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-and-node
                                       (ast/literal-number-node 1)
                                       (ast/literal-number-node 0))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/push-value 0)
                                              (emit/prim-call "logicalAnd")
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-and-with-short-circuit
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "foo"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 42))]))
                        (ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-and-node
                                       (ast/literal-number-node 1)
                                       (ast/call-node "foo" []))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)
                              (emit/constant 42)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 0
                               :instructions [(emit/push-value 42) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/jz 2)
                                              (emit/script-call "foo")
                                              (emit/jmp 1)
                                              (emit/push-value 0)
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-or-without-short-circuit
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-or-node
                                       (ast/literal-number-node 1)
                                       (ast/literal-number-node 0))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/push-value 0)
                                              (emit/prim-call "logicalOr")
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-or-with-short-circuit
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "foo"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 42))]))
                        (ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-or-node
                                       (ast/literal-number-node 1)
                                       (ast/call-node "foo" []))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)
                              (emit/constant 42)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 0
                               :instructions [(emit/push-value 42) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/jnz 2)
                                              (emit/script-call "foo")
                                              (emit/jmp 1)
                                              (emit/push-value 1)
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-and-with-short-circuit-declares-0-as-global-constant
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "foo"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "stopped"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 42))]))
                        (ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-and-node
                                       (ast/literal-number-node 2)
                                       (ast/call-node "foo" []))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 2)
                              (emit/constant 1000) (emit/constant 13)
                              (emit/constant 42)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 1000
                               :instructions [(emit/push-value 42) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 2)
                                              (emit/jz 2)
                                              (emit/script-call "foo")
                                              (emit/jmp 1)
                                              (emit/push-value 0)
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  logical-or-with-short-circuit-declares-1-as-global-constant
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "foo"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-number-node 42))]))
                        (ast/task-node
                          :name "main"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-or-node
                                       (ast/literal-number-node 2)
                                       (ast/call-node "foo" []))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 2)
                              (emit/constant 1000) (emit/constant 1)
                              (emit/constant 13) (emit/constant 42)}
                   :scripts [(emit/script
                               :name "foo"
                               :delay 0
                               :instructions [(emit/push-value 42) (emit/prim-call "retv")])
                             (emit/script
                               :name "main"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 2)
                                              (emit/jnz 2)
                                              (emit/script-call "foo")
                                              (emit/jmp 1)
                                              (emit/push-value 1)
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  has-side-effects-checks-the-arguments-of-a-call
  (let [ast (ast/program-node
              :scripts [(ast/function-node
                          :name "pin13"
                          :body (ast/block-node
                                  [(ast/return-node (ast/literal-pin-node "D" 13))]))
                        (ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-and-node
                                       (ast/literal-number-node 1)
                                       (ast/call-node
                                         "isOn"
                                         [(ast/arg-node (ast/call-node "pin13" []))]))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/call-node "pin13" []))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 1) (emit/constant 13)}
                   :scripts [(emit/script
                               :name "pin13"
                               :delay 0
                               :instructions [(emit/push-value 13) (emit/prim-call "retv")])
                             (emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/jz 3)
                                              (emit/script-call "pin13")
                                              (emit/prim-call "isOn")
                                              (emit/jmp 1)
                                              (emit/push-value 0)
                                              (emit/jz 2)
                                              (emit/script-call "pin13")
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  has-side-effects-checks-the-arguments-of-a-call-2
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/logical-and-node
                                       (ast/literal-number-node 1)
                                       (ast/call-node
                                         "isOn"
                                         [(ast/arg-node
                                            (ast/literal-pin-node "D" 13))]))
                                     (ast/block-node
                                       [(ast/call-node
                                          "toggle"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))])])
                                     (ast/block-node []))]))])
        expected (emit/program
                   :globals #{(emit/constant 1000) (emit/constant 1)
                              (emit/constant 13)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :instructions [(emit/push-value 1)
                                              (emit/push-value 13)
                                              (emit/prim-call "isOn")
                                              (emit/prim-call "logicalAnd")
                                              (emit/jz 2)
                                              (emit/push-value 13)
                                              (emit/prim-call "toggle")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  local-variable-declared-multiple-times-inside-script
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node
                          "a"
                          (ast/literal-number-node 42))]
              :scripts [(ast/task-node
                          :name "loop"
                          :tick-rate (ast/ticking-rate-node 1 "s")
                          :state "running"
                          :body (ast/block-node
                                  [(ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/literal-pin-node "D" 12))
                                      (ast/arg-node (ast/variable-node "a"))])
                                   (ast/conditional-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-number-node 0))])
                                     (ast/block-node
                                       [(ast/variable-declaration-node
                                          "a"
                                          (ast/literal-number-node -10))
                                        (ast/call-node
                                          "write"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))
                                           (ast/arg-node (ast/variable-node "a"))])])
                                     (ast/block-node
                                       [(ast/variable-declaration-node
                                          "a"
                                          (ast/literal-number-node 10))
                                        (ast/call-node
                                          "write"
                                          [(ast/arg-node
                                             (ast/literal-pin-node "D" 13))
                                           (ast/arg-node (ast/variable-node "a"))])]))
                                   (ast/call-node
                                     "write"
                                     [(ast/arg-node (ast/literal-pin-node "D" 12))
                                      (ast/arg-node
                                        (ast/call-node
                                          "*"
                                          [(ast/arg-node (ast/literal-number-node -1))
                                           (ast/arg-node
                                             (ast/variable-node "a"))]))])]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 1000)
                              (emit/constant 13) (emit/constant 10)
                              (emit/variable "a" 42) (emit/constant 12)
                              (emit/constant -10) (emit/constant -1)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 1000
                               :running? true
                               :locals [(emit/variable "a#1" -10)
                                        (emit/variable "a#2" 10)]
                               :instructions [(emit/push-value 12)
                                              (emit/read-global "a")
                                              (emit/prim-call "write")
                                              (emit/push-value 0)
                                              (emit/prim-call "isOn")
                                              (emit/jz 4)
                                              (emit/push-value 13)
                                              (emit/read-local "a#1")
                                              (emit/prim-call "write")
                                              (emit/jmp 3)
                                              (emit/push-value 13)
                                              (emit/read-local "a#2")
                                              (emit/prim-call "write")
                                              (emit/push-value 12)
                                              (emit/push-value -1)
                                              (emit/read-global "a")
                                              (emit/prim-call "multiply")
                                              (emit/prim-call "write")])])
        actual (compile ast)]
    (is (equivalent? expected actual))
    (is (= (-> expected :globals count)
           (-> actual :globals count)))))

(deftest
  global-variable-with-value-different-than-default
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node
                          "a"
                          (ast/literal-number-node 42))]
              :scripts [(ast/task-node
                          :name "loop"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "a"))
                                        (ast/arg-node
                                          (ast/literal-number-node 10))]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 10)
                              (emit/variable "a" 42)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 0
                               :running? true
                               :once? true
                               :instructions [(emit/read-global "a")
                                              (emit/push-value 10)
                                              (emit/prim-call "add")
                                              (emit/write-global "a")])])
        actual (compile ast)]
    (is (equivalent? expected actual))
    (is (= (-> expected :globals count)
           (-> actual :globals count)))))

(deftest
  local-variable-shadowing-global-variable
  (let [ast (ast/program-node
              :globals [(ast/variable-declaration-node
                          "a"
                          (ast/literal-number-node 42))]
              :scripts [(ast/task-node
                          :name "loop"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "a"))
                                        (ast/arg-node (ast/literal-number-node 1))]))
                                   (ast/variable-declaration-node
                                     "a"
                                     (ast/literal-number-node 15))
                                   (ast/assignment-node
                                     (ast/variable-node "a")
                                     (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/variable-node "a"))
                                        (ast/arg-node
                                          (ast/literal-number-node 42))]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 15)
                              (emit/constant 1) (emit/variable "a" 42)
                              (emit/constant 42)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "a#1" 15)]
                               :instructions [(emit/read-global "a")
                                              (emit/push-value 1)
                                              (emit/prim-call "add")
                                              (emit/write-global "a")
                                              (emit/read-local "a#1")
                                              (emit/push-value 42)
                                              (emit/prim-call "add")
                                              (emit/write-local "a#1")])])
        actual (compile ast)]
    (is (equivalent? expected actual))
    (is (= (-> expected :globals count)
           (-> actual :globals count)))))

(deftest
  conditional-children-order-should-not-impact-compilation
  (let [ast (ast/program-node
              :scripts [(ast/task-node
                          :name "loop"
                          :state "once"
                          :body (ast/block-node
                                  [(ast/conditional-node
                                     (ast/call-node
                                       "isOn"
                                       [(ast/arg-node (ast/literal-pin-node "D" 9))])
                                     (ast/block-node
                                       [(ast/variable-declaration-node
                                          "a"
                                          (ast/literal-number-node 11))
                                        (ast/assignment-node
                                          (ast/variable-node "a")
                                          (ast/call-node
                                            "+"
                                            [(ast/arg-node (ast/variable-node "a"))
                                             (ast/arg-node
                                               (ast/literal-number-node 10))]))])
                                     (ast/block-node
                                       [(ast/variable-declaration-node
                                          "a"
                                          (ast/literal-number-node 21))
                                        (ast/assignment-node
                                          (ast/variable-node "a")
                                          (ast/call-node
                                            "+"
                                            [(ast/arg-node (ast/variable-node "a"))
                                             (ast/arg-node
                                               (ast/literal-number-node
                                                 20))]))]))]))])
        expected (emit/program
                   :globals #{(emit/constant 0) (emit/constant 9)
                              (emit/constant 11) (emit/constant 20)
                              (emit/constant 10) (emit/constant 21)}
                   :scripts [(emit/script
                               :name "loop"
                               :delay 0
                               :running? true
                               :once? true
                               :locals [(emit/variable "a#1" 11)
                                        (emit/variable "a#2" 21)]
                               :instructions [(emit/push-value 9)
                                              (emit/prim-call "isOn")
                                              (emit/jz 5)
                                              (emit/read-local "a#1")
                                              (emit/push-value 10)
                                              (emit/prim-call "add")
                                              (emit/write-local "a#1")
                                              (emit/jmp 4)
                                              (emit/read-local "a#2")
                                              (emit/push-value 20)
                                              (emit/prim-call "add")
                                              (emit/write-local "a#2")])])
        actual (compile ast)]
    (is (equivalent? expected actual))
    (is (= (-> expected :globals count)
           (-> actual :globals count)))))

(deftest return-with-logical-and-as-value
  (let [ast (ast/program-node
             :scripts [(ast/function-node
                       :name "foo"
                       :body (ast/block-node
                              [(ast/return-node
                                (ast/logical-and-node
                                 (ast/literal-number-node 1)
                                 (ast/literal-number-node 2)))]))
                       (ast/task-node
                        :name "loop"
                        :state "once"
                        :body (ast/block-node
                               [(ast/call-node "foo" [])]))])
        expected (emit/program
                  :globals #{(emit/constant 0)
                             (emit/constant 2)
                             (emit/constant 1)}
                  :scripts [(emit/script
                             :name "foo"
                             :instructions [(emit/push-value 1)
                                            (emit/push-value 2)
                                            (emit/prim-call "logicalAnd")
                                            (emit/prim-call "retv")])
                            (emit/script
                             :name "loop"
                             :once? true
                             :running? true
                             :instructions [(emit/script-call "foo")
                                            (emit/prim-call "pop")])])
        actual (compile ast)]
    (is (equivalent? expected actual))))
