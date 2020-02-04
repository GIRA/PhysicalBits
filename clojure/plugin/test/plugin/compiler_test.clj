(ns plugin.compiler-test
  (:require [clojure.test :refer :all]
            [plugin.compiler :as compiler :refer [compile-tree]])
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
