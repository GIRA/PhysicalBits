(ns plugin.encoder-test
  (:require [clojure.test :refer :all]
            [plugin.compiler.core :as cc]
            [plugin.compiler.encoder :as en]))

(defn compile [src]
  (cc/compile-uzi-string src))

(defn encode [src]
  (-> src
      compile
      en/encode))

(deftest empty-program
  (let [expected [0 0]
        actual (encode "")]
    (is (= expected actual))))
