(ns middleware.device.utils.ring-buffer)

(defn- make-array* [size]
  ; TODO(Richo): This would probably be faster using a native array, but for now it works
  #?(:clj (apply vector-of :double (repeat size 0))
     :cljs (vec (repeat size 0))))

(defn make-ring-buffer [size]
  (atom {:array (make-array* size), :index 0}))

(defn push! [ring-buffer ^double new]
  (swap! ring-buffer
         (fn [{:keys [array ^long index] :as rb}]
           (let [size (count array)]
             (-> rb
                 (update :array (fn [^doubles arr] (assoc arr (rem index size) new)))
                 (update :index (fn [^long i] (rem (inc i) size))))))))

(defn- sum ^double [array]
  (reduce + array))

(defn avg ^double [ring-buffer]
  (let [{:keys [array]} @ring-buffer
        len (count array)]
    (if (zero? len) 0 (/ (sum array) len))))

(comment

 (def ring-buffer (make-ring-buffer 10))

 (double (avg ring-buffer))
 (push! ring-buffer 20.5)
 (time (dotimes [_ 10000]
                (push! ring-buffer (rand-int 100))))
 (:array @ring-buffer)

 ,)
