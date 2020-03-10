(ns plugin.parser.binary-operator)

(def operator-precedence ["**"
                          "*"
                          "/"
                          "%"
                          "+"
                          "-"
                          "<<"
                          ">>"
                          "<"
                          "<="
                          ">"
                          ">="
                          "=="
                          "!="
                          "&"
                          "^"
                          "|"
                          "&&"
                          "||"])


(defn- contains-value? [col value] (some #(= value %) col))

(defn first-operator [expr]
  (if (< (count expr) 3)
    nil
    (let [operators (take-nth 2 (rest expr))]
      (or
        (first (filter #(not (contains-value? operator-precedence %)) operators))
        (first (filter #(contains-value? operators %) (reverse operator-precedence)))
        nil
        ))))

(defn build-binary-expression
  [parts]
  (let [operator (first-operator parts)]
    (if (nil? operator)
      (if (= 1 (count parts))
        (first parts)
        parts)
      (let [[left [op & right]] (split-at (.lastIndexOf parts operator) parts)]
        [:binaryExpr (build-binary-expression left) op (build-binary-expression right)]
        )
      )))
