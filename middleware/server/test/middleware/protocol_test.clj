(ns middleware.protocol-test
  (:require [clojure.test :refer :all]
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

(deftest set-global-report
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
