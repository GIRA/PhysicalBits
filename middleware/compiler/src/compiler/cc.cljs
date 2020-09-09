(ns compiler.cc
  (:require [clojure.string :as str]))

(defn ^:export compile [src]
  (str/upper-case src))
