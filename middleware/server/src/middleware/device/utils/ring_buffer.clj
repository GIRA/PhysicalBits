(ns middleware.device.utils.ring-buffer)

(defn- make-array* [size]
  ; TODO(Richo): This would probably be faster using a java array, but for now it works
  (apply vector-of :double (repeat size 0)))

(defn make-ring-buffer [size]
  (atom {:array (make-array* size), :index 0}))

(defn push! [ring-buffer new]
  (swap! ring-buffer
         (fn [{:keys [array index] :as rb}]
           (-> rb
               (update :array #(assoc % (rem index (count array)) new))
               (update :index #(rem (inc %) (count array)))))))

(defn avg ^double [ring-buffer]
  (let [{:keys [array full?]} @ring-buffer
        len (count array)]
    (if (zero? len) 0 (/ (reduce + array) len))))

(comment

 (def ring-buffer (make-ring-buffer 10))

 (double (avg ring-buffer))
 (push! ring-buffer 20.5)
 (time (dotimes [_ 10000]
                (push! ring-buffer (rand-int 100))))
 (:array @ring-buffer)

 ,)
