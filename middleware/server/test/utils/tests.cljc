(ns utils.tests
  (:require [clojure.core.async :as a]
            [clojure.string :as str]
            #?(:clj [clojure.test :refer [*testing-vars*]]
               :cljs [cljs.test  :refer [get-current-env] :refer-macros [async]])
            [middleware.utils.fs.common :as fs]
            #?(:clj [middleware.utils.fs.jio :as jio])
            #?(:cljs [middleware.utils.fs.browser :as browser])
            #?(:cljs [middleware.utils.fs.node :as node])))

; NOTE(Richo): https://stackoverflow.com/a/30781278
(defn test-async
  "Asynchronous test awaiting ch to produce a value or close."
  [ch]
  #?(:clj (a/<!! ch)
    :cljs (async done
                 (a/take! ch (fn [_] (done))))))

(defn test-name []
  #?(:clj (str/join "." (map (comp :name meta) *testing-vars*))
    :cljs (str/join "." (map (comp :name meta) (:testing-vars (get-current-env))))))

(defn init-dependencies []
  #?(:clj (fs/register-fs! #'jio/file)
    :cljs (fs/register-fs! (if (node/available?)
                             #'node/file
                             #'browser/file))))

(def setup-fixture
  #?(:clj (fn [f] (init-dependencies) (f))
    :cljs {:before init-dependencies}))

(comment ; Useful snippet to run the test on a specific namespace and output to a file
  #?(:clj
     
     
     
     (with-open [writer (clojure.java.io/writer "test.out")]
       (binding [clojure.test/*test-out* writer]
         (clojure.test/run-tests 'middleware.compiler-autogen-test)
         #_(clojure.test/run-all-tests)))
     
     ))