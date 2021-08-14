(ns middleware.protocol-test
  (:require [clojure.test :refer :all]
            [middleware.device.controller :as dc]
            [middleware.device.protocol :as p]))

(def program (dc/compile
              "var counter; task loop() running { counter = counter + 1; }"
              "uzi" true))

(dc/run program)

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
    (is (= bytes (dc/set-global-value "counter" value)))))

(deftest set-global-report
  (is (= [11 3 1] (dc/set-global-report "counter" true)))
  (is (= [11 3 0] (dc/set-global-report "counter" false))))

(deftest set-pin-value
  (doseq [[value bytes]
          {0.0  [1 13 0]
           0.1  [1 13 26]
           0.01 [1 13 3]
           0.2  [1 13 51]
           0.3  [1 13 77]
           0.9  [1 13 230]
           1.0  [1 13 255]}]
    (is (= bytes (dc/set-pin-value "D13" value)))))

(deftest set-global-report
  (is (= [5 13 1] (dc/set-pin-report "D13" true)))
  (is (= [5 13 0] (dc/set-pin-report "D13" false))))

(deftest install-program
  (is (= [6 0 10 1 1 4 0 128 4 131 129 166 147]
         (dc/install program))))

(deftest start-reporting
  (is (= [3] (dc/start-reporting))))

(deftest stop-reporting
  (is (= [4] (dc/stop-reporting))))

(deftest start-profiling
  (is (= [8 1] (dc/start-profiling))))

(deftest stop-profiling
  (is (= [8 0] (dc/stop-profiling))))

(deftest set-report-interval
  (dc/set-report-interval 0) ; NOTE(Richo): Make sure it's set to something different
  (is (= [9 50] (dc/set-report-interval 50))))

(deftest set-all-breakpoints
  (is (= [14 1] (dc/set-all-breakpoints))))

(deftest clear-all-breakpoints
  (is (= [14 0] (dc/clear-all-breakpoints))))

(deftest send-continue
  (is (= [12] (dc/send-continue))))

(deftest send-keep-alive
  (is (= [7] (dc/send-keep-alive))))

(deftest connection-request
  (is (= [255 0 8] (dc/connection-request-msg))))

(deftest handshake-confirmation
  (is (= 50 (dc/handshake-confirmation 42))))

(comment
(mod )
(dc/handshake-confirmation 42)
  (dc/start-reporting)

  (dc/start-profiling)
  (dc/stop-profiling)

 (dc/set-report-interval 0)
 ,,,)
