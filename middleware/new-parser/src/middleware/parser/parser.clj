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
             (pp/star [(pp/or :variable-declaration :primitive :script :ws)])
             :ws?]
   :import TODO
   :variable-declaration TODO
   :primitive TODO
   :script (pp/or :task :function :procedure)
   :task ["task" :ws
          :identifier :ws?
          :params-list :ws?
          :task-state :ws?
          (pp/optional :ticking-rate) :ws?
          :block]
   :identifier (pp/flatten
                (pp/separated-by [(pp/or \_ pp/letter)
                                  (pp/star (pp/or \_ pp/word))]
                                 \.))
   :params-list [\(
                 (pp/optional [:ws?
                               :argument
                               (pp/star [:ws? \, :ws? :argument])])
                 :ws?
                 \)]
   :argument :identifier
   :task-state (pp/optional (pp/or "running" "stopped"))
   :ticking-rate TODO
   :block ["{" :statement-list "}"]
   :statement-list [:ws? (pp/star :statement) :ws?]
   :statement (pp/or :variable-declaration :assignment :return :conditional
                     :start-task :stop-task :pause-task :resume-task
                     :while :do-while :until :do-until :repeat :forever :for
                     :yield :expression-statement)
   :assignment TODO
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
   :for TODO
   :yield TODO
   :expression-statement TODO
   :function TODO
   :procedure TODO
   :endl [:ws? \;]
   :separated-expr (pp/or [:ws? :sub-expr]
                          [:ws :expr])
   :sub-expr [\(
              :ws? :expr :ws?
              \)]
   :expr (pp/or :non-binary-expr :binary-expr)
   :non-binary-expr (pp/or :unary :call :sub-expr :value-expr)
   :binary-expr [:non-binary-expr :ws?
                 (pp/plus [:binary-selector :ws? :non-binary-expr :ws?])]
   :binary-selector (pp/flatten
                     (pp/plus
                      (pp/predicate
                       (fn [chr] ; TODO(Richo): Maybe avoid regex? Measure performance!
                         (re-find #"[^a-zA-Z0-9\\s\\[\\]\\(\\)\\{\\}\\\"\\':#_;,]"
                                  (str chr)))
                       "Not binary")))
   :value-expr (pp/or :literal :variable)
   :literal (pp/or :constant :number)
   :constant TODO
   :number (pp/or :float :integer)
   :float (pp/flatten
           (pp/or "NaN"
                  "-Infinity"
                  "Infinity"
                  [(pp/optional \-) :digits \. :digits]))
   :digits (pp/plus pp/digit)
   :integer (pp/flatten [(pp/optional \-) :digits])
   :variable TODO
   :unary TODO
   :call TODO
   :ws (pp/plus pp/space)
   :ws? (pp/optional :ws)})

(defn- parse-int [str] (Integer/parseInt str))
(defn- parse-double [str] (Double/parseDouble str))

; TODO(Richo): This should probably be in a utils.ast namespace
(defn- script? [node]
  (contains? #{"UziTaskNode" "UziProcedureNode" "UziFunctionNode"}
             (:__class__ node)))

(def transformations
  {:program (fn [[_ imports [members] _]]
              (ast/program-node
               :imports imports
               :scripts (filterv script? members)))
   :task (fn [[_ _ name _ args _ state _ ticking-rate _ body]]
           (ast/task-node
            :name name
            :state state
            :args args
            :ticking-rate ticking-rate
            :body body))
   :block (fn [[_ stmts _]]
            (ast/block-node stmts))
   :statement-list (fn [[_ stmts _]] stmts)
   :integer (comp parse-int str)
   :float (comp parse-double str)
   :number ast/literal-number-node
   :return (fn [[_ value _]]
             (ast/return-node value))
   :separated-expr (fn [[_ expr]] expr)
   :sub-expr (fn [[_ expr _]] expr)})

(def parser (pp/compose grammar transformations :program))

(defn parse [src]
  (let [result (pp/parse parser src)]
    ;(pprint result)
    result))

(comment


 (do
   (def parser (pp/compose grammar transformations :program))
   (def src "task foo() { return -0.5; }")
   (def expected (ast/program-node
                  :scripts [(ast/task-node
                             :name "foo"
                             :state "once"
                             :body (ast/block-node
                                    [(ast/return-node
                                      (ast/literal-number-node -0.5))]))]))
   (def actual (pp/parse parser src))
   (def diff (data/diff expected actual))
   (println "ONLY IN EXPECTED")
   (pprint (first diff))
   (println)
   (println "ONLY IN ACTUAL")
   (pprint (second diff)))

 (pprint actual)
 (pprint expected)

 (pp/parse (get-in parser [:parsers :task]) "task foo () { return 0.1;}")

 (pp/parse (get-in parser [:parsers :block]) "{ return -0.1; }")

 (Double/parseDouble "Infinity")
 ,,,)
