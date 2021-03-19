(ns middleware.parser.parser
  (:require [middleware.parser.ast-nodes :as ast]
            [petitparser.core :as pp]))

(do
  (require '[clojure.pprint :refer [pprint]])
  (require '[clojure.data :as data])
  (set! *print-level* 20)
  (set! *print-length* 100))

(def TODO (pp/predicate (fn [_] false) "NOP!"))

(def grammar
  {:program [:ws?
             (pp/star :import)
             (pp/star (pp/or :variable-declaration
                              :primitive
                              :script
                              :ws))
             :ws?]
   :import TODO
   :variable-declaration ["var" :ws :variable (pp/optional [:ws? \= :ws? :expr :ws?]) :endl :ws?]
   :primitive TODO
   :script (pp/or :task :function :procedure)
   :task ["task" :ws
          :identifier :ws?
          :params-list :ws?
          :task-state :ws?
          (pp/optional :ticking-rate) :ws?
          :block]
   :function ["func" :ws :identifier :ws? :params-list :ws? :block]
   :procedure ["proc" :ws :identifier :ws? :params-list :ws? :block]
   :identifier (pp/flatten
                (pp/separated-by [(pp/or \_ pp/letter)
                                  (pp/star (pp/or \_ pp/word))]
                                 \.))
   :params-list [\(
                 :ws?
                 (pp/optional
                  (pp/separated-by :argument
                                   [:ws? \, :ws?]))
                 :ws?
                 \)]
   :argument :identifier
   :task-state (pp/optional (pp/or "running" "stopped"))
   :ticking-rate [(pp/or :float :integer)
                  :ws? "/" :ws?
                  (pp/or "s" "m" "h" "d")]
   :block ["{" :statement-list "}"]
   :statement-list [:ws? (pp/star :statement)]
   :statement [(pp/or :variable-declaration :assignment :return :conditional
                     :start-task :stop-task :pause-task :resume-task
                     :while :do-while :until :do-until :repeat :forever :for
                     :yield :expression-statement)
               :ws?]
   :assignment [:variable :ws? \= :ws? :expr :endl]
   :return ["return" (pp/optional :separated-expr) :endl]
   :conditional TODO
   :start-task TODO
   :stop-task TODO
   :pause-task TODO
   :resume-task TODO
   :while TODO
   :do-while TODO
   :until TODO
   :do-until TODO
   :repeat TODO
   :forever TODO
   :for ["for" :ws :variable :ws? \= :ws? :expr :ws
         "to" :separated-expr
         (pp/optional [:ws "by" :separated-expr])
         :ws? :block]
   :yield TODO
   :expression-statement [:expr :endl]
   :endl [:ws? \;]
   :separated-expr (pp/or [:ws? :sub-expr]
                          [:ws :expr])
   :sub-expr [\(
              :ws? :expr :ws?
              \)]
   :expr (pp/or :binary-expr :non-binary-expr )
   :non-binary-expr (pp/or :unary :call :sub-expr :value-expr)
   :binary-expr [:non-binary-expr :ws?
                 (pp/plus [:binary-selector :ws? :non-binary-expr :ws?])]
   :binary-selector (pp/flatten
                     (pp/plus
                      (pp/predicate
                       (fn [chr] ; TODO(Richo): Maybe avoid regex? Measure performance!
                         (re-find #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]"
                                  (str chr)))
                       "Not binary")))
   :value-expr (pp/or :literal :variable)
   :literal (pp/or :constant :number)
   :constant [(pp/or "D" "A") :integer]
   :number (pp/or :float :integer)
   :float (pp/flatten
           (pp/or "NaN"
                  "-Infinity"
                  "Infinity"
                  [(pp/optional \-) :digits \. :digits]))
   :digits (pp/plus pp/digit)
   :integer (pp/flatten [(pp/optional \-) :digits])
   :variable :identifier
   :unary :not
   :not TODO
   :call [:script-reference :ws? :arg-list]
   :script-reference :identifier
   :arg-list [\(
              :ws?
              (pp/optional
               (pp/separated-by :named-arg
                                [:ws? \, :ws?]))
              :ws?
              \)]
   :named-arg [(pp/optional [:identifier :ws? \:
                             :ws? ; TODO(Richo): Is this necessary?
                             ])
               :expr]
   :ws (pp/plus pp/space)
   :ws? (pp/optional :ws)})

(defn- parse-int [str] (Integer/parseInt str))
(defn- parse-double [str] (Double/parseDouble str))

; TODO(Richo): This should probably be in a utils.ast namespace
(defn- script? [node]
  (contains? #{"UziTaskNode" "UziProcedureNode" "UziFunctionNode"}
             (:__class__ node)))

(defn- variable-declaration? [node]
  (= "UziVariableDeclarationNode" (:__class__ node)))

(def precedence-table
  [#{"**"}
   #{"*" "/" "%"}
   #{"+" "-"}
   #{"<<" ">>"}
   #{"<" "<=" ">" ">="}
   #{"==" "!="}
   #{"&"}
   #{"^"}
   #{"|"}
   #{"&&"}
   #{"||"}])

(defn- build-binary-expression [selector left right]
  [selector left right]
  (case selector
    "&&" (ast/logical-and-node left right)
    "||" (ast/logical-or-node left right)
    (ast/binary-expression-node left selector right)))

(defn- fix-precedence [nodes operators]
  (if (< (count nodes) 3)
    nodes
    (let [result (atom [])
          left (atom (first nodes))]
      (doseq [[op right] (partition-all 2 (drop 1 nodes))]
        (if (or (nil? operators)
                (contains? operators op))
          (let [expr (build-binary-expression op @left right)]
            ; TODO(Richo): Add token!
            (reset! left expr))
          (do
            (swap! result conj @left op)
            (reset! left right))))
      (swap! result conj @left)
      @result)))


(defn- reduce-binary-expresions [left operations]
  (let [; First, flatten the token value so that instead of (1 ((+ 2) (+ 3)))
        ; we have (1 + 2 + 3)
        result-1 (reduce #(apply conj %1 %2) [left] operations)
        ; Then reduce operators according to the precedence table
        result-2 (reduce fix-precedence result-1 precedence-table)
        ; If a binary expression was not reduced after going through the precedence	table,
        ; we iterate once again but this time reducing any operator found (left to right)
        result-3 (fix-precedence result-2 nil)]
    ; Finally, we should have a single expression in our results
    (first result-3)))

(def transformations
  {:program (fn [[_ imports members _]]
              (ast/program-node
               :imports imports
               :globals (filterv variable-declaration? members)
               :scripts (filterv script? members)))
   :task (fn [[_ _ name _ args _ state _ ticking-rate _ body]]
           (ast/task-node
            :name name
            :state state
            :args args
            :tick-rate ticking-rate
            :body body))
   :function (fn [[_ _ name _ args _ body]]
                (ast/function-node
                 :name name
                 :arguments args
                 :body body))
   :procedure (fn [[_ _ name _ args _ body]]
                (ast/procedure-node
                 :name name
                 :arguments args
                 :body body))
   :block (fn [[_ stmts _]]
            (ast/block-node stmts))
   :statement-list (fn [[_ stmts]] stmts)
   :statement (fn [[stmt _]] stmt)
   :integer (comp parse-int str)
   :float (comp parse-double str)
   :number ast/literal-number-node
   :return (fn [[_ value _]]
             (ast/return-node value))
   :separated-expr (fn [[_ expr]] expr)
   :sub-expr (fn [[_ _ expr _ _]] expr)
   :constant (fn [[letter number]]
               (ast/literal-pin-node letter number))
   :arg-list (fn [[_ _ args _ _]]
               (vec (take-nth 2 args)))
   :params-list (fn [[_ _ args _ _]]
                  (vec (take-nth 2 args)))
   :named-arg (fn [[[name] value]]
                (ast/arg-node name value))
   :call (fn [[selector _ args]]
           (ast/call-node selector args))
   :ticking-rate (fn [[times _ _ _ scale]]
                   (ast/ticking-rate-node times scale))
   :expression-statement (fn [[expr]] expr)
   :variable ast/variable-node
   :argument ast/variable-declaration-node
   :binary-expr (fn [[left _ ops]]
                  (let [operations (map (fn [[op _ right]] [op right])
                                        ops)]
                    (reduce-binary-expresions left operations)))
   :variable-declaration (fn [[_ _ var [_ _ _ expr]]]
                           (ast/variable-declaration-node
                            (:name var)
                            (or expr (ast/literal-number-node 0))))
   :for (fn [[_ _ var _ _ _ start _ _ stop [_ _ step] _ body]]
          (ast/for-node (:name var)
                        start
                        stop
                        (or step (ast/literal-number-node 1))
                        body))
   :assignment (fn [[variable _ _ _ value]]
                 (ast/assignment-node variable value))
   })

(def parser (pp/compose grammar transformations :program))

(defn parse [src]
  (let [result (pp/parse parser src)]
    ;(pprint result)
    result))

(comment

 (take-nth 2 [1 2 3])

 (do
   (def parser (pp/compose grammar transformations :program))
   (def src "\ntask blink13() running 2/s {\nreturn a * b/c**d+n ~ j ** 3;\n } ")
   (def expected (ast/program-node
                  :scripts [(ast/task-node
                             :name "blink13"
                             :tick-rate (ast/ticking-rate-node 2 "s")
                             :state "running"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/call-node
                                       "~"
                                       [(ast/arg-node
                                         (ast/call-node
                                          "+"
                                          [(ast/arg-node
                                            (ast/call-node
                                             "/"
                                             [(ast/arg-node
                                               (ast/call-node
                                                "*"
                                                [(ast/arg-node
                                                  (ast/variable-node
                                                   "a"))
                                                 (ast/arg-node
                                                  (ast/variable-node
                                                   "b"))]))
                                              (ast/arg-node
                                               (ast/call-node
                                                "**"
                                                [(ast/arg-node
                                                  (ast/variable-node
                                                   "c"))
                                                 (ast/arg-node
                                                  (ast/variable-node
                                                   "d"))]))]))
                                           (ast/arg-node
                                            (ast/variable-node "n"))]))
                                        (ast/arg-node
                                         (ast/call-node
                                          "**"
                                          [(ast/arg-node
                                            (ast/variable-node "j"))
                                           (ast/arg-node
                                            (ast/literal-number-node
                                             3))]))]))]))]))
   (def actual (pp/parse parser src))
   (def diff (data/diff expected actual))
   (println "ONLY IN EXPECTED")
   (pprint (first diff))
   (println)
   (println "ONLY IN ACTUAL")
   (pprint (second diff))
   (println)
   (println "IN BOTH")
   (pprint (nth diff 2))
   (= expected actual))

 (pprint actual)
 (pprint expected)

 (pprint (parse "var b;func foo() {}"))

 (pprint (pp/parse (get-in parser [:parsers :expr])
                   "a * b/c**d+n ~ j ** 3"))

 (pprint (pp/parse (get-in parser [:parsers :non-binary-expr])
                   "4 ~ 2"))

 (pprint (pp/parse (get-in parser [:parsers :binary-selector])
                   " "))

 (pprint (pp/parse (get-in parser [:parsers :binary-expr])
                   "3 ~ 4"))

 (pp/parse (pp/flatten (pp/plus ["~" (pp/star pp/space) pp/digit]))
           "~ 4 ~ 5")


 ,,,)
