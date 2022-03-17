(ns middleware.debugger-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [clojure.core.async :as a :refer [<! go]]
            [utils.tests :refer [setup-fixture test-async]]
            [middleware.compilation.parser :as p]
            [middleware.compilation.compiler :as cc]
            [middleware.device.debugger :as debugger :refer [step-over step-into step-out]]))

(use-fixtures :once setup-fixture)

(defn compile-string [src]
  (cc/compile-tree (p/parse src)))

(deftest instruction-groups
  (let [program (compile-string "
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

(deftest estimate-breakpoints-1
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12); \" <--- BREAKPOINT HERE \"
                                        delayS(1);
                                        turnOff(D12);
                                        delayMs(500);
                                    }
                                }
                                
                                proc toggle(pin) {
                                    if isOn(pin) {
                                        turnOn(pin);
                                    } else {
                                        turnOff(pin);
                                    }
                                }
                                
                                func isOn(pin) {
                                    return read(pin) > 0.5;
                                }
                                
                                proc turnOn(pin) {
                                    write(pin, 1);
                                }
                                
                                proc turnOff(pin) {
                                    write(pin, 0);
                                }")
        vm {:fp 0,
            :index 0,
            :pc 3,
            :stack '((255 255 0 0))}]
    (is (= [6 7] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [29] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [0] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-2
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13); \" <--- \"
                                    forever {
                                        turnOn(D12);
                                        delayS(1);
                                        turnOff(D12);
                                        delayMs(500);
                                    }
                                }
                                
                                proc toggle(pin) {
                                    if isOn(pin) {
                                        turnOn(pin);
                                    } else {
                                        turnOff(pin); \" <--- \"
                                    }
                                }
                                
                                func isOn(pin) {
                                    return read(pin) > 0.5;
                                }
                                
                                proc turnOn(pin) {
                                    write(pin, 1);
                                }
                                
                                proc turnOff(pin) {
                                    write(pin, 0); \" <--- BREAKPOINT HERE (ON START) \"
                                }")
        vm {:fp 3,
            :index 0,
            :pc 32,
            :stack '((255 255 0 0)
                     (65 80 0 0)
                     (0 0 0 2)
                     (65 80 0 0)
                     (0 1 0 23))}]
    (is (= [23] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [23] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [23] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-3
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayS(1);
                                        turnOff(D12); \" <--- \"
                                        delayMs(500);
                                    }
                                }
                                
                                proc toggle(pin) {
                                    if isOn(pin) {
                                        turnOn(pin);
                                    } else {
                                        turnOff(pin);
                                    }
                                }
                                
                                func isOn(pin) {
                                    return read(pin) > 0.5;
                                }
                                
                                proc turnOn(pin) {
                                    write(pin, 1);
                                }
                                
                                proc turnOff(pin) {
                                    write(pin, 0); \" <--- BREAKPOINT HERE (AFTER START) \"
                                }")
        vm {:fp 1,
            :index 0,
            :pc 32,
            :stack '((255 255 0 0)
                     (65 64 0 0)
                     (0 0 0 10))}]
    (is (= [10] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [10] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [10] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-4
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayS(1);
                                        turnOff(D12);
                                        delayMs(500);
                                    }
                                }
                                
                                proc toggle(pin) {
                                    if isOn(pin) { \" <--- BREAKPOINT HERE (ON START) \"
                                        turnOn(pin);
                                    } else {
                                        turnOff(pin);
                                    }
                                }
                                
                                func isOn(pin) {
                                    return read(pin) > 0.5;
                                }
                                
                                proc turnOn(pin) {
                                    write(pin, 1);
                                }
                                
                                proc turnOff(pin) {
                                    write(pin, 0);
                                }")
        vm {:fp 1,
            :index 0,
            :pc 14,
            :stack '((255 255 0 0)
                     (65 80 0 0)
                     (0 0 0 2))}]
    (is (= [17 18 19 21 22 23] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [24] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [2] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-5
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayS(1);
                                        turnOff(D12);
                                        delayMs(500); \" <--- BREAKPOINT HERE \"
                                    }
                                }
                                
                                proc toggle(pin) {
                                    if isOn(pin) {
                                        turnOn(pin);
                                    } else {
                                        turnOff(pin);
                                    }
                                }
                                
                                func isOn(pin) {
                                    return read(pin) > 0.5;
                                }
                                
                                proc turnOn(pin) {
                                    write(pin, 1);
                                }
                                
                                proc turnOff(pin) {
                                    write(pin, 0);
                                }")
        vm {:fp 0,
            :index 0,
            :pc 11,
            :stack '((255 255 0 0))}]
    (is (= [3 4 5] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [3 4 5] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [0] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-6
  (let [program (compile-string "
                                 var pin_1 = 11;
                                 var pin_2 = 0;

                                 task loop() {
                                 	var pin_1 = D13;
                                 	toggle(pin_1);
                                 	forever {
                                 	    pin_2 = pin_2 + 1; \" <--- BREAKPOINT HERE \"
                                 	    
                                 		turnOn(pin_2);
                                 		delayS(1);
                                 		turnOff(pin_2);
                                 		delayMs(500);
                                 		
                                 		if (pin_2 >= 13) { 
                                 	        pin_2 = 0;
                                 	    }
                                 	}
                                 }
                                 
                                 proc toggle(pin) {
                                 	if isOn(pin) {
                                 		turnOn(pin);
                                 	} else {
                                 		turnOff(pin);
                                 	}
                                 }
                                 
                                 func isOn(pin) {
                                 	return (read(pin) > 0.5);
                                 }
                                 
                                 proc turnOn(pin) {
                                 	write(pin, 1);
                                 }
                                 
                                 proc turnOff(pin) {
                                 	write(pin, 0);
                                 }")
        vm {:fp 0,
            :index 0,
            :pc 3,
            :stack '((65 80 0 0) (255 255 0 0))}]
    (is (= [7 8 9] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [7 8 9] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [0] (debugger/estimate-breakpoints step-out vm program)))))