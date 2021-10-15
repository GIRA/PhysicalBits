(ns middleware.debugger-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [clojure.core.async :as a :refer [<! go]]
            [middleware.compilation.compiler :as cc]
            [middleware.device.debugger :as debugger]
            [middleware.test-utils :refer [test-async setup-fixture]]))

(use-fixtures :once setup-fixture)

(deftest instruction-groups
  (let [program (cc/compile-uzi-string "
                  task blink13() running 1/s {
                  	toggle(D13);
                  }

                  task loop() {
                  	var a = 0;
                  	forever {
                  		if ((a % 2) == 0) {
                  			turnOn(D11);
                  		} else {
                  			turnOff(D11);
                  		}
                  		a = (a + 1);
                  		delayMs(a);
                  	}
                  }
                ")
        expected '({:instructions ({:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 13,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "toggle"}}),
                    :start 0}
                   {:instructions ({:__class__ "UziReadLocalInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :name "a#1",
                                               :value 0,
                                               :size 1}}
                                   {:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 2,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "remainder"}}
                                   {:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 0,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "equals"}}
                                   {:__class__ "UziJZInstruction", :argument 3}),
                    :start 2}
                   {:instructions ({:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 11,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "turnOn"}}),
                    :start 8}
                   {:instructions ({:__class__ "UziJMPInstruction", :argument 2}),
                    :start 10}
                   {:instructions ({:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 11,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "turnOff"}}),
                    :start 11}
                   {:instructions ({:__class__ "UziReadLocalInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :name "a#1",
                                               :value 0,
                                               :size 1}}
                                   {:__class__ "UziPushInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :value 1,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive", :name "add"}}
                                   {:__class__ "UziWriteLocalInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :name "a#1",
                                               :value 0,
                                               :size 1}}),
                    :start 13}
                   {:instructions ({:__class__ "UziReadLocalInstruction",
                                    :argument {:__class__ "UziVariable",
                                               :name "a#1",
                                               :value 0,
                                               :size 1}}
                                   {:__class__ "UziPrimitiveCallInstruction",
                                    :argument {:__class__ "UziPrimitive",
                                               :name "delayMs"}}),
                    :start 17}
                   {:instructions ({:__class__ "UziJMPInstruction", :argument -18}),
                    :start 19})
        actual (map #(select-keys % [:start :instructions])
                    (debugger/instruction-groups program))]
    (is (= expected actual))))
