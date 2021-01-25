(ns middleware.compiler-test
  (:refer-clojure :exclude [compile ])
  (:require [clojure.test :refer :all]
            [middleware.compiler.utils.ast :as ast-utils]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.emitter :as emit])
  (:use [middleware.test-utils ]))

(defn compile [ast]
  (register-program! ast)
  (:compiled (cc/compile-tree ast "")))

(deftest
  empty-program-test
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "empty",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode", :statements []}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "counter"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "empty",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "counter"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "+",
                                                     :arguments [{:__class__ "Association",
                                                                  :value {:__class__ "UziVariableNode", :name "counter"}}
                                                                 {:__class__ "Association",
                                                                  :value {:__class__ "UziNumberLiteralNode",
                                                                          :value 1}}]}}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "foo",
                        :arguments [],
                        :state "running",
                        :body {:__class__ "UziBlockNode", :statements []}}],
             :primitives []}
        expected (emit/program
                   :globals #{(emit/constant 0)}
                   :scripts [(emit/script :name "foo" :delay 0 :running? true)])
        actual (compile ast)]
    (is (equivalent? expected actual))))

(deftest
  task-with-once
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "foo",
                        :arguments [],
                        :state "once",
                        :body {:__class__ "UziBlockNode", :statements []}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "foo",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "pin",
                                             :value {:__class__ "UziCallNode",
                                                     :selector "+",
                                                     :arguments [{:__class__ "Association",
                                                                  :value {:__class__ "UziNumberLiteralNode", :value 1}}
                                                                 {:__class__ "Association",
                                                                  :value {:__class__ "UziPinLiteralNode",
                                                                          :type "D",
                                                                          :number 13}}]}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "pin"}}]}]}}]}
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
  (let [original {:__class__ "UziProgramNode",
                  :scripts [{:__class__ "UziTaskNode",
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 12}}]}]}}]}
        expected {:__class__ "UziProgramNode",
                  :__foo__ 1,
                  :scripts [{:__class__ "UziTaskNode",
                             :__foo__ 2,
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :__bar__ 5,
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :__bar__ 5,
                                    :statements [{:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :__bar__ 5,
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :__bar__ 5,
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 12}}]}]}}]}
        actual (ast-utils/transform
                 original
                 "UziProgramNode"
                 (fn [node _] (assoc node :__foo__ 1))
                 "UziTaskNode"
                 (fn [node _] (assoc node :__foo__ 2))
                 "UziCallNode"
                 (fn [node _] (assoc node :__foo__ 3))
                 "UziNumberLiteralNode"
                 (fn [node _] (assoc node :__foo__ 4))
                 :default
                 (fn [node _] (assoc node :__bar__ 5)))]
    (is (= expected actual))))

(deftest
  ast-transform-without-default-clause
  (let [original {:__class__ "UziProgramNode",
                  :scripts [{:__class__ "UziTaskNode",
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 12}}]}]}}]}
        expected {:__class__ "UziProgramNode",
                  :__foo__ 1,
                  :scripts [{:__class__ "UziTaskNode",
                             :__foo__ 2,
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 12}}]}]}}]}
        actual (ast-utils/transform
                 original
                 "UziProgramNode"
                 (fn [node _] (assoc node :__foo__ 1))
                 "UziTaskNode"
                 (fn [node _] (assoc node :__foo__ 2))
                 "UziCallNode"
                 (fn [node _] (assoc node :__foo__ 3))
                 "UziNumberLiteralNode"
                 (fn [node _] (assoc node :__foo__ 4)))]
    (is (= expected actual))))

(deftest
  ast-transform-pred-test
  (let [original {:__class__ "UziProgramNode",
                  :scripts [{:__class__ "UziTaskNode",
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 12}}]}]}}]}
        expected {:__class__ "UziProgramNode",
                  :__foo__ 1,
                  :scripts [{:__class__ "UziTaskNode",
                             :__foo__ 2,
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :__bar__ 5,
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :__bar__ 5,
                                    :statements [{:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :__bar__ 5,
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :__bar__ 5,
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 12}}]}]}}]}
        actual (ast-utils/transformp
                 original
                 (fn [node _]
                   (= "UziProgramNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 1))
                 (fn [node _] (= "UziTaskNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 2))
                 (fn [node _] (= "UziCallNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 3))
                 (fn [node _]
                   (= "UziNumberLiteralNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 4))
                 :default
                 (fn [node _] (assoc node :__bar__ 5)))]
    (is (= expected actual))))

(deftest
  ast-transform-pred-without-default-clause
  (let [original {:__class__ "UziProgramNode",
                  :scripts [{:__class__ "UziTaskNode",
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :value 12}}]}]}}]}
        expected {:__class__ "UziProgramNode",
                  :__foo__ 1,
                  :scripts [{:__class__ "UziTaskNode",
                             :__foo__ 2,
                             :name "empty",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 13}}]}
                                                 {:__class__ "UziCallNode",
                                                  :__foo__ 3,
                                                  :selector "turnOn",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                       :__foo__ 4,
                                                                       :value 12}}]}]}}]}
        actual (ast-utils/transformp
                 original
                 (fn [node _]
                   (= "UziProgramNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 1))
                 (fn [node _] (= "UziTaskNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 2))
                 (fn [node _] (= "UziCallNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 3))
                 (fn [node _]
                   (= "UziNumberLiteralNode" (get node :__class__)))
                 (fn [node _] (assoc node :__foo__ 4)))]
    (is (= expected actual))))

(deftest
  program-with-local-variable-whose-value-is-a-compile-time-constant
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "a",
                                             :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "a"}}]}
                                            {:__class__ "UziVariableDeclarationNode",
                                             :name "b",
                                             :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "b"}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "a",
                                             :value {:__class__ "UziNumberLiteralNode", :value 1}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "a"}}]}
                                            {:__class__ "UziVariableDeclarationNode",
                                             :name "b",
                                             :value {:__class__ "UziNumberLiteralNode", :value 2}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "b"}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [{:__class__ "UziVariableDeclarationNode", :name "b"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "a",
                                             :value {:__class__ "UziNumberLiteralNode", :value 1}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "b"}}]}
                                            {:__class__ "UziVariableDeclarationNode",
                                             :name "b",
                                             :value {:__class__ "UziNumberLiteralNode", :value 2}}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "b"}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [{:__class__ "UziVariableDeclarationNode", :name "temp"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "temp"},
                                             :right {:__class__ "UziNumberLiteralNode", :value 0}}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "temp",
                                             :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                            {:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "temp"},
                                             :right {:__class__ "UziNumberLiteralNode", :value 0}}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "default1",
                                             :arguments []}]}}
                       {:__class__ "UziProcedureNode",
                        :name "default1",
                        :arguments [],
                        :body {:__class__ "UziBlockNode", :statements []}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "default1",
                                             :arguments []}
                                            {:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :type "D",
                                                                  :number 13}}]}]}}
                       {:__class__ "UziProcedureNode",
                        :name "default1",
                        :arguments [],
                        :body {:__class__ "UziBlockNode", :statements []}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "default1",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :type "D",
                                                                  :number 13}}]}]}}
                       {:__class__ "UziProcedureNode",
                        :name "default1",
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "arg0"}],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode",
                                                                  :name "arg0"}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [{:__class__ "UziVariableDeclarationNode", :name "temp"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "temp"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "default1",
                                                     :arguments []}}]}}
                       {:__class__ "UziFunctionNode",
                        :name "default1",
                        :arguments [],
                        :body {:__class__ "UziBlockNode", :statements []}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [{:__class__ "UziVariableDeclarationNode", :name "temp"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "temp"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "default1",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziPinLiteralNode",
                                                                          :type "D",
                                                                          :number 13}}]}}]}}
                       {:__class__ "UziFunctionNode",
                        :name "default1",
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "arg0"}],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode",
                                                                  :name "arg0"}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :scripts [{:__class__ "UziTaskNode",
                        :name "empty",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "counter"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "+",
                                                     :arguments [{:__class__ "Association",
                                                                  :value {:__class__ "UziVariableNode", :name "counter"}}
                                                                 {:__class__ "Association",
                                                                  :value {:__class__ "UziNumberLiteralNode",
                                                                          :value 1}}]}}]}}],
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "counter"}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "default1",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :type "D",
                                                                  :number 13}}]}]}}
                       {:__class__ "UziProcedureNode",
                        :name "default1",
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "arg0"}}]}]},
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "arg0"}]}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [{:__class__ "UziVariableDeclarationNode", :name "temp"}],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "temp"},
                                             :right {:__class__ "UziCallNode",
                                                     :selector "default1",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziPinLiteralNode",
                                                                          :type "D",
                                                                          :number 13}}]}}]}}
                       {:__class__ "UziFunctionNode",
                        :name "default1",
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "arg0"}}]}]},
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "arg0"}]}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziProcedureNode",
                        :name "default",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode", :value nil}]}}
                       {:__class__ "UziTaskNode",
                        :name "loop",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "default",
                                             :arguments []}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziFunctionNode",
                        :name "addition",
                        :arguments [{:__class__ "UziVariableDeclarationNode", :name "x"}
                                    {:__class__ "UziVariableDeclarationNode", :name "y"}],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziCallNode",
                                                     :selector "+",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode", :name "x"}}
                                                                 {:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode", :name "y"}}]}}]}}
                       {:__class__ "UziTaskNode",
                        :name "loop",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "write",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :type "D",
                                                                  :number 13}}
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziCallNode",
                                                                  :selector "addition",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                                       :value 0.25}}
                                                                              {:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                                       :value 0.75}}]}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "loop",
                        :arguments [],
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"},
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "toggle",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :type "D",
                                                                  :number 13}}]}]}}
                       {:__class__ "UziTaskNode",
                        :name "main",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziScriptStartNode", :scripts ["loop"]}
                                            {:__class__ "UziScriptStopNode", :scripts ["loop"]}
                                            {:__class__ "UziScriptResumeNode", :scripts ["loop"]}
                                            {:__class__ "UziScriptPauseNode", :scripts ["loop"]}
                                            {:__class__ "UziCallNode",
                                             :selector "loop",
                                             :arguments []}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :imports [],
             :globals [],
             :scripts [{:__class__ "UziTaskNode",
                        :name "default",
                        :arguments [],
                        :state "once",
                        :tickingRate nil,
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "turnOn",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziNumberLiteralNode", :value 0}}]}]}}
                       {:__class__ "UziProcedureNode",
                        :name "turnOn",
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "pin"}],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :selector "write",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "pin"}}
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziNumberLiteralNode",
                                                                  :value 1}}]}]}}],
             :primitives []}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 13,
                                                                  :type "D"}}],
                                             :primitiveName "turnOn",
                                             :selector "turnOn"}
                                            {:__class__ "UziYieldNode"}
                                            {:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 13,
                                                                  :type "D"}}],
                                             :primitiveName "turnOff",
                                             :selector "turnOff"}
                                            {:__class__ "UziYieldNode"}]},
                        :name "main",
                        :state "running",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key "a",
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 9,
                                                                  :type "D"}}
                                                         {:__class__ "Association",
                                                          :key "c",
                                                          :value {:__class__ "UziNumberLiteralNode", :value 0.5}}
                                                         {:__class__ "Association",
                                                          :key "b",
                                                          :value {:__class__ "UziNumberLiteralNode", :value 0.75}}],
                                             :selector "foo"}]},
                        :name "main",
                        :state "running",
                        :tickingRate nil}
                       {:__class__ "UziProcedureNode",
                        :arguments [{:__class__ "UziVariableDeclarationNode",
                                     :name "a",
                                     :value nil}
                                    {:__class__ "UziVariableDeclarationNode",
                                     :name "b",
                                     :value nil}
                                    {:__class__ "UziVariableDeclarationNode",
                                     :name "c",
                                     :value nil}],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "a"}}
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "b"}}
                                                                              {:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "c"}}],
                                                                  :selector "+"}}],
                                             :selector "write"}]},
                        :name "foo",
                        :state nil,
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 13,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "turnOn"}]},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "turnOff"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 13,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "turnOff"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 13,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "turnOn"}]},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode", :statements []}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForeverNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziPinLiteralNode",
                                                                                       :number 13,
                                                                                       :type "D"}}],
                                                                  :selector "toggle"}]}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziWhileNode",
                                             :condition
                                                        {:__class__ "UziNumberLiteralNode", :value 1},
                                             :negated false,
                                             :post
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]},
                                             :pre {:__class__ "UziBlockNode", :statements []}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziUntilNode",
                                             :condition
                                                        {:__class__ "UziNumberLiteralNode", :value 1},
                                             :negated true,
                                             :post
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]},
                                             :pre {:__class__ "UziBlockNode", :statements []}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziDoWhileNode",
                                             :condition
                                                        {:__class__ "UziNumberLiteralNode", :value 1},
                                             :negated false,
                                             :post {:__class__ "UziBlockNode", :statements []},
                                             :pre
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziDoUntilNode",
                                             :condition
                                                        {:__class__ "UziNumberLiteralNode", :value 1},
                                             :negated true,
                                             :post {:__class__ "UziBlockNode", :statements []},
                                             :pre
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziWhileNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 9,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :negated false,
                                             :post {:__class__ "UziBlockNode", :statements []},
                                             :pre {:__class__ "UziBlockNode", :statements []}}
                                            {:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 13,
                                                                  :type "D"}}],
                                             :selector "toggle"}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziUntilNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 9,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :negated true,
                                             :post {:__class__ "UziBlockNode", :statements []},
                                             :pre {:__class__ "UziBlockNode", :statements []}}
                                            {:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 13,
                                                                  :type "D"}}],
                                             :selector "toggle"}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "i"}}],
                                                                  :selector "toggle"}]},
                                             :counter
                                                        {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value nil},
                                             :start {:__class__ "UziNumberLiteralNode", :value 1},
                                             :step {:__class__ "UziNumberLiteralNode", :value 1},
                                             :stop
                                                        {:__class__ "UziNumberLiteralNode", :value 10}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 1}}]},
                        :name "start",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "i"}}],
                                                                  :selector "toggle"}]},
                                             :counter
                                                        {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value nil},
                                             :start
                                                        {:__class__ "UziCallNode",
                                                         :arguments [],
                                                         :selector "start"},
                                             :step {:__class__ "UziNumberLiteralNode", :value 1},
                                             :stop
                                                        {:__class__ "UziNumberLiteralNode", :value 10}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziPinLiteralNode",
                                                                                       :number 9,
                                                                                       :type "D"}}
                                                                              {:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziCallNode",
                                                                                       :arguments [{:__class__ "Association",
                                                                                                    :key nil,
                                                                                                    :value {:__class__ "UziVariableNode", :name "i"}}
                                                                                                   {:__class__ "Association",
                                                                                                    :key nil,
                                                                                                    :value {:__class__ "UziNumberLiteralNode",
                                                                                                            :value 100}}],
                                                                                       :selector "/"}}],
                                                                  :selector "write"}]},
                                             :counter
                                                        {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value nil},
                                             :start
                                                        {:__class__ "UziNumberLiteralNode", :value 100},
                                             :step
                                                        {:__class__ "UziNumberLiteralNode", :value -10},
                                             :stop
                                                        {:__class__ "UziNumberLiteralNode", :value 0}}]},
                        :name "main",
                        :state "running",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 1}}]},
                        :name "step",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "i"}}],
                                                                  :selector "toggle"}]},
                                             :counter
                                                        {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value nil},
                                             :start {:__class__ "UziNumberLiteralNode", :value 1},
                                             :step
                                                        {:__class__ "UziCallNode",
                                                         :arguments [],
                                                         :selector "step"},
                                             :stop
                                                        {:__class__ "UziNumberLiteralNode", :value 10}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 100}}]},
                        :name "step",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziRepeatNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziPinLiteralNode",
                                                                                       :number 13,
                                                                                       :type "D"}}],
                                                                  :selector "toggle"}
                                                                 {:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziNumberLiteralNode",
                                                                                       :value 1000}}],
                                                                  :selector "delayMs"}]},
                                             :times
                                                        {:__class__ "UziCallNode",
                                                         :arguments [],
                                                         :selector "step"}}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziRepeatNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziPinLiteralNode",
                                                                                       :number 13,
                                                                                       :type "D"}}],
                                                                  :selector "toggle"}]},
                                             :times
                                                        {:__class__ "UziNumberLiteralNode", :value 5}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziForNode",
                                             :body {:__class__ "UziBlockNode",
                                                    :statements [{:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziPinLiteralNode",
                                                                                       :number 13,
                                                                                       :type "D"}}],
                                                                  :selector "toggle"}]},
                                             :counter
                                                        {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value nil},
                                             :start {:__class__ "UziNumberLiteralNode", :value 1},
                                             :step {:__class__ "UziNumberLiteralNode", :value 2},
                                             :stop
                                                        {:__class__ "UziNumberLiteralNode", :value 10}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziVariableDeclarationNode",
                                             :name "a",
                                             :value nil}
                                            {:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "a"}}],
                                             :selector "toggle"}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalAndNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziNumberLiteralNode", :value 0}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 42}}]},
                        :name "foo",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalAndNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [],
                                                                 :selector "foo"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalOrNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziNumberLiteralNode", :value 0}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 42}}]},
                        :name "foo",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalOrNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [],
                                                                 :selector "foo"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 42}}]},
                        :name "foo",
                        :state "stopped",
                        :tickingRate {:__class__ "UziTickingRateNode", :scale "s", :value 1}}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalAndNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 2},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [],
                                                                 :selector "foo"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziNumberLiteralNode", :value 42}}]},
                        :name "foo",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalOrNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 2},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [],
                                                                 :selector "foo"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "main",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziFunctionNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziReturnNode",
                                             :value {:__class__ "UziPinLiteralNode",
                                                     :number 13,
                                                     :type "D"}}]},
                        :name "pin13",
                        :state nil,
                        :tickingRate nil}
                       {:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalAndNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [{:__class__ "Association",
                                                                              :key nil,
                                                                              :value {:__class__ "UziCallNode",
                                                                                      :arguments [],
                                                                                      :selector "pin13"}}],
                                                                 :selector "isOn"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziCallNode",
                                                                                            :arguments [],
                                                                                            :selector "pin13"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "loop",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziLogicalAndNode",
                                                         :left {:__class__ "UziNumberLiteralNode", :value 1},
                                                         :right {:__class__ "UziCallNode",
                                                                 :arguments [{:__class__ "Association",
                                                                              :key nil,
                                                                              :value {:__class__ "UziPinLiteralNode",
                                                                                      :number 13,
                                                                                      :type "D"}}],
                                                                 :selector "isOn"}},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode", :statements []},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}],
                                                                       :selector "toggle"}]}}]},
                        :name "loop",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
  (let [ast {:__class__ "UziProgramNode",
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "a",
                        :value {:__class__ "UziNumberLiteralNode", :value 42}}],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 12,
                                                                  :type "D"}}
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziVariableNode", :name "a"}}],
                                             :selector "write"}
                                            {:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziNumberLiteralNode", :value 0}}],
                                                         :selector "isOn"},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziVariableDeclarationNode",
                                                                       :name "a",
                                                                       :value {:__class__ "UziNumberLiteralNode", :value -10}}
                                                                      {:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}
                                                                                   {:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziVariableNode", :name "a"}}],
                                                                       :selector "write"}]},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziVariableDeclarationNode",
                                                                       :name "a",
                                                                       :value {:__class__ "UziNumberLiteralNode", :value 10}}
                                                                      {:__class__ "UziCallNode",
                                                                       :arguments [{:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziPinLiteralNode",
                                                                                            :number 13,
                                                                                            :type "D"}}
                                                                                   {:__class__ "Association",
                                                                                    :key nil,
                                                                                    :value {:__class__ "UziVariableNode", :name "a"}}],
                                                                       :selector "write"}]}}
                                            {:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 12,
                                                                  :type "D"}}
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziCallNode",
                                                                  :arguments [{:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziNumberLiteralNode", :value -1}}
                                                                              {:__class__ "Association",
                                                                               :key nil,
                                                                               :value {:__class__ "UziVariableNode", :name "a"}}],
                                                                  :selector "*"}}],
                                             :selector "write"}]},
                        :name "loop",
                        :state "running",
                        :tickingRate {:__class__ "UziTickingRateNode",
                                      :scale "s",
                                      :value 1}}]}
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
    (is (= (:globals expected) (:globals actual)))))

(deftest
  global-variable-with-value-different-than-default
  (let [ast {:__class__ "UziProgramNode",
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "a",
                        :value {:__class__ "UziNumberLiteralNode", :value 42}}],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "a"},
                                             :right {:__class__ "UziCallNode",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode", :name "a"}}
                                                                 {:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziNumberLiteralNode", :value 10}}],
                                                     :selector "+"}}]},
                        :name "loop",
                        :state "once",
                        :tickingRate nil}]}
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
    (is (= (:globals expected) (:globals actual)))))

(deftest
  local-variable-shadowing-global-variable
  (let [ast {:__class__ "UziProgramNode",
             :globals [{:__class__ "UziVariableDeclarationNode",
                        :name "a",
                        :value {:__class__ "UziNumberLiteralNode", :value 42}}],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "a"},
                                             :right {:__class__ "UziCallNode",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode", :name "a"}}
                                                                 {:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziNumberLiteralNode", :value 1}}],
                                                     :selector "+"}}
                                            {:__class__ "UziVariableDeclarationNode",
                                             :name "a",
                                             :value {:__class__ "UziNumberLiteralNode", :value 15}}
                                            {:__class__ "UziAssignmentNode",
                                             :left {:__class__ "UziVariableNode", :name "a"},
                                             :right {:__class__ "UziCallNode",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode", :name "a"}}
                                                                 {:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziNumberLiteralNode", :value 42}}],
                                                     :selector "+"}}]},
                        :name "loop",
                        :state "once",
                        :tickingRate nil}]}
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
    (is (= (:globals expected) (:globals actual)))))

(deftest
  conditional-children-order-should-not-impact-compilation
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziConditionalNode",
                                             :condition
                                                        {:__class__ "UziCallNode",
                                                         :arguments [{:__class__ "Association",
                                                                      :key nil,
                                                                      :value {:__class__ "UziPinLiteralNode",
                                                                              :number 9,
                                                                              :type "D"}}],
                                                         :selector "isOn"},
                                             :falseBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziVariableDeclarationNode",
                                                                       :name "a",
                                                                       :value {:__class__ "UziNumberLiteralNode", :value 21}}
                                                                      {:__class__ "UziAssignmentNode",
                                                                       :left {:__class__ "UziVariableNode", :name "a"},
                                                                       :right {:__class__ "UziCallNode",
                                                                               :arguments [{:__class__ "Association",
                                                                                            :key nil,
                                                                                            :value {:__class__ "UziVariableNode", :name "a"}}
                                                                                           {:__class__ "Association",
                                                                                            :key nil,
                                                                                            :value {:__class__ "UziNumberLiteralNode",
                                                                                                    :value 20}}],
                                                                               :selector "+"}}]},
                                             :trueBranch
                                                        {:__class__ "UziBlockNode",
                                                         :statements [{:__class__ "UziVariableDeclarationNode",
                                                                       :name "a",
                                                                       :value {:__class__ "UziNumberLiteralNode", :value 11}}
                                                                      {:__class__ "UziAssignmentNode",
                                                                       :left {:__class__ "UziVariableNode", :name "a"},
                                                                       :right {:__class__ "UziCallNode",
                                                                               :arguments [{:__class__ "Association",
                                                                                            :key nil,
                                                                                            :value {:__class__ "UziVariableNode", :name "a"}}
                                                                                           {:__class__ "Association",
                                                                                            :key nil,
                                                                                            :value {:__class__ "UziNumberLiteralNode",
                                                                                                    :value 10}}],
                                                                               :selector "+"}}]}}]},
                        :name "loop",
                        :state "once",
                        :tickingRate nil}]}
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
    (is (= (:globals expected) (:globals actual)))))
