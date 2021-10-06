(ns middleware.controller-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [clojure.core.async :as a :refer [<! go]]
            [middleware.test-utils :refer [test-async setup-fixture]]
            [middleware.compiler.compiler :as cc]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.boards :refer [UNO]]
            [middleware.device.utils.ring-buffer :as rb]
            [middleware.utils.fs.common :as fs]))

(use-fixtures :once setup-fixture)

; HACK(Richo): Quick mock to fake an uzi port that does nothing...
(extend-type #?(:clj java.lang.String
                :cljs string)
  ports/UziPort
  (close! [_])
  (make-in-chan! [_] (a/to-chan! (iterate inc 0)))
  (make-out-chan! [_] (a/chan (a/dropping-buffer 1))))

(defn setup []
  (ports/register-constructors! identity)
  (reset! dc/state dc/initial-state)
  ; HACK(Richo): Fake connection
  (swap! dc/state assoc
         :connection (ports/connect! "FAKE_PORT")
         :board UNO
         :timing {:diffs (rb/make-ring-buffer 10)
                  :arduino nil
                  :middleware nil}
         :reporting {:interval 5
                     :pins #{}
                     :globals #{}})
  ; HACK(Richo): Fake program
  (let [program (cc/compile-uzi-string
                 "task blink13() running 1/s { toggle(D13); }

                  var counter;
                  var n;

                  task loop() { add(n); }
                  proc add(v) { counter = inc(v); }
                  func inc(v) { return v + 1; }")]
    (dc/run program)))

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
  (let [program (cc/compile-uzi-string "task blink13() running 1/s { toggle(D13); }")]
    (dc/run program)
    (is (empty? (-> @dc/state :reporting :globals)))
    (is (= (-> @dc/state :program :running)
           program))))

(deftest set-report-interval
  (setup)
  (dc/set-report-interval 50)
  (is (= 50 (-> @dc/state :reporting :interval))))

; TODO(Richo): The following test logs an error message, we should consider disabling logging
(deftest process-running-scripts
  (setup)
  (dc/process-running-scripts
   {:scripts [{:running? true,
               :error-code 0,
               :error-msg "NO_ERROR",
               :error? false}
              {:running? false,
               :error-code 8,
               :error-msg "OUT_OF_MEMORY",
               :error? true}
              {:running? false,
               :error-code 0,
               :error-msg "NO_ERROR",
               :error? false}
              {:running? false,
               :error-code 0,
               :error-msg "NO_ERROR",
               :error? false}],
    :timestamp 4880})
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
  (dc/process-free-ram {:memory {:uzi 2182, :arduino 4163974488},
                        :timestamp 3429})
  (is (= (-> @dc/state :memory)
         {:uzi 2182, :arduino 4163974488})))

(deftest process-pin-value
  (setup)
  (dc/process-pin-value {:timestamp 14159, :data [{:number 13, :value 0}]})
  (is (= (-> @dc/state :pins)
         {:timestamp 14159, :data {"D13" {:number 13, :name "D13", :value 0}}})))

(deftest process-global-value
  (setup)
  (dc/process-global-value
   {:timestamp 14167,
    :data [{:number 3, :value 42, :raw-bytes [66 40 0 0]}
           {:number 4, :value 42, :raw-bytes [66 40 0 0]}]})
  (is (= (-> @dc/state :globals)
         {:timestamp 14167,
          :data {"n" {:number 4,
                      :name "n",
                      :value 42,
                      :raw-bytes [0x42 0x28 0x00 0x00]},
                 "counter" {:number 3,
                            :name "counter",
                            :value 42,
                            :raw-bytes [0x42 0x28 0x00 0x00]}}})))

(deftest process-profile
  (setup)
  (dc/process-profile
   {:data {:report-interval 5, :ticks 22835, :interval-ms 100}})
  (is (= (-> @dc/state :profiler)
         {:report-interval 5, :ticks 22835, :interval-ms 100})))

; TODO(Richo): The following test logs an error message, we should consider disabling logging
(deftest process-error
  (test-async
   (go
    (setup)
    (is (dc/connected?))
    (<! (dc/process-error {:error {:msg "DISCONNECT_ERROR", :code 32}}))
    (is (not (dc/connected?))))))

(deftest process-coroutine-state
  (setup)
  (dc/process-coroutine-state {:data {:index 1
                                      :pc 515
                                      :stack [0 1 2 3 4 5 6 7]
                                      :fp 4}})
  (is (= (-> @dc/state :debugger)
         {:index 1, :pc 515, :stack [0 1 2 3 4 5 6 7], :fp 4})))
