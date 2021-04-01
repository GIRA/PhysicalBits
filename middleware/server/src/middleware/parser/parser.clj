(ns middleware.parser.parser
  (:require [middleware.parser.ast-nodes :as ast]
            [petitparser.core :as pp]))

(def TODO (pp/predicate (fn [_] false) "NOP!"))

(def grammar
  {:start (pp/end :program)
   :program [:ws?
             (pp/star :import)
             (pp/star (pp/or :variable-declaration
                              :primitive
                              :script
                              :ws))
             :ws?]
   :import ["import" :ws
            (pp/optional [:identifier :ws "from" :ws])
            :import-path (pp/or :endl [:ws? :block]) :ws?]
   :import-path ["'" (pp/flatten (pp/star (pp/negate "'"))) "'"]
   :variable-declaration ["var" :ws :variable (pp/optional [:ws? \= :ws? :expr :ws?]) :endl :ws?]
   :primitive ["prim" :ws
               (pp/optional [(pp/or :binary-selector :identifier) :ws \: :ws])
               :identifier :endl]
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
   :conditional ["if" :separated-expr :ws? :block (pp/optional [:ws? "else" :ws? :block])]
   :start-task ["start" :ws :script-list :endl]
   :stop-task ["stop" :ws :script-list :endl]
   :pause-task ["pause" :ws :script-list :endl]
   :resume-task ["resume" :ws :script-list :endl]
   :script-list (pp/separated-by :script-reference
                                 [:ws? \, :ws?])
   :while ["while" :separated-expr :ws? (pp/or :block :endl) :ws?]
   :do-while ["do" :ws? :block :ws? "while" :separated-expr :endl]
   :until ["until" :separated-expr :ws? (pp/or :block :endl) :ws?]
   :do-until ["do" :ws? :block :ws? "until" :separated-expr :endl]
   :repeat ["repeat" :separated-expr :ws? :block]
   :forever ["forever" :ws? :block]
   :for ["for" :ws :variable :ws? \= :ws? :expr :ws
         "to" :separated-expr
         (pp/optional [:ws "by" :separated-expr])
         :ws? :block]
   :yield ["yield" :endl]
   :expression-statement [:expr :endl]
   :endl [:ws? \;]
   :separated-expr (pp/or [:ws :expr]
                          [:ws? :sub-expr])
   :sub-expr [\(
              :ws? :expr :ws?
              \)]
   :expr (pp/or :binary-expr :non-binary-expr )
   :non-binary-expr (pp/or :unary :literal :call :variable :sub-expr)
   :binary-expr [:non-binary-expr :ws?
                 (pp/plus [:binary-selector :ws? :non-binary-expr :ws?])]
   :binary-selector (pp/flatten
                     (pp/plus
                      (pp/predicate
                       (fn [chr] ; TODO(Richo): Maybe avoid regex? Measure performance!
                         (re-find #"[^a-zA-Z0-9\s\[\]\(\)\{\}\"\':#_;,]"
                                  (str chr)))
                       "Not binary")))
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
   :not [\! :ws? :non-binary-expr]
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

   ; TODO(Richo): Don't lose the comments
   :ws (pp/plus (pp/or pp/space :comment))
   :ws? (pp/optional :ws)
   :comment (pp/flatten ["\"" (pp/flatten (pp/star (pp/negate "\""))) "\""])})

(defn- parse-int [str] (Integer/parseInt str))
(defn- parse-double [str] (Double/parseDouble str))

; TODO(Richo): This should probably be in a utils.ast namespace
(defn- script? [node]
  (contains? #{"UziTaskNode" "UziProcedureNode" "UziFunctionNode"}
             (:__class__ node)))

(defn- variable-declaration? [node]
  (= "UziVariableDeclarationNode" (:__class__ node)))

(defn- primitive? [node]
  (= "UziPrimitiveDeclarationNode" (:__class__ node)))

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


; TODO(Richo): Refactor this, maybe with a threaded macro or something...
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
               :scripts (filterv script? members)
               :primitives (filterv primitive? members)))
   :import (fn [[_ _ [alias] path [_ init-block]]]
             (if alias
               (if init-block
                 (ast/import-node alias path init-block)
                 (ast/import-node alias path))
               (ast/import-node path)))
   :import-path (fn [[_ path _]] path)
   :task (fn [[_ _ name _ args _ state _ ticking-rate _ body]]
           (ast/task-node
            :name name
            :state state
            :arguments args
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
   :while (fn [[_ condition _ body]]
            (ast/while-node condition (or body (ast/block-node []))))
   :until (fn [[_ condition _ body]]
            (ast/until-node condition (or body (ast/block-node []))))
   :do-while (fn [[_ _ body _ _ condition]]
               (ast/do-while-node condition body))
   :do-until (fn [[_ _ body _ _ condition]]
               (ast/do-until-node condition body))
   :endl (constantly nil)
   :conditional (fn [[_ condition _ true-branch [_ _ _ false-branch]]]
                  (ast/conditional-node condition
                                        true-branch
                                        (or false-branch (ast/block-node []))))
   :forever (fn [[_ _ body]]
              (ast/forever-node body))
   :repeat (fn [[_ times _ body]]
             (ast/repeat-node times body))
   :yield (constantly (ast/yield-node))
   :script-list (partial take-nth 2)
   :start-task (fn [[_ _ tasks _]]
                 (ast/start-node (vec tasks)))
   :stop-task (fn [[_ _ tasks _]]
                 (ast/stop-node (vec tasks)))
   :pause-task (fn [[_ _ tasks _]]
                 (ast/pause-node (vec tasks)))
   :resume-task (fn [[_ _ tasks _]]
                 (ast/resume-node (vec tasks)))
   :not (fn [[_ _ expr]]
          (ast/call-node "!" [(ast/arg-node expr)]))
   :primitive (fn [[_ _ [alias] name]]
                (if alias
                  (ast/primitive-node alias name)
                  (ast/primitive-node name)))
   })

(def parser (pp/compose grammar transformations))

(defn parse [src]
  (let [result (pp/parse parser src)]
    ;(pprint result)
    result))

(comment

 (do
   (require '[clojure.pprint :refer [pprint]])
   (require '[clojure.data :as data])
   (set! *print-level* 50)
   (set! *print-length* 100))

 (do
   (def parser (pp/compose grammar transformations))
   (def src "task foo() { return (3) + 4; }")
   (def expected (ast/program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/call-node
                                       "+"
                                       [(ast/arg-node (ast/literal-number-node 3))
                                        (ast/arg-node (ast/literal-number-node 4))]))]))]))
   (def actual (pp/parse parser src))
   (def diff (data/diff expected actual))
   (println "ONLY IN EXPECTED")
   (pprint (first diff))
   (println)
   (println "ONLY IN ACTUAL")
   (pprint (second diff))
   (println)
   (println "IN BOTH")
   ;(pprint (nth diff 2))
   (= expected actual))

 (pprint actual)
 (pprint expected)


 (pprint (pp/parse (get-in parser [:parsers :return])
                   "return (3) + 4;"))

 (pp/parse (pp/or (pp/transform pp/letter (constantly 4))
                  (pp/transform "a" (constantly 1))
                  (pp/transform "b" (constantly 2))
                  (pp/transform "c" (constantly 3)))
           "a")

 (pprint (pp/parse (get-in parser [:parsers :non-binary-expr])
                   "4 ~ 2"))

 (pprint (pp/parse (get-in parser [:parsers :binary-selector])
                   " "))

 (pprint (pp/parse (get-in parser [:parsers :primitive])
                   "prim add;"))

 (pprint (pp/parse (get-in parser [:parsers :script-list])
                   "blink13, loop, a, b;"))

(pprint (parse "func map(value, fromLow, fromHigh, toLow, toHigh) {
  return (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow) + toLow;
}"))

 ,,,)
