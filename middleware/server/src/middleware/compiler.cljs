(ns middleware.compiler
  (:require [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.compilation.parser :as p]
            [middleware.compilation.compiler :as c]
            [middleware.compilation.encoder :as en]
            [middleware.compilation.linker :as linker]
            [middleware.ast.utils :as ast-utils]))

(defn init []
  (fs/register-fs! #'browser/file)
  (println "Compiler loaded successfully!"))

(defn ^:export parse [str]
  (clj->js (p/parse str)))

(defn ^:export compile [str type]
  (println type)
  (if (= type "uzi")
    (clj->js (c/compile-uzi-string str))
    (clj->js (c/compile-json-string str))))

(defn ^:export encode [program]
  (clj->js (en/encode (js->clj program :keywordize-keys true))))

(comment

 (defn compile-empty []
   (c/compile-uzi-string ""))
 (defn compile-blink []
   (c/compile-uzi-string "task blink() running 1/s {toggle(D13);}"))
 (defn compile-full []
   (c/compile-uzi-string "
    import sonar from 'Sonar.uzi' {
    	trigPin = D4;
    	echoPin = D3;
    	maxDistance = 200;
    	start reading;
    }
    import rightMotor from 'DCMotor.uzi' {
    	enablePin = D7;
    	forwardPin = D6;
    	reversePin = D5;
    }
    import leftMotor from 'DCMotor.uzi' {
    	enablePin = D10;
    	forwardPin = D9;
    	reversePin = D8;
    }

    task blink13() running 1/s {
    	toggle(D13);
    	if (sonar.distance_cm() < 15) {
    		rightMotor.backward(speed: 1);
    		leftMotor.backward(speed: 1);
    	} else {
    		rightMotor.forward(speed: 1);
    		leftMotor.forward(speed: 1);
    	}
    }"))

 (time (def program (compile-full)))
 (profile {}
          (dotimes [_ 50]
                   (compile-full)))
 (profile {} (compile-full))

 (simple-benchmark [] (compile-blink) 500)
 (simple-benchmark [] (compile-full) 50)
 *e
 ,,)
