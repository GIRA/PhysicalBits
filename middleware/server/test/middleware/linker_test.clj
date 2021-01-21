(ns middleware.linker-test
  (:require [clojure.test :refer :all]
            [middleware.compiler.compiler :as cc]
            [middleware.compiler.linker :as l]
            [middleware.compiler.emitter :as emit])
  (:use [middleware.test-utils ]))

(def lib-dir "../../uzi/tests")

(defn compile-uzi-string [src]
  (register-program! src :lib-dir lib-dir)
  (:compiled (cc/compile-uzi-string src :lib-dir lib-dir)))

(defn link [ast]
  (-> ast
      (l/resolve-imports lib-dir)
      ; HACK(Richo): I remove the :primitives key because it makes the diff hard to read
      (dissoc :primitives)))

(def core-import
 {:__class__ "UziImportNode", :isResolved true, :path "core.uzi"})

(deftest
  importing-library-prepends-imported-tree-with-alias-applied
  ; import test from 'test_1.uzi';
  ; task main() { write(D13, test.foo()); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "test",
               :initializationBlock nil,
               :isResolved false,
               :path "test_1.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments
                  [{:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziPinLiteralNode",
                     :number 13,
                     :type "D"}}
                   {:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziCallNode",
                     :arguments [],
                     :selector "test.foo"}}],
                  :selector "write"}]},
               :name "main",
               :state "once",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "test",
                    :isResolved true,
                    :path "test_1.uzi"}],
                  :scripts
                  [{:__class__ "UziFunctionNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziReturnNode",
                       :value
                       {:__class__ "UziNumberLiteralNode",
                        :value 42}}]},
                    :name "test.foo"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}
                        {:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziCallNode",
                          :arguments [],
                          :selector "test.foo"}}],
                       :selector "write"}]},
                    :name "main",
                    :state "once",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-the-same-library-twice-doesnt-collide
  ; import test1 from 'test_1.uzi';
  ; import test2 from 'test_1.uzi';
  ; task main() { write(test2.foo(), test1.foo()); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [core-import
              {:__class__ "UziImportNode",
               :alias "test1",
               :initializationBlock nil,
               :isResolved false,
               :path "test_1.uzi"}
              {:__class__ "UziImportNode",
               :alias "test2",
               :initializationBlock nil,
               :isResolved false,
               :path "test_1.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments
                  [{:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziCallNode",
                     :arguments [],
                     :selector "test2.foo"}}
                   {:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziCallNode",
                     :arguments [],
                     :selector "test1.foo"}}],
                  :selector "write"}]},
               :name "main",
               :state "once",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "test1",
                    :isResolved true,
                    :path "test_1.uzi"}
                   {:__class__ "UziImportNode",
                    :alias "test2",
                    :isResolved true,
                    :path "test_1.uzi"}],
                  :scripts
                  [{:__class__ "UziFunctionNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziReturnNode",
                       :value
                       {:__class__ "UziNumberLiteralNode",
                        :value 42}}]},
                    :name "test1.foo"}
                   {:__class__ "UziFunctionNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziReturnNode",
                       :value
                       {:__class__ "UziNumberLiteralNode",
                        :value 42}}]},
                    :name "test2.foo"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziCallNode",
                          :arguments [],
                          :selector "test2.foo"}}
                        {:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziCallNode",
                          :arguments [],
                          :selector "test1.foo"}}],
                       :selector "write"}]},
                    :name "main",
                    :state "once",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-nested-libraries
  ; import t2 from 'test_3.uzi';
  ; task main() { write(D13, t2.bar(1)); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [core-import
              {:__class__ "UziImportNode",
               :alias "t2",
               :initializationBlock nil,
               :isResolved false,
               :path "test_3.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments
                  [{:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziPinLiteralNode",
                     :number 13,
                     :type "D"}}
                   {:__class__ "Association",
                    :key nil,
                    :value
                    {:__class__ "UziCallNode",
                     :arguments
                     [{:__class__ "Association",
                       :key nil,
                       :value
                       {:__class__ "UziNumberLiteralNode", :value 1}}],
                     :selector "t2.bar"}}],
                  :selector "write"}]},
               :name "main",
               :state "once",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals
                  [{:__class__ "UziVariableDeclarationNode",
                    :name "t2.t1.v",
                    :value
                    {:__class__ "UziNumberLiteralNode", :value 42}}],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t2",
                    :isResolved true,
                    :path "test_3.uzi"}],
                  :scripts
                  [{:__class__ "UziFunctionNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziReturnNode",
                       :value
                       {:__class__ "UziNumberLiteralNode",
                        :value 42}}]},
                    :name "t2.t1.foo"}
                   {:__class__ "UziFunctionNode",
                    :arguments
                    [{:__class__ "UziVariableDeclarationNode",
                      :name "a"}],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziReturnNode",
                       :value
                       {:__class__ "UziCallNode",
                        :arguments
                        [{:__class__ "Association",
                          :key nil,
                          :value
                          {:__class__ "UziCallNode",
                           :arguments
                           [{:__class__ "Association",
                             :key nil,
                             :value
                             {:__class__ "UziVariableNode",
                              :name "t2.t1.v"}}
                            {:__class__ "Association",
                             :key nil,
                             :value
                             {:__class__ "UziVariableNode",
                              :name "a"}}],
                           :selector "t2.+"}}
                         {:__class__ "Association",
                          :key nil,
                          :value
                          {:__class__ "UziCallNode",
                           :arguments [],
                           :selector "t2.t1.foo"}}],
                        :selector "t2.+"}}]},
                    :name "t2.bar"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}
                        {:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziCallNode",
                          :arguments
                          [{:__class__ "Association",
                            :key nil,
                            :value
                            {:__class__ "UziNumberLiteralNode",
                             :value 1}}],
                          :selector "t2.bar"}}],
                       :selector "write"}]},
                    :name "main",
                    :state "once",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  stopping-imported-task
  ; import t3 from 'test_4.uzi';
  ; task main() running { stop t3.blink13; }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t3",
               :initializationBlock nil,
               :isResolved false,
               :path "test_4.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziScriptStopNode",
                  :scripts ["t3.blink13"]}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t3",
                    :isResolved true,
                    :path "test_4.uzi"}],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t3.toggle"}]},
                    :name "t3.blink13",
                    :state "running"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStopNode",
                       :scripts ["t3.blink13"]}]},
                    :name "main",
                    :state "running",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  starting-imported-task
  ; import t3 from 'test_4.uzi';
  ; task main() running { start t3.blink13; }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t3",
               :initializationBlock nil,
               :isResolved false,
               :path "test_4.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziScriptStartNode",
                  :scripts ["t3.blink13"]}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t3",
                    :isResolved true,
                    :path "test_4.uzi"}],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t3.toggle"}]},
                    :name "t3.blink13",
                    :state "running"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStartNode",
                       :scripts ["t3.blink13"]}]},
                    :name "main",
                    :state "running",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-stops-a-task
  ; import t from 'test_5.uzi';
  ; task main() running { t.stopTask(); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock nil,
               :isResolved false,
               :path "test_5.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments [],
                  :selector "t.stopTask"}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :initializationBlock nil,
                    :isResolved true,
                    :path "test_5.uzi"}],
                  :scripts
                  [{:__class__ "UziProcedureNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStopNode",
                       :scripts ["t.blink"]}]},
                    :name "t.stopTask"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t.toggle"}]},
                    :name "t.blink",
                    :state "running"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.stopTask"}]},
                    :name "main",
                    :state "running",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-starts-a-task
  ; import t from 'test_6.uzi';
  ; task main() running { t.startTask(); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock nil,
               :isResolved false,
               :path "test_6.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments [],
                  :selector "t.startTask"}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :isResolved true,
                    :path "test_6.uzi"}],
                  :scripts
                  [{:__class__ "UziProcedureNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStartNode",
                       :scripts ["t.blink"]}]},
                    :name "t.startTask"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t.toggle"}]},
                    :name "t.blink",
                    :state "stopped"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.startTask"}]},
                    :name "main",
                    :state "running",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-calls-internal-proc
  ; import t from 'test_8.uzi';
  ; task main() running { t.blink(); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock nil,
               :isResolved false,
               :path "test_8.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments [],
                  :selector "t.blink"}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :isResolved true,
                    :path "test_8.uzi"}],
                  :scripts
                  [{:__class__ "UziProcedureNode",
                    :arguments
                    [{:__class__ "UziVariableDeclarationNode",
                      :name "pin"}
                     {:__class__ "UziVariableDeclarationNode",
                      :name "ms"}],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziVariableNode", :name "pin"}}],
                       :selector "t.toggle"}
                      {:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziVariableNode", :name "ms"}}],
                       :selector "t.delayMs"}]},
                    :name "t.toggleAndWait"}
                   {:__class__ "UziProcedureNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}
                        {:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziNumberLiteralNode",
                          :value 100}}],
                       :selector "t.toggleAndWait"}]},
                    :name "t.blink"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.blink"}]},
                    :name "main",
                    :state "running",
                    :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-script-that-executes-for-loop
  ; import t from 'test_9.uzi';
  ; task main() running { t.foo(); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock nil,
               :isResolved false,
               :path "test_9.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments [],
                  :selector "t.foo"}]},
               :name "main",
               :state "running",
               :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :initializationBlock nil,
                    :isResolved true,
                    :path "test_9.uzi"}],
                  :scripts
                  [{:__class__ "UziProcedureNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziForNode",
                       :body
                       {:__class__ "UziBlockNode",
                        :statements [{:__class__ "UziYieldNode"}]},
                       :counter
                       {:__class__ "UziVariableDeclarationNode",
                        :name "i"},
                       :start
                       {:__class__ "UziNumberLiteralNode", :value 1},
                       :step
                       {:__class__ "UziNumberLiteralNode", :value 1},
                       :stop
                       {:__class__ "UziNumberLiteralNode",
                        :value 10}}]},
                    :name "t.foo"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.foo"}]},
                    :name "main",
                    :state "running"}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  specifying-init-block-to-initialize-variables
  ; import t from 'test_10.uzi' {
  ;   a = 10; b = 20; c = 30;
  ; }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziAssignmentNode",
                  :left {:__class__ "UziVariableNode", :name "a"},
                  :right
                  {:__class__ "UziNumberLiteralNode", :value 10}}
                 {:__class__ "UziAssignmentNode",
                  :left {:__class__ "UziVariableNode", :name "b"},
                  :right
                  {:__class__ "UziNumberLiteralNode", :value 20}}
                 {:__class__ "UziAssignmentNode",
                  :left {:__class__ "UziVariableNode", :name "c"},
                  :right
                  {:__class__ "UziNumberLiteralNode", :value 30}}]},
               :isResolved false,
               :path "test_10.uzi"}],
             :scripts []}
        expected {:__class__ "UziProgramNode",
                  :globals
                  [{:__class__ "UziVariableDeclarationNode",
                    :name "t.a",
                    :value
                    {:__class__ "UziNumberLiteralNode", :value 10}}
                   {:__class__ "UziVariableDeclarationNode",
                    :name "t.b",
                    :value
                    {:__class__ "UziNumberLiteralNode", :value 20}}
                   {:__class__ "UziVariableDeclarationNode",
                    :name "t.c",
                    :value
                    {:__class__ "UziNumberLiteralNode", :value 30}}],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :initializationBlock
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziAssignmentNode",
                       :left {:__class__ "UziVariableNode", :name "a"},
                       :right
                       {:__class__ "UziNumberLiteralNode", :value 10}}
                      {:__class__ "UziAssignmentNode",
                       :left {:__class__ "UziVariableNode", :name "b"},
                       :right
                       {:__class__ "UziNumberLiteralNode", :value 20}}
                      {:__class__ "UziAssignmentNode",
                       :left {:__class__ "UziVariableNode", :name "c"},
                       :right
                       {:__class__ "UziNumberLiteralNode",
                        :value 30}}]},
                    :isResolved true,
                    :path "test_10.uzi"}],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 12,
                          :type "D"}}],
                       :selector "t.toggle"}]},
                    :name "t.blink12",
                    :state "running",
                    :tickingRate
                    {:__class__ "UziTickingRateNode",
                     :scale "s",
                     :value 1}}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziAssignmentNode",
                       :left
                       {:__class__ "UziVariableNode", :name "t.a"},
                       :right
                       {:__class__ "UziNumberLiteralNode", :value 1}}
                      {:__class__ "UziAssignmentNode",
                       :left
                       {:__class__ "UziVariableNode", :name "t.b"},
                       :right
                       {:__class__ "UziNumberLiteralNode", :value 2}}
                      {:__class__ "UziAssignmentNode",
                       :left
                       {:__class__ "UziVariableNode", :name "t.c"},
                       :right
                       {:__class__ "UziNumberLiteralNode",
                        :value 3}}]},
                    :name "t.setup",
                    :state "once"}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  specifying-init-block-to-start-and-stop-tasks
  ; import t from 'test_11.uzi' {
  ; 	start stopped1; resume stopped2;
  ; 	stop running1; pause running2;
  ; }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziScriptStartNode",
                  :scripts ["stopped1"]}
                 {:__class__ "UziScriptResumeNode",
                  :scripts ["stopped2"]}
                 {:__class__ "UziScriptStopNode",
                  :scripts ["running1"]}
                 {:__class__ "UziScriptPauseNode",
                  :scripts ["running2"]}]},
               :isResolved false,
               :path "test_11.uzi"}],
             :scripts []}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :initializationBlock
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStartNode",
                       :scripts ["stopped1"]}
                      {:__class__ "UziScriptResumeNode",
                       :scripts ["stopped2"]}
                      {:__class__ "UziScriptStopNode",
                       :scripts ["running1"]}
                      {:__class__ "UziScriptPauseNode",
                       :scripts ["running2"]}]},
                    :isResolved true,
                    :path "test_11.uzi"}],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.running1"}]},
                    :name "t.stopped1",
                    :state "running",
                    :tickingRate
                    {:__class__ "UziTickingRateNode",
                     :scale "s",
                     :value 1}}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments [],
                       :selector "t.running2"}]},
                    :name "t.stopped2",
                    :state "running",
                    :tickingRate
                    {:__class__ "UziTickingRateNode",
                     :scale "s",
                     :value 2}}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 12,
                          :type "D"}}],
                       :selector "t.toggle"}]},
                    :name "t.running1",
                    :state "stopped",
                    :tickingRate
                    {:__class__ "UziTickingRateNode",
                     :scale "s",
                     :value 3}}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t.toggle"}]},
                    :name "t.running2",
                    :state "stopped",
                    :tickingRate
                    {:__class__ "UziTickingRateNode",
                     :scale "s",
                     :value 4}}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  stopping-once-task-from-init-block-should-have-no-effect
  ; import t from 'test_12.uzi' { stop setup; }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :initializationBlock
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziScriptStopNode",
                  :scripts ["setup"]}]},
               :isResolved false,
               :path "test_12.uzi"}],
             :scripts []}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports
                  [core-import
                   {:__class__ "UziImportNode",
                    :alias "t",
                    :initializationBlock
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziScriptStopNode",
                       :scripts ["setup"]}]},
                    :isResolved true,
                    :path "test_12.uzi"}],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :key nil,
                         :value
                         {:__class__ "UziPinLiteralNode",
                          :number 13,
                          :type "D"}}],
                       :selector "t.turnOn"}]},
                    :name "t.setup",
                    :state "once"}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  importing-library-with-primitive-declarations
  ; import a from 'test_13.uzi';
  ; task main() { toggle(a.add(3, 4)); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "a",
               :isResolved false,
               :path "test_13.uzi"}],
             :scripts
             [{:__class__ "UziTaskNode",
               :arguments [],
               :body
               {:__class__ "UziBlockNode",
                :statements
                [{:__class__ "UziCallNode",
                  :arguments
                  [{:__class__ "Association",
                    :value
                    {:__class__ "UziCallNode",
                     :arguments
                     [{:__class__ "Association",
                       :value
                       {:__class__ "UziNumberLiteralNode", :value 3}}
                      {:__class__ "Association",
                       :value
                       {:__class__ "UziNumberLiteralNode", :value 4}}],
                     :selector "a.add"}}],
                  :selector "toggle"}]},
               :name "main",
               :state "once"}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :scripts
                  [{:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziVariableDeclarationNode",
                       :name "a",
                       :value
                       {:__class__ "UziCallNode",
                        :arguments
                        [{:__class__ "Association",
                          :value
                          {:__class__ "UziNumberLiteralNode",
                           :value 3}}
                         {:__class__ "Association",
                          :value
                          {:__class__ "UziNumberLiteralNode",
                           :value 4}}],
                        :primitive-name "add",
                        :selector "a.add"}}
                      {:__class__ "UziVariableDeclarationNode",
                       :name "b",
                       :value
                       {:__class__ "UziCallNode",
                        :arguments
                        [{:__class__ "Association",
                          :value
                          {:__class__ "UziNumberLiteralNode",
                           :value 3}}
                         {:__class__ "Association",
                          :value
                          {:__class__ "UziNumberLiteralNode",
                           :value 4}}],
                        :primitive-name "notEquals",
                        :selector "a.~="}}
                      {:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :value
                         {:__class__ "UziVariableNode", :name "a"}}
                        {:__class__ "Association",
                         :value
                         {:__class__ "UziVariableNode", :name "b"}}],
                       :primitive-name "write",
                       :selector "a.write"}]},
                    :name "a.test",
                    :state "once"}
                   {:__class__ "UziTaskNode",
                    :arguments [],
                    :body
                    {:__class__ "UziBlockNode",
                     :statements
                     [{:__class__ "UziCallNode",
                       :arguments
                       [{:__class__ "Association",
                         :value
                         {:__class__ "UziCallNode",
                          :arguments
                          [{:__class__ "Association",
                            :value
                            {:__class__ "UziNumberLiteralNode",
                             :value 3}}
                           {:__class__ "Association",
                            :value
                            {:__class__ "UziNumberLiteralNode",
                             :value 4}}],
                          :primitive-name "add",
                          :selector "a.add"}}],
                       :primitive-name "toggle",
                       :selector "toggle"}]},
                    :name "main",
                    :state "once"}]}
        actual (link ast)]
    (is (equivalent? expected actual))))

(deftest
  dependency-cycles-should-not-hang-the-compiler
  ; import a from 'test_14_a.uzi';
  ; import b from 'test_14_b.uzi';
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "a",
               :isResolved false,
               :path "test_14_a.uzi"}
              {:__class__ "UziImportNode",
               :alias "b",
               :isResolved false,
               :path "test_14_b.uzi"}],
             :scripts []}]
    (is (thrown? Exception (link ast)))))

(deftest
  importing-non-existing-library-should-raise-error
  ; import t from 'test0_NO_EXISTE.uzi';
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports
             [{:__class__ "UziImportNode",
               :alias "t",
               :isResolved false,
               :path "test0_NO_EXISTE.uzi"}],
             :scripts []}]
    (is (thrown? Exception (link ast)))))

(deftest
  importing-a-script-with-a-local-var-that-shadows-global
  (let [expected (emit/program
                   :globals
                   #{(emit/constant 0) (emit/variable "t.a" 0)
                     (emit/constant 100) (emit/constant 10)}
                   :scripts
                   [(emit/script
                      :name
                      "t.foo"
                      :delay
                      0
                      :running?
                      true
                      :locals
                      [(emit/variable "b#1" 0)
                       (emit/variable "a#2" 100)]
                      :instructions
                      [(emit/push-value 10)
                       (emit/write-global "t.a")
                       (emit/read-global "t.a")
                       (emit/write-local "b#1")
                       (emit/read-local "a#2")
                       (emit/write-local "b#1")
                       (emit/stop "t.foo")])])
        actual (compile-uzi-string "import t from 'test_15.uzi';")]
    (is (equivalent? expected actual))))
