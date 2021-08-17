(ns middleware.controller-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [middleware.device.controller :as dc]
            [middleware.device.boards :refer [UNO]]
            [middleware.device.utils.ring-buffer :as rb]))

; HACK(Richo): Quick mock to fake an uzi port that does nothing...
(extend-type java.lang.String
  dc/UziPort
  (close! [port])
  (write! [port data])
  (listen! [port listener-fn]))

(def program (dc/compile "task blink13() running 1/s { toggle(D13); }

                          var counter;
                          var n;

                          task loop() { add(n); }
                          proc add(v) { counter = inc(v); }
                          func inc(v) { return v + 1; }"
                         "uzi" true))

(defn setup []
  (reset! dc/state dc/initial-state)
  ; HACK(Richo): Fake connection
  (swap! dc/state assoc
         :port "COM4"
         :port-name "COM4"
         :connected? true
         :board UNO
         :timing {:diffs (rb/make-ring-buffer 10)
                  :arduino nil
                  :middleware nil}
         :reporting {:interval 5
                     :pins #{}
                     :globals #{}})
  ; HACK(Richo): Fake program
  (dc/run program))

(deftest set-global-report
  (setup)
  (dc/set-global-report "counter" false)
  (is (not (contains? (-> @dc/state :reporting :globals)
                      "counter")))
  (dc/set-global-report "counter" true)
  (is (contains? (-> @dc/state :reporting :globals)
                 "counter")))

(deftest set-pin-report
  (setup)
  (dc/set-pin-report "D13" false)
  (is (not (contains? (-> @dc/state :reporting :pins)
                      "D13")))
  (dc/set-pin-report "D13" true)
  (is (contains? (-> @dc/state :reporting :pins)
                 "D13")))

(deftest run-program
  (setup)
  (let [program (dc/compile "task blink13() running 1/s { toggle(D13); }" "uzi" true)]
    (dc/run program)
    (is (empty? (-> @dc/state :reporting :globals)))
    (is (= (-> @dc/state :program :running)
           program))))

(deftest set-report-interval
  (setup)
  (dc/set-report-interval 50)
  (is (= 50 (-> @dc/state :reporting :interval))))

(deftest process-running-scripts
  (setup)
  (dc/process-running-scripts
   (a/to-chan (remove string?
                      ["timestamp"    0 0 19 16
                       "count"        4
                       "scripts"      128 8 0 0])))
  (is (= (-> @dc/state :scripts)
         {"loop" {:running? false,
                  :index 1,
                  :name "loop",
                  :error-code 8,
                  :error-msg "OUT_OF_MEMORY",
                  :task? true,
                  :error? true},
          "blink13" {:running? true,
                     :index 0,
                     :name "blink13",
                     :error-code 0,
                     :error-msg "NO_ERROR",
                     :task? true,
                     :error? false},
          "inc" {:running? false,
                 :index 3,
                 :name "inc",
                 :error-code 0,
                 :error-msg "NO_ERROR",
                 :task? false,
                 :error? false},
          "add" {:running? false,
                 :index 2,
                 :name "add",
                 :error-code 0,
                 :error-msg "NO_ERROR",
                 :task? false,
                 :error? false}})))

(deftest process-free-ram
  (setup)
  (dc/process-free-ram
   (a/to-chan (remove string?
                      ["timestamp"  0 0 13 101
                       "arduino"    248 49 53 88
                       "uzi"        0 0 8 134])))
  (is (= (-> @dc/state :memory)
         {:uzi 2182, :arduino 4163974488})))

(deftest process-pin-value
  (setup)
  (dc/process-pin-value
   (a/to-chan (remove string? ["timestamp"			0 0 55 79
                               "count"					1
                               "n1[0]"					52
                               "n2[0]"					0])))
  (is (= (-> @dc/state :pins)
         {:timestamp 14159, :data {"D13" {:number 13, :name "D13", :value 0.0}}})))

(deftest process-global-value
  (setup)
  (dc/process-global-value
   (a/to-chan (remove string? ["timestamp"				0 0 55 87
                               "count" 				  2
                               "number[0]" 			3
                               "n1..n4[0]" 			0x42 0x28 0x00 0x00
                               "number[1]"				4
                               "n1..n4[1]"				0x42 0x28 0x00 0x00])))
  (is (= (-> @dc/state :globals)
         {:timestamp 14167,
          :data {"n" {:number 4,
                      :name "n",
                      :value 42.0,
                      :raw-bytes [0x42 0x28 0x00 0x00]},
                 "counter" {:number 3,
                            :name "counter",
                            :value 42.0,
                            :raw-bytes [0x42 0x28 0x00 0x00]}}})))

(deftest process-profile
  (setup)
  (dc/process-profile
   (a/to-chan (remove string? ["n1"				178
                               "n2"				51
                               "report-interval"	5])))
  (is (= (-> @dc/state :profiler)
         {:report-interval 5, :ticks 22835, :interval-ms 100})))

(deftest process-error
  (setup)
  (is (dc/connected?))
  (dc/process-error (a/to-chan [32]))
  (is (not (dc/connected?))))

(deftest process-coroutine-state
  (setup)
  (dc/process-coroutine-state
   (a/to-chan (remove string?
                      ["index"        1
                       "pc"           2 3
                       "fp"           4
                       "stack-size"   2
                       "stack"        0 1 2 3
                       4 5 6 7])))
  (is (= (-> @dc/state :debugger)
         {:index 1, :pc 515, :stack [0 1 2 3 4 5 6 7], :fp 4})))
