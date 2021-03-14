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
             (pp/star [(pp/or :variable-declaration
                              :primitive
                              :script
                              :ws)])
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
   :ticking-rate [(pp/or :float :integer)
                  :ws? "/" :ws?
                  (pp/or "s" "m" "h" "d")]
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
   :expression-statement [:expr :endl]
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
   :constant [(pp/or "D" "A") :integer]
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
            :tick-rate ticking-rate
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
   :sub-expr (fn [[_ expr _]] expr)
   :constant (fn [[letter number]]
               (ast/literal-pin-node letter number))
   :arg-list (fn [[_ _ args _ _]]
               (take-nth 2 args))
   :named-arg (fn [[[name] value]]
                (ast/arg-node name value))
   :call (fn [[selector _ args]]
           (ast/call-node selector args))
   :ticking-rate (fn [[times _ _ _ scale]]
                   (ast/ticking-rate-node times scale))
   :expression-statement (fn [[expr]] expr)})

(def parser (pp/compose grammar transformations :program))

(defn parse [src]
  (let [result (pp/parse parser src)]
    ;(pprint result)
    result))

(comment

 (take-nth 2 [1 2 3])

 (do
   (def parser (pp/compose grammar transformations :program))
   (def src "task default() running 1/s {\n\ttoggle(D13);\n}")
   (def expected (ast/program-node
                  :scripts [(ast/task-node
                             :name "default"
                             :tick-rate (ast/ticking-rate-node 1 "s")
                             :state "running"
                             :body (ast/block-node
                                    [(ast/call-node
                                      "toggle"
                                      [(ast/arg-node
                                        (ast/literal-pin-node "D" 13))])]))]))
   (def actual (pp/parse parser src))
   (def diff (data/diff expected actual))
   (println "ONLY IN EXPECTED")
   (pprint (first diff))
   (println)
   (println "ONLY IN ACTUAL")
   (pprint (second diff))
   (println)
   (println "T")
   (pprint (nth diff 2))
   (= expected actual))

 (pprint actual)
 (pprint expected)

 (pp/parse (get-in parser [:parsers :ticking-rate]) "1 / s")


 (pprint (pp/parse (get-in parser [:parsers :task]) "task default() running 1/s {}"))

 (Double/parseDouble "Infinity")
 ,,,)
