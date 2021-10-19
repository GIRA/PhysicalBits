(ns middleware.equivalent-test
  (:require #?(:clj [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing use-fixtures]])
            [utils.equivalent :refer [equivalent?]]))

(deftest equivalent?-matches-simple-values
  (is (equivalent? 1 1))
  (is (equivalent? 0.1 0.1))
  (is (equivalent? :richo :richo))
  (is (equivalent? "Richo" "Richo"))
  (is (equivalent? \a \a))
  (is (equivalent? true true))
  (is (not (equivalent? 1 2)))
  (is (not (equivalent? 0.1 0.2)))
  (is (not (equivalent? :richo :richi)))
  (is (not (equivalent? "Richo" "RICHO")))
  (is (not (equivalent? \a \b)))
  (is (not (equivalent? true false))))

(deftest equivalent?-works-on-vectors
  (is (equivalent? [1 2 3] [1 2 3]))
  (is (equivalent? ["Richo" \space "Capo" \!]
                   ["Richo" \space "Capo" \!]))
  (is (equivalent? [[0 1] [2 3]]
                   [[0 1] [2 3]]))
  (is (not (equivalent? [] nil)))
  (is (not (equivalent? [\1 \2 \3] "123")))
  (is (not (equivalent? [4 2] 42))))

(deftest equivalent?-works-on-lists
  (is (equivalent? '(1 2 3) [1 2 3]))
  (is (equivalent? ["Richo" \space "Capo" \!]
                   '("Richo" \space "Capo" \!)))
  (is (equivalent? '((0 1) [2 3])
                   [[0 1] '(2 3)]))
  (is (not (equivalent? '() nil)))
  (is (not (equivalent? '(\1 \2 \3) "123")))
  (is (not (equivalent? '(4 2) 42))))

(deftest equivalent?-works-on-vectors-of-different-sizes
  (is (equivalent? [1 2 3] [1 2 3 4] :strict? false))
  (is (equivalent? [] [1 2 3] :strict? false))
  (is (equivalent? [[0 1] [2 3]]
                   [[0 1 2] [2 3 4] [4 5 6]]
                   :strict? false))
  (is (not (equivalent? [1 2 3] [1 2 3 4])))
  (is (not (equivalent? [] [1 2 3])))
  (is (not (equivalent? [[0 1] [2 3]]
                        [[0 1 2] [2 3 4] [4 5 6]])))
  (is (not (equivalent? [[0 1] [2 3] 4 5 6]
                        [[0 1 2] [2 3 4] [4 5 6]]))))

(deftest equivalent?-works-on-lists-of-different-sizes
  (is (equivalent? '(1 2 3) '(1 2 3 4) :strict? false))
  (is (equivalent? [] '(1 2 3) :strict? false))
  (is (equivalent? '((0 1) (2 3))
                   '((0 1 2) (2 3 4) (4 5 6))
                   :strict? false))
  (is (not (equivalent? '(1 2 3) '(1 2 3 4))))
  (is (not (equivalent? [] '(1 2 3))))
  (is (not (equivalent? '((0 1) (2 3))
                        '((0 1 2) (2 3 4) (4 5 6)))))
  (is (not (equivalent? '((0 1) (2 3) 4 5 6)
                        '((0 1 2) (2 3 4) (4 5 6))))))

(deftest equivalent?-works-on-maps
  (is (equivalent? {:a 1, :b 2, :c 3}
                   {:a 1, :b 2, :c 3}))
  (is (equivalent? {:a 1, :b 2, :c 3}
                   {:b 2, :c 3, :a 1}))
  (is (equivalent? {:a [1 2 3], :b [2 3 4], :c [4 5 6]}
                   {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]}
                   :strict? false))
  (is (equivalent? {:a [1 2 3]}
                   {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]}
                   :strict? false))
  (is (equivalent? {:a []}
                   {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]}
                   :strict? false))
  (is (equivalent? {} {:a 1}
                   :strict? false))
  (is (not (equivalent? {:a [1 2 3], :b [2 3 4], :c [4 5 6]}
                        {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]})))
  (is (not (equivalent? {:a [1 2 3]}
                        {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]})))
  (is (not (equivalent? {:a []}
                        {:a [1 2 3 4], :b [2 3 4 5], :c [4 5 6 7]})))
  (is (not (equivalent? {} {:a 1})))
  (is (equivalent? {nil [1 2 3 4]} {nil '(1 2 3 4)}))
  (is (not (equivalent? {:a 1} {})))
  (is (not (equivalent? {nil 1} {nil 2})))
  (is (not (equivalent? {nil 1} {1 2})))
  (is (not (equivalent? {:a 1} nil)))
  (is (not (equivalent? {} nil)))
  (is (not (equivalent? {:a 1} [:a 1])))
  (is (not (equivalent? {:a 1} (seq {:a 1})))))

(deftest equivalent?-works-on-sets
  (is (equivalent? #{1 2 3} #{1 2 3}))
  (is (equivalent? #{1 2 3} #{3 2 1}))
  (is (equivalent? #{1 2 3} #{1 2 3 5}
                   :strict? false))
  (is (equivalent? #{[1 2] 3} #{3 5 '(1 2 3) 2}
                   :strict? false))
  (is (not (equivalent? #{1 2 3} #{1 2 3 5})))
  (is (not (equivalent? #{[1 2] 3} #{3 5 '(1 2 3) 2})))
  (is (not (equivalent? #{[1 2] 3} #{3 5 '(1 3) 2})))
  (is (not (equivalent? #{[1 2] [3 4]} #{[1 2 3 4] 3 4})))
  (is (equivalent? #{1 2 3} [2 3 1]))
  (is (equivalent? #{1 2 3} '(2 3 1)))
  (is (equivalent? #{1 2 3} (range 1 4))))

(deftest equivalent?-works-on-nested-structures
  (is (equivalent? {:first-name "Jorge"
                    :middle-name "Ricardo"
                    :last-name "Moran"
                    :birth-date {:year 1998, :month 4, :day 7}
                    :addresses #{"Avenida Siempreviva 742"
                                 "221B Baker Street"}}
                   {:first-name "Jorge"
                    :middle-name "Ricardo"
                    :last-name "Moran"
                    :nick-names ["Richo" "Richi" "OCHO"]
                    :birth-date {:year 1998, :month 4, :day 7, :day-of-week "Thursday"}
                    :addresses ["Avenida Siempreviva 742"
                                "Privet Drive 4"
                                "221B Baker Street"]}
                   :strict? false))
  (is (not (equivalent? {:first-name "Jorge"
                         :middle-name "Ricardo"
                         :last-name "Moran"
                         :birth-date {:year 1998, :month 4, :day 7}
                         :addresses #{"Avenida Siempreviva 742"
                                      "221B Baker Street"}}
                        {:first-name "Jorge"
                         :middle-name "Ricardo"
                         :last-name "Moran"
                         :birth-date {:year 1998, :month 4, :day 6}
                         :addresses ["Avenida Siempreviva 742"
                                     "221B Baker Street"]})))
  (is (not (equivalent? {:first-name "Jorge"
                         :middle-name "Ricardo"
                         :last-name "Moran"
                         :birth-date {:year 1998, :month 4, :day 7}
                         :addresses #{"Avenida Siempreviva 742"
                                      "221B Baker Street"}}
                        {:first-name "Jorge"
                         :middle-name "Ricardo"
                         :last-name "Moran"
                         :nick-names ["Richo" "Richi" "OCHO"]
                         :birth-date {:year 1998, :month 4, :day 7, :day-of-week "Thursday"}
                         :addresses ["Avenida Siempreviva 742"
                                     "Privet Drive 4"
                                     "221B Baker Street"]}))))
