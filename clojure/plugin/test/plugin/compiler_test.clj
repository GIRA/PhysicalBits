(ns plugin.compiler-test
  (:require [clojure.test :refer :all]
            [plugin.compiler :refer :all])
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
                  :variables [{:__class__ "UziVariable",
                               :value 1000}]}
        actual (compile ast)]
    (is (equivalent? expected actual))))
