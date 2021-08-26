(ns middleware.ring-buffer-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing]])
            [middleware.device.utils.ring-buffer :as rb]))

(deftest ring-buffer-push!
  (let [buffer (rb/make-ring-buffer 10)]
    (dotimes [i 10]
             (rb/push! buffer i))
    (is (= (:array @buffer)
           (map double [0 1 2 3 4 5 6 7 8 9])))
    (rb/push! buffer 10)
    (is (= (:array @buffer)
           (map double [10 1 2 3 4 5 6 7 8 9])))
    (rb/push! buffer 11)
    (is (= (:array @buffer)
           (map double [10 11 2 3 4 5 6 7 8 9])))))

(deftest ring-buffer-avg
  (let [buffer (rb/make-ring-buffer 10)]
    (dotimes [i 10]
             (rb/push! buffer i))
    (is (= 4.5 (rb/avg buffer)))
    (doseq [n (range 10 15)]
      (rb/push! buffer n))
    (is (= 9.5 (rb/avg buffer)))))
