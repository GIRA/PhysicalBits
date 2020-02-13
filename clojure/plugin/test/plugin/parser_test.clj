(ns plugin.parser-test
  (:require [clojure.test :refer :all]
            [plugin.parser.core :as parser :refer [parse]]
            )
  (:use [plugin.test-utils]))



(deftest sanity-check
  (testing "Sanity check."
    (is (= 1 1))))

(deftest empty-program
  (testing "An Empty Program"
    (let [src ""
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [],
                    :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest blink13
  (testing "A Task that blinks the pin 13"
    (let [src "task default() running 1/s {\n\ttoggle(D13);\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [{:__class__   "UziTaskNode",
                                  ;:id "3$/qBEx97?LM|Hh;wA${",
                                  :name        "default",
                                  :arguments   [],
                                  :state       "running",
                                  :tickingRate {:__class__ "UziTickingRateNode",
                                                ;:id "3$/qBEx97?LM|Hh;wA${",
                                                :value     1,
                                                :scale     "s"},
                                  :body        {:__class__  "UziBlockNode",
                                                ;:id "3$/qBEx97?LM|Hh;wA${",
                                                :statements [{:__class__ "UziCallNode",
                                                              ;:id "^q3#.2SSim/:=a8jJ*La",
                                                              :selector  "toggle",
                                                              :arguments [{:__class__ "Association",
                                                                           :key       nil,
                                                                           :value     {:__class__ "UziPinLiteralNode",
                                                                                       ;:id "LfLVxWiv*bl~AKL.9GJV",
                                                                                       :type      "D",
                                                                                       :number    13}}]}]}}],
                    :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest procedure-with-argument
  (testing "A procedure with a single argument"
    (let [src "proc blink(arg0) {\n\tturnOn(arg0);\n\tdelayS(1);\n\tturnOff(arg0);\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [{:__class__ "UziProcedureNode",
                                  :name      "blink",
                                  :arguments [{:__class__ "UziVariableDeclarationNode",
                                               :name      "arg0"}],
                                  :body      {:__class__  "UziBlockNode",
                                              :statements [{:__class__ "UziCallNode",
                                                            :selector  "turnOn",
                                                            :arguments [{:__class__ "Association",
                                                                         :key       nil,
                                                                         :value     {:__class__ "UziVariableNode",
                                                                                     :name      "arg0"}}]},
                                                           {:__class__ "UziCallNode",
                                                            :selector  "delayS",
                                                            :arguments [{:__class__ "Association",
                                                                         :key       nil,
                                                                         :value     {:__class__ "UziNumberLiteralNode",
                                                                                     :value     1}}]},
                                                           {:__class__ "UziCallNode",
                                                            :selector  "turnOff",
                                                            :arguments [{:__class__ "Association",
                                                                         :key       nil,
                                                                         :value     {:__class__ "UziVariableNode",
                                                                                     :name      "arg0"}}]}]}}],
                    :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest function-with-arguments
  (testing "A Function with two arguments and a return"
    (let [src "func default(arg0, arg1) {\n\treturn (arg0 % arg1);\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [{:__class__ "UziFunctionNode",
                                  :name      "default",
                                  :arguments [{:__class__ "UziVariableDeclarationNode",
                                               :name      "arg0"},
                                              {:__class__ "UziVariableDeclarationNode",
                                               :name      "arg1"}],
                                  :body      {:__class__  "UziBlockNode",
                                              :statements [{:__class__ "UziReturnNode",
                                                            :value     {:__class__ "UziCallNode",
                                                                        :selector  "%",
                                                                        :arguments [{:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziVariableNode",
                                                                                                 :name      "arg0"}},
                                                                                    {:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziVariableNode",
                                                                                                 :name      "arg1"}}]}}]}}],
                    :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest functions-with-calls-and-globals
  (testing "A program with two functions that modify a global"
    (let [src "var global;\n\nfunc forIncrease(from, to, by) {\n\tfor i = from to to by by {\n\t\tglobal = (global + 1);\n\t}\n\treturn global;\n}\n\nfunc run() {\n\tvar temp = forIncrease(1, 10, 0.5);\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [{:__class__ "UziVariableDeclarationNode",
                                  :name      "global"}],
                    :scripts    [{:__class__ "UziFunctionNode",
                                  :name      "forIncrease",
                                  :arguments [{:__class__ "UziVariableDeclarationNode",
                                               :name      "from"},
                                              {:__class__ "UziVariableDeclarationNode",
                                               :name      "to"},
                                              {:__class__ "UziVariableDeclarationNode",
                                               :name      "by"}],
                                  :body      {:__class__  "UziBlockNode",
                                              :statements [{:__class__ "UziForNode",
                                                            :counter   {:__class__ "UziVariableDeclarationNode",
                                                                        :name      "i"},
                                                            :start     {:__class__ "UziVariableNode",
                                                                        :name      "from"},
                                                            :stop      {:__class__ "UziVariableNode",
                                                                        :name      "to"},
                                                            :step      {:__class__ "UziVariableNode",
                                                                        :name      "by"},
                                                            :body      {:__class__  "UziBlockNode",
                                                                        :statements [{:__class__ "UziAssignmentNode",
                                                                                      :left      {:__class__ "UziVariableNode",
                                                                                                  :name      "global"},
                                                                                      :right     {:__class__ "UziCallNode",
                                                                                                  :selector  "+",
                                                                                                  :arguments [{:__class__ "Association",
                                                                                                               :key       nil,
                                                                                                               :value     {:__class__ "UziVariableNode",
                                                                                                                           :name      "global"}},
                                                                                                              {:__class__ "Association",
                                                                                                               :key       nil,
                                                                                                               :value     {:__class__ "UziNumberLiteralNode",
                                                                                                                           :value     1}}]}}]}},
                                                           {:__class__ "UziReturnNode",
                                                            :value     {:__class__ "UziVariableNode",
                                                                        :name      "global"}}]}},
                                 {:__class__ "UziFunctionNode",
                                  :name      "run",
                                  :arguments [],
                                  :body      {:__class__  "UziBlockNode",
                                              :statements [{:__class__ "UziVariableDeclarationNode",
                                                            :name      "temp",
                                                            :value     {:__class__ "UziCallNode",
                                                                        :selector  "forIncrease",
                                                                        :arguments [{:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziNumberLiteralNode",
                                                                                                 :value     1}},
                                                                                    {:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziNumberLiteralNode",
                                                                                                 :value     10}},
                                                                                    {:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziNumberLiteralNode",
                                                                                                 :value     0.5}}]}}]}}],
                    :primitives []}
          actual (parse src)
          ] (is (equivalent? expected actual)))))

(deftest operator-precedence
  ;TODO(Tera): This should probably test all the operators
  (testing "A Function with some of the operators to check if the correct precedence is being built"
    (let [src "func operate(arg0, arg1) {\n\treturn arg0 + arg1**2*3;\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [{:__class__ "UziFunctionNode",
                                  :name      "operate",
                                  :arguments [{:__class__ "UziVariableDeclarationNode", :name "arg0"}
                                              {:__class__ "UziVariableDeclarationNode", :name "arg1"}],
                                  :body      {:__class__  "UziBlockNode",
                                              :statements [{:__class__ "UziReturnNode",
                                                            :value     {:__class__ "UziCallNode",
                                                                        :selector  "+",
                                                                        :arguments [{:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziVariableNode", :name "arg0"}}
                                                                                    {:__class__ "Association",
                                                                                     :key       nil,
                                                                                     :value     {:__class__ "UziCallNode",
                                                                                                 :selector  "*",
                                                                                                 :arguments [{:__class__ "Association",
                                                                                                              :key       nil,
                                                                                                              :value     {:__class__ "UziCallNode",
                                                                                                                          :selector  "**",
                                                                                                                          :arguments [{:__class__ "Association",
                                                                                                                                       :key       nil,
                                                                                                                                       :value     {:__class__ "UziVariableNode",
                                                                                                                                                   :name      "arg1"}}
                                                                                                                                      {:__class__ "Association",
                                                                                                                                       :key       nil,
                                                                                                                                       :value     {:__class__ "UziNumberLiteralNode",
                                                                                                                                                   :value     2}}]}}
                                                                                                             {:__class__ "Association",
                                                                                                              :key       nil,
                                                                                                              :value     {:__class__ "UziNumberLiteralNode",
                                                                                                                          :value     3}}]}}]}}]}}],
                    :primitives []}
          actual (parse src)
          ] (is (equivalent? expected actual)))
    ))

(deftest control-structures
  (testing "Several tasks with the main control structures on them"
    (let [src "task while_loop() {\n\twhile 1 {\n\t\twhile 1;\n\t}\n}\n\ntask until_loop() {\n\tuntil 1 {\n\t\tuntil 1;\n\t}\n}\n\ntask repeat_forever() {\n\tforever {\n\t\trepeat 5 {}\n\t}\n}\n\ntask conditional() {\n\tif 1 {\n\t\tif 0 {\n\t\t\tdelayS(1000);\n\t\t}\n\t} else {\n\t\tdelayMs(1000);\n\t}\n}"
          expected {:__class__  "UziProgramNode",
                    :imports    [],
                    :globals    [],
                    :scripts    [{:__class__   "UziTaskNode",
                                  :name        "while_loop",
                                  :arguments   [],
                                  :state       "once",
                                  :tickingRate nil,
                                  :body        {:__class__  "UziBlockNode",
                                                :statements [{:__class__ "UziWhileNode",
                                                              :pre       {:__class__  "UziBlockNode",
                                                                          :statements []},
                                                              :condition {:__class__ "UziNumberLiteralNode",
                                                                          :value     1},
                                                              :post      {:__class__  "UziBlockNode",
                                                                          :statements [{:__class__ "UziWhileNode",
                                                                                        :pre       {:__class__  "UziBlockNode",
                                                                                                    :statements []},
                                                                                        :condition {:__class__ "UziNumberLiteralNode",
                                                                                                    :value     1},
                                                                                        :post      {:__class__  "UziBlockNode",
                                                                                                    :statements []},
                                                                                        :negated   false}]},
                                                              :negated   false}]}},
                                 {:__class__   "UziTaskNode",
                                  :name        "until_loop",
                                  :arguments   [],
                                  :state       "once",
                                  :tickingRate nil,
                                  :body        {:__class__  "UziBlockNode",
                                                :statements [{:__class__ "UziWhileNode",
                                                              :pre       {:__class__  "UziBlockNode",
                                                                          :statements []},
                                                              :condition {:__class__ "UziNumberLiteralNode",
                                                                          :value     1},
                                                              :post      {:__class__  "UziBlockNode",
                                                                          :statements [{:__class__ "UziWhileNode",
                                                                                        :pre       {:__class__  "UziBlockNode",
                                                                                                    :statements []},
                                                                                        :condition {:__class__ "UziNumberLiteralNode",
                                                                                                    :value     1},
                                                                                        :post      {:__class__  "UziBlockNode",
                                                                                                    :statements []},
                                                                                        :negated   true}]},
                                                              :negated   true}]}},
                                 {:__class__   "UziTaskNode",
                                  :name        "repeat_forever",
                                  :arguments   [],
                                  :state       "once",
                                  :tickingRate nil,
                                  :body        {:__class__  "UziBlockNode",
                                                :statements [{:__class__ "UziForeverNode",
                                                              :body      {:__class__  "UziBlockNode",
                                                                          :statements [{:__class__ "UziRepeatNode",
                                                                                        :times     {:__class__ "UziNumberLiteralNode",
                                                                                                    :value     5},
                                                                                        :body      {:__class__  "UziBlockNode",
                                                                                                    :statements []}}]}}]}},
                                 {:__class__   "UziTaskNode",
                                  :name        "conditional",
                                  :arguments   [],
                                  :state       "once",
                                  :tickingRate nil,
                                  :body        {:__class__  "UziBlockNode",
                                                :statements [{:__class__   "UziConditionalNode",
                                                              :condition   {:__class__ "UziNumberLiteralNode",
                                                                            :value     1},
                                                              :trueBranch  {:__class__  "UziBlockNode",
                                                                            :statements [{:__class__   "UziConditionalNode",
                                                                                          :condition   {:__class__ "UziNumberLiteralNode",
                                                                                                        :value     0},
                                                                                          :trueBranch  {:__class__  "UziBlockNode",
                                                                                                        :statements [{:__class__ "UziCallNode",
                                                                                                                      :selector  "delayS",
                                                                                                                      :arguments [{:__class__ "Association",
                                                                                                                                   :key       nil,
                                                                                                                                   :value     {:__class__ "UziNumberLiteralNode",
                                                                                                                                               :value     1000}}]}]},
                                                                                          :falseBranch {:__class__  "UziBlockNode",
                                                                                                        :statements []}}]},
                                                              :falseBranch {:__class__  "UziBlockNode",
                                                                            :statements [{:__class__ "UziCallNode",
                                                                                          :selector  "delayMs",
                                                                                          :arguments [{:__class__ "Association",
                                                                                                       :key       nil,
                                                                                                       :value     {:__class__ "UziNumberLiteralNode",
                                                                                                                   :value     1000}}]}]}}]}}],
                    :primitives []}
          actual (parse src)
          ] (is (equivalent? expected actual)))

    ))

(deftest control-structures-part-II
  (testing "A task with a do while, do until and a yield"
    (let
      [src "task test()\n{\n\tdo{var a = 3;}\n\tuntil(1);\n\tdo{\n\t\tvar a= 4;\n\t\tyield;\n\t}while(1);\n}"
       expected {:__class__ "UziProgramNode",
                 :globals   [],
                 :imports   [],
                 :scripts   [{:__class__   "UziTaskNode",
                              :arguments   [],
                              :body        {:__class__  "UziBlockNode",
                                            :statements [{:__class__ "UziDoUntilNode",
                                                          :condition {:__class__ "UziNumberLiteralNode",
                                                                      :value     1},
                                                          :negated   true,
                                                          :post      {:__class__  "UziBlockNode",
                                                                      :statements []},
                                                          :pre       {:__class__  "UziBlockNode",
                                                                      :statements [{:__class__ "UziVariableDeclarationNode",
                                                                                    :name      "a",
                                                                                    :value     {:__class__ "UziNumberLiteralNode",
                                                                                                :value     3}}]}},
                                                         {:__class__ "UziDoWhileNode",
                                                          :condition {:__class__ "UziNumberLiteralNode",
                                                                      :value     1},
                                                          :negated   false,
                                                          :post      {:__class__  "UziBlockNode",
                                                                      :statements []},
                                                          :pre       {:__class__  "UziBlockNode",
                                                                      :statements [{:__class__ "UziVariableDeclarationNode",
                                                                                    :name      "a",
                                                                                    :value     {:__class__ "UziNumberLiteralNode",
                                                                                                :value     4}},
                                                                                   {:__class__ "UziYieldNode",
                                                                                    }]}}]},
                              :name        "test",
                              :state       "once",
                              :tickingRate nil}]}

       actual (parse src)] (is (equivalent? expected actual)))))