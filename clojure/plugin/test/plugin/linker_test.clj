(ns plugin.linker-test
  (:require [clojure.test :refer :all]
            [plugin.compiler.linker :as l])
  (:use [plugin.test-utils]))

(defn link [ast]
  (l/resolve-imports ast "../../uzi/tests"))

(deftest importing-library-prepends-imported-tree-with-alias-applied
  ;  import test from 'test.uzi';
  ;  task main() { write(D13, test.foo()); }
  (let [ast {:__class__ "UziProgramNode",
             :globals [],
             :imports [{:__class__ "UziImportNode",
                        :alias "test",
                        :isResolved false,
                        :path "test.uzi"}],
             :primitives [],
             :scripts [{:__class__ "UziTaskNode",
                        :arguments [],
                        :body {:__class__ "UziBlockNode",
                               :statements [{:__class__ "UziCallNode",
                                             :arguments [{:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziPinLiteralNode",
                                                                  :number 13,
                                                                  :type "D"}},
                                                         {:__class__ "Association",
                                                          :key nil,
                                                          :value {:__class__ "UziCallNode",
                                                                  :arguments [],
                                                                  :selector "test.foo"}}],
                                             :selector "write"}]},
                        :name "main",
                        :state "once",
                        :tickingRate nil}]}
        expected {:__class__ "UziProgramNode",
                  :globals [],
                  :imports [{:__class__ "UziImportNode",
                             :alias "test",
                             :isResolved true,
                             :path "test.uzi"}],
                  :primitives [],
                  :scripts [{:__class__ "UziFunctionNode",
                             :arguments [],
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziReturnNode",
                                                  :value {:__class__ "UziNumberLiteralNode",
                                                          :value 42}}]},
                             :name "test.foo"},
                            {:__class__ "UziTaskNode",
                             :arguments [],
                             :body {:__class__ "UziBlockNode",
                                    :statements [{:__class__ "UziCallNode",
                                                  :arguments [{:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziPinLiteralNode",
                                                                       :number 13,
                                                                       :type "D"}},
                                                              {:__class__ "Association",
                                                               :key nil,
                                                               :value {:__class__ "UziCallNode",
                                                                       :arguments [],
                                                                       :selector "test.foo"}}],
                                                  :selector "write"}]},
                             :name "main",
                             :state "once",
                             :tickingRate nil}]}
        actual (link ast)]
    (is (equivalent? expected actual))))
