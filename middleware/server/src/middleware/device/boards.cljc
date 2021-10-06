(ns middleware.device.boards
  (:require [middleware.utils.core :refer [index-of]]))

(def UNO {:name "Arduino UNO"
          :pin-names ["D2" "D3" "D4" "D5" "D6" "D7" "D8" "D9" "D10" "D11" "D12" "D13"
                      "A0" "A1" "A2" "A3" "A4" "A5"]
          :pin-numbers [2 3 4 5 6 7 8 9 10 11 12 13
                        14 15 16 17 18 19]})

(def Nano {:name "Arduino Nano"
           :pin-names ["D2" "D3" "D4" "D5" "D6" "D7" "D8" "D9" "D10" "D11" "D12" "D13"
                       "A0" "A1" "A2" "A3" "A4" "A5" "A6" "A7"]
           :pin-numbers [2 3 4 5 6 7 8 9 10 11 12 13
                         14 15 16 17 18 19 20 21]})

(def get-pin-name
  (memoize (fn [pin-number & [board]]
             (let [board (or board UNO)
                   ^java.util.List pin-numbers (board :pin-numbers)
                   index (index-of pin-numbers pin-number)]
               (if-not (= -1 index)
                 (nth (board :pin-names) index)
                 nil)))))

(def get-pin-number
  (memoize (fn [pin-name & [board]]
             (let [board (or board UNO)
                   ^java.util.List pin-names (board :pin-names)
                   index (index-of pin-names pin-name)]
               (if-not (= -1 index)
                 (nth (board :pin-numbers) index)
                 nil)))))
