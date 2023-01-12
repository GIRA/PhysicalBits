(ns middleware.debugger-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [utils.tests :refer [setup-fixture]]
            [middleware.compilation.parser :as p]
            [middleware.compilation.compiler :as cc]
            [middleware.program.utils :as program]
            [middleware.program.emitter :as emit]
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
        expected [{:instructions [(emit/push-value 13)
                                  (emit/script-call "toggle")
                                  #_(emit/prim-call "pop")] ,
                   :start 12}
                  {:instructions [(emit/read-local "a#1")
                                  (emit/push-value 2)
                                  (emit/prim-call "remainder")
                                  (emit/push-value 0)
                                  (emit/prim-call "equals")
                                  (emit/jz 3)],
                   :start 14}
                  {:instructions [(emit/push-value 11)
                                  (emit/script-call "turnOn")
                                  #_(emit/prim-call "pop")] ,
                   :start 20}
                  {:instructions [(emit/jmp 2)],
                   :start 22}
                  {:instructions [(emit/push-value 11)
                                  (emit/script-call "turnOff")
                                  #_(emit/prim-call "pop")] ,
                   :start 23}
                  {:instructions [(emit/read-local "a#1")
                                  (emit/push-value 1)
                                  (emit/prim-call "add")
                                  (emit/write-local "a#1")] ,
                   :start 25}
                  {:instructions [(emit/read-local "a#1")
                                  (emit/prim-call "delayMs")] ,
                   :start 29}
                  {:instructions [(emit/jmp -18)] ,
                   :start 31}]
        actual (->> (debugger/instruction-groups program)
                    (filter (comp #{"blink13" "loop"} :name :script))
                    (map #(select-keys % [:start :instructions])))]
    (is (= expected actual))))

(deftest estimate-breakpoints-1
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12); \" <--- BREAKPOINT HERE \"
                                        delayMs(1);
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
            :pc 2,
            :stack '((255 255 0 0))}]
    (is (= [4 5] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [24] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-2
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13); \" <--- \"
                                    forever {
                                        turnOn(D12);
                                        delayMs(1000);
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
            :pc 27,
            :stack '((255 255 0 0) 
                     (65 80 0 0) 
                     (0 0 0 1)
                     (65 80 0 0) 
                     (0 1 0 18))}]
    (is (= [2] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [2] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [2] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-3
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayMs(1000);
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
            :pc 27,
            :stack '((255 255 0 0)
                     (65 64 0 0)
                     (0 0 0 7))}]
    (is (= [8] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [8] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [8] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-4
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayMs(1000);
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
            :pc 11,
            :stack '((255 255 0 0)
                     (65 80 0 0)
                     (0 0 0 1))}]
    (is (= [14 15 17 18] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [19] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [2] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-5
  (let [program (compile-string "
                                task loop() {
                                    toggle(D13);
                                    forever {
                                        turnOn(D12);
                                        delayMs(1000);
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
            :pc 8,
            :stack '((255 255 0 0))}]
    (is (= [2 3] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [2 3] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [] (debugger/estimate-breakpoints step-out vm program)))))

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
                                 		delayMs(1000);
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
            :pc 2,
            :stack '((65 80 0 0) (255 255 0 0))}]
    (is (= [6 7] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [6 7] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-7
  (let [program (compile-string "
                                var pin_2 = 0;
                                var pin_1 = 11;
                                
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
                                }
                                
                                proc loop() {
                                	pin_2 = 0;
                                	while (pin_2 < 13) { \" <--- BREAKPOINT HERE \"
                                		turnOn(pin_2);
                                		delayMs(1000);
                                		turnOff(pin_2);
                                		delayMs(500);
                                		pin_2 = (pin_2 + 1);
                                	}
                                	toggle(pin_1);
                                }
                                
                                task main() {
                                	forever {
                                		loop();
                                	}
                                }")
        vm {:fp 1,
            :index 5,
            :pc 21,
            :stack '((255 255 0 40) (0 0 0 40))}]
    (is (= [25 26 38 39] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [25 26 38 39] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [41] (debugger/estimate-breakpoints step-out vm program)))))

(deftest estimate-breakpoints-8
  (let [program (compile-string "
                                 var pin_2 = 0;
                                 var pin_1 = 13;
                                 
                                 proc toggle(pin) {
                                 	if isOn(pin) {
                                 		turnOff(pin);
                                 	} else {
                                 		turnOn(pin);
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
                                 }
                                 
                                 proc loop() {
                                 	for i = 0 to 12 by 1 { \" <--- BREAKPOINT HERE \"
                                 	    pin_2 = i;
                                 		turnOn(pin_2);
                                 		delayMs(100);
                                 		turnOff(pin_2);
                                 		delayMs(5);
                                 	}
                                 	toggle(pin_1);
                                 }
                                 
                                 task main() {
                                 	forever {
                                 		loop();
                                 	}
                                 }")
        vm {:fp 1,
            :index 5,
            :pc 19,
            :stack '((255 255 0 42)
                     (0 0 0 0)
                     (0 0 0 42))}]
    (is (= [21 22 23 24] (debugger/estimate-breakpoints step-over vm program)))
    (is (= [21 22 23 24] (debugger/estimate-breakpoints step-into vm program)))
    (is (= [43] (debugger/estimate-breakpoints step-out vm program)))))

(comment
  (do
    (def state @middleware.device.controller/state)
    (def program (-> state :program))
    (def vm (-> state :debugger :vm)))
  (map-indexed vector (program/instructions program))
  )