(ns middleware.device.debugger
  (:require [clojure.set :as set]
            [clojure.core.async :as a :refer [<! >! go go-loop timeout]]
            [middleware.program.utils :as program]
            [middleware.device.protocol :as p]
            [middleware.utils.conversions :as conversions]
            [middleware.utils.core :refer [seek]]
            [middleware.device.controller :as dc :refer [initial-state state update-chan send!]]))

(defn- send-breakpoints! []
  (let [bpts (apply set/union (-> @state :debugger :breakpoints vals))
        pcs (program/pcs (-> @state :program :running))]
    (if (< (count bpts)
           (count pcs))
      (do
        (send! (p/clear-all-breakpoints))
        (send! (p/set-breakpoints bpts)))
      (do
        (send! (p/set-all-breakpoints))
        (send! (p/clear-breakpoints (remove bpts pcs)))))
    (a/put! update-chan :debugger)))

(defn set-user-breakpoints! [pcs]
  (swap! state assoc-in [:debugger :breakpoints :user] (set pcs))
  (send-breakpoints!))

(defn set-system-breakpoints! [pcs]
  (swap! state assoc-in [:debugger :breakpoints :system] (set pcs))
  (send-breakpoints!))

(defn clear-system-breakpoints! []
  (swap! state assoc-in [:debugger :breakpoints :system] #{})
  (send-breakpoints!))

(defn break! []
  (set-system-breakpoints! (program/pcs (-> @state :program :running))))

(defn send-continue! []
  (swap! state assoc-in [:debugger :vm] nil)
  (send! (p/continue))
  (a/put! update-chan :debugger))

(defn continue! []
  (clear-system-breakpoints!)
  (send-continue!))

(defn step-next! []
  (break!)
  (send-continue!))


(defn stack-frames [program {:keys [stack pc fp]}]
  (when-not (empty? stack)
    (when-let [script (program/script-for-pc program pc)]
      (let [arguments (-> script :arguments)
            locals (-> script :locals)
            var-count (+ (count arguments)
                         (count locals))
            next-data (conversions/bytes->uint32
                       (nth stack (+ fp var-count)))
            next-pc (bit-and 0xFFFF next-data)
            next-fp (bit-and 0xFFFF (bit-shift-right next-data 16))
            frame {:script script
                   :pc pc
                   :fp fp
                   :stack stack
                   :return next-pc
                   :arguments (vec (map-indexed (fn [index {var-name :name}]
                                                  {:name var-name
                                                   :value (conversions/bytes->float
                                                           (nth stack (+ index fp)))})
                                                arguments))
                   :locals (vec (map-indexed (fn [index {var-name :name}]
                                               {:name var-name
                                                :value (conversions/bytes->float
                                                        (nth stack (+ (count arguments) index fp)))})
                                             locals))}]
        (lazy-seq
         (cons frame
               (stack-frames program
                             {:stack (take fp stack)
                              :pc next-pc
                              :fp next-fp})))))))

(defn instruction-groups [program]
  (let [groups (volatile! [])
        current (volatile! [])]
    (loop [pc 0
           [script & rest] (:scripts program)]
      (when script
        (let [instructions (vec (:instructions script))]
          (dotimes [i (count instructions)]
                   (let [instr (nth instructions i)]
                     (vswap! current conj [(+ pc i) instr])
                     (when (program/statement? instr)
                       (let [instrs (map second @current)
                             start (apply min (map first @current))
                             stop (+ start (count instrs) -1)
                             pcs (range start (inc stop))]
                         (vswap! groups conj {:start start
                                              :stop stop
                                              :instructions instrs
                                              :pcs pcs
                                              :script script}))
                       (vreset! current []))))
          (recur
            (+ pc (count instructions))
            rest))))
    @groups))

(defn interval-at-pc [program pc]
  ; TODO(Richo): Calculate the interval for the entire instruction group?
  (when-let [token (-> (program/instruction-at-pc program pc)
                       meta :node
                       meta :token)]
    [(:start token)
     (+ (:start token)
        (:count token))]))

(defn instruction-group-at-pc [groups pc]
  (seek (fn [{:keys [start stop]}]
          (and (>= pc start)
               (<= pc stop)))
        groups))

(defn- trivial? [{:keys [instructions]}] ; TODO(Richo): Better name please!
  (and (program/unconditional-branch? (last instructions))
       (not-any? program/script-call? instructions)))

(defn- call? [{:keys [instructions]}]
  (some? (some program/script-call? instructions)))

(defn- return? [{:keys [instructions]}]
  (some? (some program/return? instructions)))

(defn- branch? [{:keys [instructions] :as ig}]
  (and (not (trivial? ig))
       (program/branch? (last instructions))))

(defn next-instruction-group [groups {:keys [stop script]}]
  (when-let [next (instruction-group-at-pc groups (+ 1 stop))]
    (when (= script (:script next))
      next)))

(defn branch-instruction [groups {:keys [stop instructions script]}]
  (when-let [branch (instruction-group-at-pc groups
                                             (-> instructions last :argument inc (+ stop)))]
    (when (= script (:script branch)))))

(declare step-over)

(defn- step-over-return [vm program groups ig]
  [(-> (stack-frames program vm) first :return)])

(defn- step-over-regular [vm program groups ig]
  (if-let [next (next-instruction-group groups ig)]
    (if (trivial? next)
      (step-over vm program groups next)
      (:pcs next))
    (step-over-return vm program groups ig)))

(defn- step-over-branch [vm program groups ig]
  (concat (step-over-regular vm program groups ig)
          (when-let [branch (branch-instruction groups ig)]
            (if (trivial? branch)
              (step-over vm program groups branch)
              (:pcs branch)))))

(defn- step-over-trivial [vm program groups ig]
  (if-let [branch (branch-instruction groups ig)]
    (:pcs branch)
    (step-over-regular vm program groups ig)))

(defn- step-over-call [vm program groups ig]
  (step-over-regular vm program groups ig))

(defn- step-over [vm program groups ig]
  (cond
    (trivial? ig) (step-over-trivial vm program groups ig)
    (call? ig) (step-over-call vm program groups ig)
    (return? ig) (step-over-return vm program groups ig)
    (branch? ig) (step-over-branch vm program groups ig)
    :else (step-over-regular vm program groups ig)))

(defn step-over-breakpoints [vm program]
  (let [groups (instruction-groups program)
        pc (:pc vm)]
    (if-let [ig (instruction-group-at-pc groups pc)]
      (step-over vm program groups ig)
      [])))

(defn step-over! []
  (clear-system-breakpoints!) ; TODO(Richo): I don't think this is necessary
  (let [bpts (step-over-breakpoints (-> @state :debugger :vm)
                                    (-> @state :program :running))]
    (set-system-breakpoints! bpts)
    (send-continue!)))


(defn- call-instruction [{:keys [start instructions]} pc]
  (seek program/script-call?
        (map second
             (filter (fn [[pc* _]] (>= pc* pc))
                     (map-indexed (fn [i instr] [(+ start i)
                                                 instr])
                                  instructions)))))

(defn- step-into-call [vm program groups ig]
  (if-let [call (call-instruction ig (:pc vm))]
    (if-let [target (seek (fn [g] (= (-> g :script :name)
                                     (:argument call)))
                          groups)]
      [(:start target)]
      (step-over-regular vm program groups ig))
    (step-over-regular vm program groups ig)))

(defn- step-into [vm program groups ig]
  (if (call? ig)
    (step-into-call vm program groups ig)
    (step-over vm program groups ig)))

(defn- step-into-breakpoints [vm program]
  (let [groups (instruction-groups program)
        pc (:pc vm)]
    (if-let [ig (instruction-group-at-pc groups pc)]
      (step-into vm program groups ig)
      [])))

(defn step-into! []
  (clear-system-breakpoints!) ; TODO(Richo): I don't think this is necessary
  (let [bpts (step-into-breakpoints (-> @state :debugger :vm)
                                    (-> @state :program :running))]
    (set-system-breakpoints! bpts)
    (send-continue!)))

(defn- step-out [vm program groups ig]
  (step-over-return vm program groups ig))

(defn- step-out-breakpoints [vm program]
  (let [groups (instruction-groups program)
        pc (:pc vm)]
    (if-let [ig (instruction-group-at-pc groups pc)]
      (step-out vm program groups ig)
      [])))

(defn step-out! []
  (clear-system-breakpoints!)
  (let [bpts (step-out-breakpoints (-> @state :debugger :vm)
                                    (-> @state :program :running))]
    (set-system-breakpoints! bpts)
    (send-continue!)))

(comment

 (do
   (def program (-> @state :program :running))
   (def vm (-> @state :debugger :vm))
   (def pc (:pc vm))
   (def groups (instruction-groups program))
   (def ig (instruction-group-at-pc groups pc))
   (def next (next-instruction-group groups ig)))

 (step-over-breakpoints vm program)
 (step-into-breakpoints vm program)
 (next-instruction-group groups (instruction-group-at-pc groups pc))

 (connect! "COM4")
 (connect! "127.0.0.1:4242")
 (dc/connected?)
 (disconnect!)

 (instruction-group-at-pc (-> @state :program :running)
                          (-> @state :debugger :vm :pc))

 (break!)
 (step-next!)
 (-> @state :debugger)
 (def sf (stack-frames (-> @state :program :running)
                       (-> @state :debugger :vm)))
 (def ig (instruction-groups (-> @state :program :running)))
 (mapv #(dissoc % :script) ig)
 (mapv (fn [g] (-> g
                   (dissoc :script)
                   (assoc :trivial? (trivial? g))
                   (assoc :call? (call? g))
                   (assoc :return? (return? g))
                   (assoc :branch? (branch? g))))
       ig)

 (continue!)
 (dc/reset-debugger!)

 (set-user-breakpoints! [9 12])
 (send-breakpoints!)
 (-> @state :globals)
 (send-continue)
 (-> @state :debugger)

 (map-indexed (fn [v i] [v i])
              (program/instructions (-> @state :program :running)))

 ,,)
