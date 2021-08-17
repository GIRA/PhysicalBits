(ns middleware.protocol-test
  (:require [clojure.test :refer :all]
            [clojure.core.async :as a]
            [middleware.device.protocol :as p]))

(deftest set-global-value
  (doseq [[value bytes]
          {10       [10 3 65 32 0 0]
           -1       [10 3 191 128 0 0]
           3.14159  [10 3 64 73 15 208]
           0        [10 3 0 0 0 0]
           1        [10 3 63 128 0 0]
           -10      [10 3 193 32 0 0]
           65536    [10 3 71 128 0 0]
           65537    [10 3 71 128 0 128]
           -65536   [10 3 199 128 0 0]
           -65537   [10 3 199 128 0 128]}]
    (is (= bytes (p/set-global-value 3 value)))))

(deftest set-global-report
  (is (= [11 3 1] (p/set-global-report 3 true)))
  (is (= [11 3 0] (p/set-global-report 3 false))))

(deftest set-pin-value
  (doseq [[value bytes]
          {0.0  [1 13 0]
           0.1  [1 13 26]
           0.01 [1 13 3]
           0.2  [1 13 51]
           0.3  [1 13 77]
           0.9  [1 13 230]
           1.0  [1 13 255]}]
    (is (= bytes (p/set-pin-value 13 value)))))

(deftest set-pin-report
  (is (= [5 13 1] (p/set-pin-report 13 true)))
  (is (= [5 13 0] (p/set-pin-report 13 false))))

(deftest run-program
  (let [bytecodes [1 1 4 0 128 4 131 129 166 147]]
    (is (= [0 0 10 1 1 4 0 128 4 131 129 166 147]
           (p/run bytecodes)))))

(deftest install-program
  (let [bytecodes [1 1 4 0 128 4 131 129 166 147]]
    (is (= [6 0 10 1 1 4 0 128 4 131 129 166 147]
           (p/install bytecodes)))))

(deftest start-reporting
  (is (= [3] (p/start-reporting))))

(deftest stop-reporting
  (is (= [4] (p/stop-reporting))))

(deftest start-profiling
  (is (= [8 1] (p/start-profiling))))

(deftest stop-profiling
  (is (= [8 0] (p/stop-profiling))))

(deftest set-report-interval
  (is (= [9 50] (p/set-report-interval 50))))

(deftest set-all-breakpoints
  (is (= [14 1] (p/set-all-breakpoints))))

(deftest clear-all-breakpoints
  (is (= [14 0] (p/clear-all-breakpoints))))

(deftest send-continue
  (is (= [12] (p/continue))))

(deftest keep-alive
  (is (= [7] (p/keep-alive))))

(deftest request-connection
  (is (= [255 0 8] (p/request-connection))))

(deftest confirm-handshake
  (is (= 50 (p/confirm-handshake 42))))

(deftest read-timestamp
  (let [in (a/to-chan [0 0 13 58])]
    (is (= 3386 (p/read-timestamp in)))))

(deftest process-running-scripts
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_RUNNING_SCRIPTS
                              "timestamp"  0 0 13 58
                              "count"      0])))
         {:tag :running-scripts
          :timestamp 3386
          :scripts []}))
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_RUNNING_SCRIPTS
                              "timestamp"  0 0 46 12
                              "count"      1
                              "scripts"    128])))
         {:tag :running-scripts
          :timestamp 11788
          :scripts [{:running? true
                     :error-code 0
                     :error-msg "NO_ERROR"
                     :error? false}]}))
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_RUNNING_SCRIPTS
                              "timestamp"    0 0 19 16
                              "count"        4
                              "scripts"      128 8 0 0])))
         {:tag :running-scripts
          :timestamp 4880
          :scripts [{:running? true
                     :error-code 0
                     :error-msg "NO_ERROR"
                     :error? false}
                    {:running? false
                     :error-code 8
                     :error-msg "OUT_OF_MEMORY"
                     :error? true}
                    {:running? false
                     :error-code 0
                     :error-msg "NO_ERROR"
                     :error? false}
                    {:running? false
                     :error-code 0
                     :error-msg "NO_ERROR"
                     :error? false}]})))

(deftest process-free-ram
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_FREE_RAM
                              "timestamp"  0 0 13 101
                              "arduino"    248 49 53 88
                              "uzi"        0 0 8 134])))
         {:tag :free-ram
          :timestamp 3429
          :memory {:uzi 2182, :arduino 4163974488}})))

(deftest process-pin-value
  (is (= (p/process-next-message
          (a/to-chan (remove string? [p/MSG_IN_PIN_VALUE
                                      "timestamp"			0 0 55 79
                                      "count"					1
                                      "n1[0]"					52
                                      "n2[0]"					0])))
         {:tag :pin-value
          :timestamp 14159
          :data [{:number 13 :value 0.0}]})))

(deftest process-global-value
  (is (= (p/process-next-message
          (a/to-chan (remove string? [p/MSG_IN_GLOBAL_VALUE
                                      "timestamp"				0 0 55 87
                                      "count" 				  2
                                      "number[0]" 			3
                                      "n1..n4[0]" 			0x42 0x28 0x00 0x00
                                      "number[1]"				4
                                      "n1..n4[1]"				0x42 0x28 0x00 0x00])))
         {:tag :global-value
          :timestamp 14167
          :data [{:number 3 :value 42.0 :raw-bytes [0x42 0x28 0x00 0x00]}
                 {:number 4 :value 42.0 :raw-bytes [0x42 0x28 0x00 0x00]}]})))

(deftest process-profile
  (is (= (p/process-next-message
          (a/to-chan (remove string? [p/MSG_IN_PROFILE
                                      "n1"				178
                                      "n2"				51
                                      "report-interval"	5])))
         {:tag :profile
          :data {:report-interval 5, :ticks 22835, :interval-ms 100}})))

(deftest process-error
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_ERROR 1]))
         {:tag :error
          :error {:code 1 :msg "STACK_OVERFLOW"}}))
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_ERROR 2]))
         {:tag :error
          :error {:code 2 :msg "STACK_UNDERFLOW"}}))
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_ERROR 4]))
         {:tag :error
          :error {:code 4 :msg "ACCESS_VIOLATION"}}))
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_ERROR 8]))
         {:tag :error
          :error {:code 8 :msg "OUT_OF_MEMORY"}}))
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_ERROR 9]))
         {:tag :error
          :error {:code 9 :msg "STACK_OVERFLOW & OUT_OF_MEMORY"}})))

(deftest process-coroutine-state
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_COROUTINE_STATE
                              "index"        1
                              "pc"           2 3
                              "fp"           4
                              "stack-size"   2
                              "stack"        0 1 2 3
                              4 5 6 7])))
         {:tag :coroutine-state
          :data {:index 1, :pc 515, :stack [0 1 2 3 4 5 6 7], :fp 4}})))

(deftest process-trace
  (is (= (p/process-next-message
          (a/to-chan (remove string?
                             [p/MSG_IN_TRACE
                              "count"       10
                              "msg"         82 105 99 104 111 32 99 97 112 111])))
         {:tag :trace
          :msg "Richo capo"})))

(deftest process-serial-tunnel
  (is (= (p/process-next-message (a/to-chan [p/MSG_IN_SERIAL_TUNNEL 42]))
         {:tag :serial
          :data 42})))
