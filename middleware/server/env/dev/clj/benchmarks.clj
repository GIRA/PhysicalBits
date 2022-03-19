(ns benchmarks
  (:require [criterium.core :refer [bench with-progress-reporting]]))

(comment
  
  (def path (apply list (range 30)))
  (def path (apply vector (range 30)))

  (with-progress-reporting (bench (first path)))
  
  )

(comment
  
  (require '[middleware.core :refer [compile-uzi-string]])

  (with-progress-reporting
    (bench (compile-uzi-string src :check-external-code? false)))
  
  (with-progress-reporting
    (bench (not= src src)))
  
  (with-progress-reporting
    (bench (not (identical? src src))))


  (def src
"
import sonar from 'Sonar.uzi' {
	trigPin = A5;
	echoPin = A4;
	maxDistance = 200;
	start reading;
}
import leftMotor from 'DCMotor.uzi' {
	enablePin = D5;
	forwardPin = D7;
	reversePin = D8;
}
import rightMotor from 'DCMotor.uzi' {
	enablePin = D6;
	forwardPin = D11;
	reversePin = D9;
}

var temp = 0;

task setup() {
	setServoDegrees(D3, 90);
}

task keepDistance() running 1000/s {
	temp = sonar.distance_cm();
	temp = ((temp - 10) / 10);
	if (temp > 100) {
		leftMotor.brake();
		rightMotor.brake();
	} else {
		move(temp);
	}
}

proc move(speed) {
	if (speed > 0) {
		leftMotor.forward(speed: speed);
		rightMotor.forward(speed: speed);
	} else {
		speed = (speed * -1);
		leftMotor.backward(speed: speed);
		rightMotor.backward(speed: speed);
	}
}")
  )

(comment

 (def v (vec (range 0 100)))
 (nth (map inc v) 12 nil)
 (nth v 120)

 (with-progress-reporting (bench (reduce + 0 v)))
"Evaluation count : 20696940 in 60 samples of 344949 calls.
             Execution time mean : 3.085101 µs
    Execution time std-deviation : 433.058990 ns
   Execution time lower quantile : 2.734087 µs ( 2.5%)
   Execution time upper quantile : 4.433880 µs (97.5%)
                   Overhead used : 11.013935 ns

Found 6 outliers in 60 samples (10.0000 %)
	low-severe	 2 (3.3333 %)
	low-mild	 4 (6.6667 %)
 Variance from outliers : 82.4155 % Variance is severely inflated by outliers"

 (with-progress-reporting
   (bench (loop [i 0, acc 0]
            (if-let [val (get v i)]
              (recur
                (inc i)
                (+ acc val))
              acc))))
"Evaluation count : 17386500 in 60 samples of 289775 calls.
             Execution time mean : 3.532437 µs
    Execution time std-deviation : 217.695749 ns
   Execution time lower quantile : 3.311638 µs ( 2.5%)
   Execution time upper quantile : 4.040414 µs (97.5%)
                   Overhead used : 11.013935 ns

Found 5 outliers in 60 samples (8.3333 %)
	low-severe	 3 (5.0000 %)
	low-mild	 2 (3.3333 %)
 Variance from outliers : 46.7315 % Variance is moderately inflated by outliers
"

(with-progress-reporting
  (bench (loop [i 0, acc 0]
           (if-let [val (nth v i nil)]
             (recur
               (inc i)
               (+ acc val))
             acc))))
"Evaluation count : 22580340 in 60 samples of 376339 calls.
             Execution time mean : 2.723621 µs
    Execution time std-deviation : 424.689886 ns
   Execution time lower quantile : 2.430304 µs ( 2.5%)
   Execution time upper quantile : 3.792985 µs (97.5%)
                   Overhead used : 11.013935 ns

Found 4 outliers in 60 samples (6.6667 %)
	low-severe	 3 (5.0000 %)
	low-mild	 1 (1.6667 %)
 Variance from outliers : 85.8622 % Variance is severely inflated by outliers"


 )

(comment
 (require '[middleware.parser.parser :as new-parser])

 (def sources (mapv slurp
                    (filter #(.isFile %)
                            (file-seq (clojure.java.io/file "../../uzi/libraries")))))

 (def trees (time (mapv new-parser/parse sources)))

 (with-progress-reporting (bench (mapv new-parser/parse sources) :verbose))


 (def src (slurp "../../uzi/tests/syntax.uzi"))
 (with-progress-reporting (bench (new-parser/parse src) :verbose))


 (def src (slurp "../../uzi/libraries/DCMotor.uzi"))
 (with-progress-reporting (bench (new-parser/parse src) :verbose))
 ,)


(comment

 (defn seek1 [pred coll]
   (reduce #(when (pred %2) (reduced %2)) nil coll))

 (defn seek2 [pred coll]
   (first (filter pred coll)))

 (time (seek2 (partial < 300) (range 0 1000)))
 (time (seek1 (partial < 300) (range 0 1000)))

 (with-progress-reporting (bench (seek2 (partial < 300) (range 0 1000))
                                 :verbose))
 "
Evaluation count : 5437620 in 60 samples of 90627 calls.
      Execution time sample mean : 13.207795 µs
             Execution time mean : 13.205522 µs
Execution time sample std-deviation : 2.424093 µs
    Execution time std-deviation : 2.454799 µs
   Execution time lower quantile : 10.773628 µs ( 2.5%)
   Execution time upper quantile : 20.332946 µs (97.5%)
                   Overhead used : 10.904902 ns

Found 7 outliers in 60 samples (11.6667 %)
	low-severe	 3 (5.0000 %)
	low-mild	 4 (6.6667 %)
 Variance from outliers : 89.3896 % Variance is severely inflated by outliers"

 (with-progress-reporting (bench (seek1 (partial < 300) (range 0 1000))
                                 :verbose))
 "
Evaluation count : 11782620 in 60 samples of 196377 calls.
      Execution time sample mean : 5.578162 µs
             Execution time mean : 5.586430 µs
Execution time sample std-deviation : 869.360948 ns
    Execution time std-deviation : 895.587587 ns
   Execution time lower quantile : 4.897106 µs ( 2.5%)
   Execution time upper quantile : 7.914496 µs (97.5%)
                   Overhead used : 10.904902 ns

Found 5 outliers in 60 samples (8.3333 %)
	low-severe	 2 (3.3333 %)
	low-mild	 3 (5.0000 %)
 Variance from outliers : 85.9056 % Variance is severely inflated by outliers"


 (let [data (vec (range 0 10000))]
   (with-progress-reporting (bench (seek2 (partial < 300) data)
                                   :verbose)))
 "
Evaluation count : 4280460 in 60 samples of 71341 calls.
      Execution time sample mean : 13.278606 µs
             Execution time mean : 13.276709 µs
Execution time sample std-deviation : 1.970235 µs
    Execution time std-deviation : 1.982780 µs
   Execution time lower quantile : 11.133591 µs ( 2.5%)
   Execution time upper quantile : 17.500149 µs (97.5%)
                   Overhead used : 10.904902 ns"

 (let [data (vec (range 0 10000))]
   (with-progress-reporting (bench (seek1 (partial < 300) data)
                                   :verbose)))
 "
Evaluation count : 10645920 in 60 samples of 177432 calls.
      Execution time sample mean : 5.251314 µs
             Execution time mean : 5.252672 µs
Execution time sample std-deviation : 167.601902 ns
    Execution time std-deviation : 171.474301 ns
   Execution time lower quantile : 5.117362 µs ( 2.5%)
   Execution time upper quantile : 5.844896 µs (97.5%)
                   Overhead used : 10.904902 ns

Found 6 outliers in 60 samples (10.0000 %)
	low-severe	 3 (5.0000 %)
	low-mild	 3 (5.0000 %)
 Variance from outliers : 19.0226 % Variance is moderately inflated by outliers"

 (let [data (vec (range 0 10000))]
   (with-progress-reporting (bench (seek2 (partial < 30000) data)
                                   :verbose)))
 "
Evaluation count : 170400 in 60 samples of 2840 calls.
      Execution time sample mean : 396.240212 µs
             Execution time mean : 396.641513 µs
Execution time sample std-deviation : 94.228010 µs
    Execution time std-deviation : 94.556211 µs
   Execution time lower quantile : 353.318325 µs ( 2.5%)
   Execution time upper quantile : 710.531518 µs (97.5%)
                   Overhead used : 10.904902 ns

Found 9 outliers in 60 samples (15.0000 %)
	low-severe	 3 (5.0000 %)
	low-mild	 6 (10.0000 %)
 Variance from outliers : 92.9342 % Variance is severely inflated by outliers"


 (let [data (vec (range 0 10000))]
   (with-progress-reporting (bench (seek1 (partial < 30000) data)
                                   :verbose)))
 "
Evaluation count : 351900 in 60 samples of 5865 calls.
      Execution time sample mean : 170.756418 µs
             Execution time mean : 170.825148 µs
Execution time sample std-deviation : 5.328324 µs
    Execution time std-deviation : 5.445278 µs
   Execution time lower quantile : 167.098268 µs ( 2.5%)
   Execution time upper quantile : 185.872910 µs (97.5%)
                   Overhead used : 10.904902 ns

Found 8 outliers in 60 samples (13.3333 %)
	low-severe	 2 (3.3333 %)
	low-mild	 6 (10.0000 %)
 Variance from outliers : 18.9748 % Variance is moderately inflated by outliers"


 ,,)
