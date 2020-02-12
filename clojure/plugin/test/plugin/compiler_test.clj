(ns plugin.compiler-test
  (:require [clojure.test :refer :all]
            [plugin.compiler.core :as compiler :refer [compile-tree]])
  (:use [plugin.test-utils]))

(deftest empty-program-test
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "empty",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :value 1,
                                      :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements []}}]}
        expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 1000},
                             :instructions [],
                             :locals [],
                             :name "empty",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                                :value 1000}}}
        actual (compile-tree ast)]
    (is (equivalent? expected actual))))


(deftest program-with-global-variable-test
  (let [ast {:__class__ "UziProgramNode",
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "counter"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "empty",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :value 1,
                                      :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode",
                                                    :name "counter"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "+",
                                                     :arguments [{:__class__ "Association",
                                                                  :value {:__class__ "UziVariableNode",
                                                                          :name "counter"}},
                                                                 {:__class__ "Association",
                                                                  :value {:__class__ "UziNumberLiteralNode",
                                                                          :value 1}}]}}]}}],
             :primitives []}
        expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 1000},
                             :instructions [{:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :name "counter",
                                                        ;:value 0
                                                        }},
                                            {:__class__ "UziPushInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :value 1}},
                                            {:__class__ "UziPrimitiveCallInstruction",
                                             :argument {:__class__ "UziPrimitive",
                                                        ;:code 6,
                                                        :name "add",
                                                        ;:stackTransition {:__class__ "Association",
                                                        ;                  :key 2,
                                                        ;                  :value 1}
                                                        }},
                                            {:__class__ "UziPopInstruction",
                                             :argument {:__class__ "UziVariable",
                                                        :name "counter",
                                                        ;:value 0
                                                        }}],
                             :locals [],
                             :name "empty",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                                :name "counter",
                                :value 0},
                               {:__class__ "UziVariable",
                                :value 1000},
                               {:__class__ "UziVariable",
                                :value 1}}}
        actual (compile-tree ast)]
    (is (equivalent? expected actual))))

(deftest task-without-ticking-rate
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",,
                        :name "foo",
                        :arguments [],
                        :state "running",
                        :body {:__class__ "UziBlockNode",
                               :statements []}}],
             :primitives []}
        expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [],
                             :locals [],
                             :name "foo",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                               :value 0}}}
        actual (compile-tree ast)]
    (is (equivalent? expected actual))))

(deftest task-with-once
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",,
                        :name "foo",
                        :arguments [],
                        :state "once",
                        :body {:__class__ "UziBlockNode",
                               :statements []}}],
             :primitives []}
        expected {:__class__ "UziProgram",
                  :scripts [{:__class__ "UziScript",
                             :arguments [],
                             :delay {:__class__ "UziVariable",
                                     :value 0},
                             :instructions [{:__class__ "UziStopScriptInstruction",
                                             :argument "foo"}],
                             :locals [],
                             :name "foo",
                             :ticking true}],
                  :variables #{{:__class__ "UziVariable",
                                :value 0}}}
        actual (compile-tree ast)]
    (is (equivalent? expected actual))))

; TODO(RICHO): Uncomment this test and make it pass!
#_(deftest program-with-local-variable
    (let [ast {:__class__ "UziProgramNode",
               :scripts [{:__class__ "UziTaskNode",
                          :name "foo",
                          :arguments [],
                          :state "once",
                          :tickingRate nil,
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziVariableDeclarationNode",
                                               :name "pin",
                                               :value {:__class__ "UziPinLiteralNode",
                                                       :type "D",
                                                       :number 13}},
                                              {:__class__ "UziCallNode",
                                               :selector "toggle",
                                               :arguments [{:__class__ "Association",
                                                            :key nil,
                                                            :value {:__class__ "UziVariableNode",
                                                                    :name "pin"}}]}]}}]}
          expected {:__class__ "UziProgram",
                    :scripts [{:__class__ "UziScript",
                               :name "foo",
                               :ticking true,
                               :delay {:__class__ "UziVariable",
                                       :value 0},
                               :locals [{:__class__ "UziVariable",
                                         :name "pin#1",
                                         :value 0}],
                               :instructions [{:__class__ "UziPushInstruction",
                                               :argument {:__class__ "UziVariable",
                                                          :value 13}},
                                              {:__class__ "UziWriteLocalInstruction",
                                               :argument {:__class__ "UziVariable",
                                                          :name "pin#1",
                                                          :value 0}},
                                              {:__class__ "UziReadLocalInstruction",
                                               :argument {:__class__ "UziVariable",
                                                          :name "pin#1",
                                                          :value 0}},
                                              {:__class__ "UziPrimitiveCallInstruction",
                                               :argument {:__class__ "UziPrimitive",
                                                          ;:code 2,
                                                          :name "toggle",
                                                          ;:stackTransition {:__class__ "Association",
                                                          ;                  :key 1,
                                                          ;                  :value 0}
                                                          }},
                                              {:__class__ "UziStopScriptInstruction",
                                               :argument "foo"}]}],
                    :variables #{{:__class__ "UziVariable",
                                  :value 0},
                                 {:__class__ "UziVariable",
                                  :value 13}}}
          actual (compile-tree ast)]
      (is (equivalent? expected actual))))
