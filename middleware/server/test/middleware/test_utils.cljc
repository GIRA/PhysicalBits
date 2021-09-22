(ns middleware.test-utils
  (:require [clojure.walk :as w]
            [clojure.data :as data]
            [clojure.core.async :as a]
            [clojure.string :as str]
            #?(:clj [clojure.test :refer [*testing-vars*]]
               :cljs [cljs.test  :refer [get-current-env] :refer-macros [async]])
            [middleware.utils.fs.common :as fs]
            #?(:clj [middleware.utils.fs.jio :as jio])
            #?(:cljs [middleware.utils.fs.browser :as browser])
            #?(:cljs [middleware.utils.fs.node :as node])))

(defn without-internal-ids [f]
  (w/postwalk #(if (map? %)
                 (dissoc % :internal-id)
                 %)
              f))

; TODO(Richo): Change implementation for sets and vectors so that it checks equality
(defn equivalent? [a b]
  (-> (data/diff a b) first nil?))

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
