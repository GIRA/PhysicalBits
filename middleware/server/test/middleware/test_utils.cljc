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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(declare equivalent?)

(defn- equivalent-sequential? [a b strict?]
  (try
    (when (sequential? b)
      (and (or (not strict?)
               (= (count a) (count b)))
           (every? (fn [[i v]]
                     (equivalent? v (nth b i)
                                  :strict? strict?))
                   (map-indexed (fn [i e] [i e]) a))))
    (catch #?(:clj Throwable :cljs :default) _ false)))

(defn- equivalent-map? [a b strict?]
  (when (associative? b)
    (and (or (not strict?)
             (= (count a) (count b)))
         (every? (fn [[k v]]
                   (equivalent? v (get b k)
                                :strict? strict?))
                 a))))

(defn- equivalent-set? [a b strict?]
  (and (or (not strict?)
           (= (count a) (count b)))
       (every? (if (set? b)
                 (fn [v]
                   (or (contains? b v)
                       (some (fn [v'] (equivalent? v v' :strict? strict?))
                             b)))
                 (fn [v]
                   (some (fn [v'] (equivalent? v v' :strict? strict?))
                         b)))
               a)))

(defn equivalent?
  [a b &{:keys [strict?] :or {strict? true}}]
  (or (identical? a b)
      (= a b)
      (cond
        (sequential? a) (equivalent-sequential? a b strict?)
        (map? a) (equivalent-map? a b strict?)
        (set? a) (equivalent-set? a b strict?)
        :else false)))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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
