(ns plugin.dead-code-remover-test
  (:require [clojure.test :refer :all]
            [clojure.walk :as w]
            [plugin.compiler.ast-utils :as ast-utils]
            [plugin.compiler.core :as cc])
  (:use [plugin.test-utils]))

(defn compile [src]
  (cc/compile-uzi-string src))

(deftest stopped-task-with-no-refs-should-be-removed
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "alive1",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 11}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziStopScriptInstruction",
                                             :argument "alive2"}],
                             :locals [],
                             :name "alive2",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 11}}}
        actual (compile "
                  task alive1() running { toggle(D13); }
	                task dead() stopped { toggle(D12); }
                  task alive2() { toggle(D11); }")]
    (is (= expected actual))))

(deftest start-task-should-mark-the-script-as-alive
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziStartScriptInstruction",
                                             :argument "dead"}],
                             :locals [],
                             :name "alive",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "dead",
                             :ticking false}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
                		task alive() running { toggle(D13); start dead; }
                		task dead() stopped { toggle(D12); }
                		task reallyDead() stopped { toggle(D11); }")]
    (is (= expected actual))))

(deftest stop-task-should-mark-the-script-as-alive
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziStopScriptInstruction",
                                             :argument "dead"}],
                             :locals [],
                             :name "alive",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "dead",
                             :ticking false}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
              		task alive() running { toggle(D13); stop dead; }
              		task dead() stopped { toggle(D12); }
              		task reallyDead() stopped { toggle(D11); }")]
    (is (= expected actual))))

(deftest pause-task-should-mark-the-script-as-alive
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziPauseScriptInstruction",
                                             :argument "dead"}],
                             :locals [],
                             :name "alive",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "dead",
                             :ticking false}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
              		task alive() running { toggle(D13); pause dead; }
              		task dead() stopped { toggle(D12); }
              		task reallyDead() stopped { toggle(D11); }")]
    (is (= expected actual))))

(deftest resume-task-should-mark-the-script-as-alive
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziResumeScriptInstruction",
                                             :argument "dead"}],
                             :locals [],
                             :name "alive",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "dead",
                             :ticking false}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
              		task alive() running { toggle(D13); resume dead; }
              		task dead() stopped { toggle(D12); }
              		task reallyDead() stopped { toggle(D11); }")]
    (is (= expected actual))))

(deftest the-visit-order-should-not-matter
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "dead",
                             :ticking false},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziStartScriptInstruction",
                                             :argument "dead"}],
                             :locals [],
                             :name "alive",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
                  task dead() stopped { toggle(D12); }
              		task alive() running { toggle(D13); start dead; }
              		task reallyDead() stopped { toggle(D11); }")]
    (is (= expected actual))))


(deftest circular-refs-should-not-be-a-problem
  (let [expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 13}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}},
                                            {:__class__ "UziStartScriptInstruction",
                                             :argument "bar"}],
                             :locals [],
                             :name "foo",
                             :ticking true},
                            {:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziStartScriptInstruction",
                                             :argument "foo"},
                                            {:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 12}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        :name "toggle"}}],
                             :locals [],
                             :name "bar",
                             :ticking false}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 13},
                               {:__class__ "UziVariable",
                                :value 12}}}
        actual (compile "
                  task foo() running { toggle(D13); start bar; }
                  task bar() stopped { start foo; toggle(D12); }")]
    (is (= expected actual))))

(deftest stopped-script-that-starts-itself-should-not-count
  (let [expected {:__class__ "UziProgram", :scripts [], :variables #{}}
        actual (compile "task bar() stopped { start bar; toggle(D12); }")]
    (is (= expected actual))))
