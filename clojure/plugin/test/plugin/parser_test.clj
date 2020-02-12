(ns plugin.parser-test
  (:require [clojure.test :refer :all]
            [plugin.parser.core :as parser :refer [parse]]
            )
  (:use [plugin.test-utils]))



(deftest empty-program
 (testing "An Empty Program" (let [src ""
        expected {:__class__ "UziProgramNode",
                  :imports [],
                  :globals [],
                  :scripts [],
                  :primitives []}
        actual (parse src) ]
    (is (equivalent? expected actual)))))

(deftest blink13
 (testing "A Task that blinks the pin 13" (let [src "task default() running 1/s {\n\ttoggle(D13);\n}"
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

(deftest sanity-check
  (testing "Sanity check."
    (is (= 1 1))))


