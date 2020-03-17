(ns plugin.dead-code-remover-test
  (:require [clojure.test :refer :all]
            [clojure.walk :as w]
            [plugin.compiler.ast-utils :as ast-utils]
            [plugin.compiler.core :as cc])
  (:use [plugin.test-utils]))

(defn compile [src]
  (cc/compile-uzi-string src))

(deftest stopped-task-with-no-refs-should-be-removed
  (let [expected (compile "task alive() running { toggle(D13); }")
        actual (compile "task alive() running { toggle(D13); }
		                     task dead() stopped { toggle(D12); }")]
    (is (= expected actual))))
