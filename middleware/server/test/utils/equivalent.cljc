(ns utils.equivalent)

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
