(ns middleware.code-generator-test
  (:require [clojure.test :refer :all]
            [middleware.code_generator.code_generator :refer :all ])
  (:use [middleware.test-utils]))


(deftest sanity-check
  (testing "Sanity check."
    (is (= 1 1))))

(deftest empty-program
  (testing "An Empty program should return an empty string"
    (let [expected ""
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest uninitialized-global-declaration
  (testing "An uninitialized Global variable should be printed on top of the program with it's default value"
    (let [expected "var a = 0;"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [{:__class__ "UziVariableDeclarationNode",
                          :name "a", :value {:__class__ "UziNumberLiteralNode", :value 0}}],
               :scripts [],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest multiple-global-declaration
  (testing "Several global variables declared should be printed in the correct order and the defined value"
    (let [expected "var a = 5;\nvar b = 3;\nvar c = 0.5;\nvar d = D13;"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [{:__class__ "UziVariableDeclarationNode", :name "a", :value {:__class__ "UziNumberLiteralNode", :value 5}}
                         {:__class__ "UziVariableDeclarationNode", :name "b", :value {:__class__ "UziNumberLiteralNode", :value 3}}
                         {:__class__ "UziVariableDeclarationNode", :name "c", :value {:__class__ "UziNumberLiteralNode", :value 0.5}}
                         {:__class__ "UziVariableDeclarationNode", :name "d",:value {:__class__ "UziPinLiteralNode", :type "D", :number 13}}],
               :scripts [],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest empty-script-running-once
  (testing "An empty script without any statements nor tickrate"
    (let [expected "task foo()\n{\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziTaskNode",
                          :name "foo",
                          :arguments [],
                          :body {:__class__ "UziBlockNode", :statements []},
                          :state "once",
                          :tickingRate nil}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest empty-script-stopped
  (testing "An empty and stopped script without any statements "
    (let [expected "task bar() stopped\n{\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziTaskNode",
                          :name "bar",
                          :arguments [],
                          :body {:__class__ "UziBlockNode", :statements []},
                          :state "stopped",
                          :tickingRate nil}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest empty-script-ticking
  (testing "An empty ticking script without any statements "
    (let [expected "task baz() running 3/s\n{\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziTaskNode",
                          :name "baz",
                          :arguments [],
                          :body {:__class__ "UziBlockNode", :statements []},
                          :state "running",
                          :tickingRate {:__class__ "UziTickingRateNode", :value 3, :scale "s"}}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest blink13
  (testing "The classic blink example"
    (let [expected "task blink() running 1/s\n{\n\ttoggle(D13);\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziTaskNode",
                          :name "blink",
                          :arguments [],
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziCallNode",
                                               :selector "toggle",
                                               :arguments [{:__class__ "Association",
                                                            :key nil,
                                                            :value {:__class__ "UziPinLiteralNode", :type "D", :number 13}}]}]},
                          :state "running",
                          :tickingRate {:__class__ "UziTickingRateNode", :value 1, :scale "s"}}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest procedure-with-argument
  (testing "A procedure with a single argument"
    (let [expected "proc blink(arg0)\n{\n\tturnOn(arg0);\n\tdelayS(1);\n\tturnOff(arg0);\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziProcedureNode",
                          :name "blink",
                          :arguments [{:__class__ "UziVariableDeclarationNode",
                                       :name "arg0",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}],
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziCallNode",
                                               :selector "turnOn",
                                               :arguments [{:__class__ "Association",
                                                            :key nil,
                                                            :value {:__class__ "UziVariableNode", :name "arg0"}}]}
                                              {:__class__ "UziCallNode",
                                               :selector "delayS",
                                               :arguments [{:__class__ "Association",
                                                            :key nil,
                                                            :value {:__class__ "UziNumberLiteralNode", :value 1}}]}
                                              {:__class__ "UziCallNode",
                                               :selector "turnOff",
                                               :arguments [{:__class__ "Association",
                                                            :key nil,
                                                            :value {:__class__ "UziVariableNode", :name "arg0"}}]}]}}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest function-with-arguments
  (testing "A Function with two arguments and a return"
    (let [expected "func default(arg0, arg1)\n{\n\treturn (arg0 % arg1);\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [],
               :scripts [{:__class__ "UziFunctionNode",
                          :name "default",
                          :arguments [{:__class__ "UziVariableDeclarationNode",
                                       :name "arg0",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                      {:__class__ "UziVariableDeclarationNode",
                                       :name "arg1",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}],
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziReturnNode",
                                               :value {:__class__ "UziCallNode",
                                                       :selector "%",
                                                       :arguments [{:__class__ "Association",
                                                                    :key nil,
                                                                    :value {:__class__ "UziVariableNode", :name "arg0"}}
                                                                   {:__class__ "Association",
                                                                    :key nil,
                                                                    :value {:__class__ "UziVariableNode", :name "arg1"}}]}}]}}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))

(deftest functions-with-calls-and-globals
  (testing "A program with two functions that modify a global"
    (let [expected "var global = 0;\nfunc forIncrease(from, to, by)\n{\n\tfor i = from to to by by\n\t{\n\t\tglobal = (global + 1);\n\t}\n\treturn global;\n}\nfunc run()\n{\n\tvar temp = forIncrease(1, 10, 0.5);\n}"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [{:__class__ "UziVariableDeclarationNode",
                          :name "global",
                          :value {:__class__ "UziNumberLiteralNode", :value 0}}],
               :scripts [{:__class__ "UziFunctionNode",
                          :name "forIncrease",
                          :arguments [{:__class__ "UziVariableDeclarationNode",
                                       :name "from",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                      {:__class__ "UziVariableDeclarationNode",
                                       :name "to",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}
                                      {:__class__ "UziVariableDeclarationNode",
                                       :name "by",
                                       :value {:__class__ "UziNumberLiteralNode", :value 0}}],
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziForNode",
                                               :counter {:__class__ "UziVariableDeclarationNode",
                                                         :name "i",
                                                         :value {:__class__ "UziNumberLiteralNode", :value 0}},
                                               :start {:__class__ "UziVariableNode", :name "from"},
                                               :stop {:__class__ "UziVariableNode", :name "to"},
                                               :step {:__class__ "UziVariableNode", :name "by"},
                                               :body {:__class__ "UziBlockNode",
                                                      :statements [{:__class__ "UziAssignmentNode",
                                                                    :left {:__class__ "UziVariableNode", :name "global"},
                                                                    :right {:__class__ "UziCallNode",
                                                                            :selector "+",
                                                                            :arguments [{:__class__ "Association",
                                                                                         :key nil,
                                                                                         :value {:__class__ "UziVariableNode",
                                                                                                 :name "global"}}
                                                                                        {:__class__ "Association",
                                                                                         :key nil,
                                                                                         :value {:__class__ "UziNumberLiteralNode",
                                                                                                 :value 1}}]}}]}}
                                              {:__class__ "UziReturnNode", :value {:__class__ "UziVariableNode", :name "global"}}]}}
                         {:__class__ "UziFunctionNode",
                          :name "run",
                          :arguments [],
                          :body {:__class__ "UziBlockNode",
                                 :statements [{:__class__ "UziVariableDeclarationNode",
                                               :name "temp",
                                               :value {:__class__ "UziCallNode",
                                                       :selector "forIncrease",
                                                       :arguments [{:__class__ "Association",
                                                                    :key nil,
                                                                    :value {:__class__ "UziNumberLiteralNode", :value 1}}
                                                                   {:__class__ "Association",
                                                                    :key nil,
                                                                    :value {:__class__ "UziNumberLiteralNode", :value 10}}
                                                                   {:__class__ "Association",
                                                                    :key nil,
                                                                    :value {:__class__ "UziNumberLiteralNode", :value 0.5}}]}}]}}],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))))