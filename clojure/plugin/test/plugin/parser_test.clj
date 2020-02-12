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
        expected {:__class__ "UziProgramNode",
                  :imports [],
                  :globals [],
                  :scripts [],
                  :primitives []}
        actual (parse src) ]
    (is (equivalent? expected actual)))))

(deftest blink13
 (testing "A Task that blinks the pin 13"
   (let [src "task default() running 1/s {\n\ttoggle(D13);\n}"
        expected {:__class__ "UziProgramNode",
                  :imports [],
                  :globals [],
                  :scripts [{:__class__ "UziTaskNode",
                             ;:id "3$/qBEx97?LM|Hh;wA${",
                             :name "default",
                             :arguments [],
                             :state "running",
                             :tickingRate {:__class__ "UziTickingRateNode",
                                           ;:id "3$/qBEx97?LM|Hh;wA${",
                                           :value 1,
                                           :scale "s"},
                             :body {:__class__ "UziBlockNode",
                                    ;:id "3$/qBEx97?LM|Hh;wA${",
                                    :statements [{:__class__ "UziCallNode",
                                                  ;:id "^q3#.2SSim/:=a8jJ*La",
                                                  :selector "toggle",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziPinLiteralNode",
                                                                       ;:id "LfLVxWiv*bl~AKL.9GJV",
                                                                       :type "D",
                                                                       :number 13}}]}]}}],
                  :primitives []}
        actual (parse src) ]
    (is (equivalent? expected actual)))))

(deftest procedure-with-argument
  (testing "A procedure with a single argument"
    (let [src "proc blink(arg0) {\n\tturnOn(arg0);\n\tdelayS(1);\n\tturnOff(arg0);\n}"
          expected  {:__class__ "UziProgramNode",
                     :imports [],
                     :globals [],
                     :scripts [{:__class__ "UziProcedureNode",
                                :name "blink",
                                :arguments [{:__class__ "UziVariableDeclarationNode",
                                             :name "arg0"}],
                                :body {:__class__ "UziBlockNode",
                                       :statements [{:__class__ "UziCallNode",
                                                     :selector "turnOn",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode",
                                                                          :name "arg0"}}]},
                                                    {:__class__ "UziCallNode",
                                                     :selector "delayS",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziNumberLiteralNode",
                                                                          :value 1}}]},
                                                    {:__class__ "UziCallNode",
                                                     :selector "turnOff",
                                                     :arguments [{:__class__ "Association",
                                                                  :key nil,
                                                                  :value {:__class__ "UziVariableNode",
                                                                          :name "arg0"}}]}]}}],
                     :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))

(deftest function-with-arguments
  (testing "A Function with two arguments and a return"
    (let [ src "func default(arg0, arg1) {\n\treturn (arg0 % arg1);\n}"
          expected {:__class__ "UziProgramNode",
                    :imports [],
                    :globals [],
                    :scripts [{:__class__ "UziFunctionNode",
                               :name "default",
                               :arguments [{:__class__ "UziVariableDeclarationNode",
                                            :name "arg0"},
                                           {:__class__ "UziVariableDeclarationNode",
                                            :name "arg1"}],
                               :body {:__class__ "UziBlockNode",
                                      :statements [{:__class__ "UziReturnNode",
                                                    :value {:__class__ "UziCallNode",
                                                            :selector "%",
                                                            :arguments [{:__class__ "Association",
                                                                         :key nil,
                                                                         :value {:__class__ "UziVariableNode",
                                                                                 :name "arg0"}},
                                                                        {:__class__ "Association",
                                                                         :key nil,
                                                                         :value {:__class__ "UziVariableNode",
                                                                                 :name "arg1"}}]}}]}}],
                    :primitives []}
          actual (parse src)]
      (is (equivalent? expected actual)))))