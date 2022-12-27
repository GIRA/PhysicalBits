(ns utils.compilation
  (:require [middleware.ast.nodes :as ast]
            [middleware.program.utils :as program]
            [clojure.set :as set]
            [middleware.compilation.compiler :as cc]
            [middleware.program.emitter :as emit]))


; NOTE(Richo): I compile an empty program without removing the dead-code so that we
; get all the core.uzi scripts
(def core-scripts
  (memoize (fn [& [lib-dir]]
             (:scripts (cc/compile-tree (ast/program-node)
                                        :lib-dir (or lib-dir "../../uzi/libraries")
                                        :remove-dead-code? false)))))

(defn link-core [program]
  (let [globals (:globals program)
        called-scripts (set/difference (->> (program/instructions program)
                                            (filter program/script-call?)
                                            (map :argument)
                                            (set))
                                       (set (map :name (:scripts program))))
        scripts (vec (concat (filter #(contains? called-scripts (:name %))
                                     (core-scripts))
                             (:scripts program)))]
    (emit/program
     :globals globals
     :scripts scripts)))

(defn emit-program [& {:keys [globals scripts]
                       :or {globals [] scripts []}}]
  (link-core (emit/program :globals globals
                           :scripts scripts)))