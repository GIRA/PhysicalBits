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
      (is (= expected actual)))
    ))

(deftest uninitialized-global
  (testing "An uninitialized Global variable should be printed on top of the program with it's default value"
    (let [expected "var a = 0;"
          ast {:__class__ "UziProgramNode",
               :imports [],
               :globals [{:__class__ "UziVariableDeclarationNode",
                          :name "a", :value {:__class__ "UziNumberLiteralNode", :value 0}}],
               :scripts [],
               :primitives []}
          actual (print ast)]
      (is (= expected actual)))
    ))