(ns middleware.device.debugger
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [clojure.core.async :as a]
            [middleware.program.utils :as program]
            [middleware.device.protocol :as p]
            [middleware.utils.conversions :as conversions]
            [middleware.utils.core :refer [seek]]
            [middleware.device.controller :as dc :refer [state update-chan send!]]))

(defn send-breakpoints! [breakpoints]
  (let [pcs (program/pcs (-> @state :program :running))]
    (if (< (count breakpoints)
           (count pcs))
      (do
        (send! (p/clear-all-breakpoints))
        (send! (p/set-breakpoints breakpoints)))
      (do
        (send! (p/set-all-breakpoints))
        (send! (p/clear-breakpoints (remove breakpoints pcs)))))))

(defn send-all-breakpoints! []
  (send-breakpoints! (apply set/union (-> @state :debugger :breakpoints vals))))

(defn preserve-breakpoints! [action]
  (let [breakpoints (-> @state :debugger :breakpoints :user)]
    (action)
    (swap! state assoc-in [:debugger :breakpoints :user] breakpoints)
    (send-breakpoints! breakpoints)
    (send! (p/continue))))

(defn set-user-breakpoints! [pcs]
  (swap! state assoc-in [:debugger :breakpoints :user] (set pcs))
  (send-all-breakpoints!)
  (a/put! update-chan :debugger))

(defn set-system-breakpoints! [pcs]
  (swap! state assoc-in [:debugger :breakpoints :system] (set pcs))
  (send-all-breakpoints!)
  (a/put! update-chan :debugger))

(defn clear-system-breakpoints! []
  (swap! state assoc-in [:debugger :breakpoints :system] #{})
  (send-all-breakpoints!)
  (a/put! update-chan :debugger))

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

(defn var-display-name [name]
  (first (str/split name #"#")))

(defn stack-frames [program {:keys [stack pc fp]}]
  "Recursively parses the stack data and returns a sequence of stack-frames"
  (when-not (empty? stack)
    (when-let [script (program/script-for-pc program pc)]
      (let [arguments (-> script :arguments)
            locals (-> script :locals)
            variables (vec (concat arguments locals))
            var-count (count variables)
            next-data (conversions/bytes->uint32
                       (nth stack (+ fp var-count)))
            next-pc (bit-and 0xFFFF next-data)
            next-fp (bit-and 0xFFFF (bit-shift-right next-data 16))
            frame {:script script
                   :source (-> script meta :node meta :token :source)
                   :pc pc
                   :fp fp
                   :stack (map-indexed (fn [i val]
                                         {:raw-value val
                                          :description (cond
                                                         (< i var-count)
                                                         (str (var-display-name (:name (nth variables i)))
                                                              ": " (conversions/bytes->float val))

                                                         (> i var-count)
                                                         (str (conversions/bytes->float val))

                                                         :else (str "PC: " next-pc
                                                                    ", FP: " (if (= 0xFFFF next-fp)
                                                                               -1
                                                                               next-fp)))})
                                       (drop fp stack))
                   :return next-pc
                   :arguments (vec (map-indexed (fn [index {var-name :name}]
                                                  {:name (var-display-name var-name)
                                                   :value (conversions/bytes->float
                                                           (nth stack (+ index fp)))})
                                                arguments))
                   :locals (vec (map-indexed (fn [index {var-name :name}]
                                               {:name (var-display-name var-name)
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
  "An instruction group is sequence of contiguous instructions in which
   the last instruction is a statement (as defined in program/statement?).
   This grouping is useful to implement step-by-step execution because we
   mostly care about stepping over statements, instructions in between
   don't really matter much and we can safely bypass them."
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

(defn instruction-group-at-pc [groups pc]
  (seek (fn [{:keys [start stop]}]
          (and (>= pc start)
               (<= pc stop)))
        groups))

(defn interval-at-pc [program pc]
  (let [get-token (fn [instr] (-> instr meta :node meta :token))]
    (if-some [tokens (seq (map get-token
                               (remove program/branch?
                                       (-> program
                                           instruction-groups
                                           (instruction-group-at-pc pc)
                                           :instructions))))]
      [(apply min (map :start tokens))
       (apply max (map #(+ (:start %) (:count %)) tokens))]
      (when-let [token (get-token (program/instruction-at-pc program pc))]
        [(:start token)
         (+ (:start token)
            (:count token))]))))

(defn- trivial? [{:keys [instructions]}] ; TODO(Richo): Better name please! Maybe just jmp?
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
    (when (= script (:script branch))
      branch)))

(declare step-over)

(defn- step-over-return [vm program _groups _ig]
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
    (step-over-return vm program groups ig)))

(defn- step-over-call [vm program groups ig]
  (step-over-regular vm program groups ig))

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

(defn step-over [vm program groups ig]
  (cond
    (trivial? ig) (step-over-trivial vm program groups ig)
    (branch? ig) (step-over-branch vm program groups ig)
    (call? ig) (step-over-call vm program groups ig)
    (return? ig) (step-over-return vm program groups ig)
    :else (step-over-regular vm program groups ig)))

(defn step-into [vm program groups ig]
  (if (call? ig)
    (step-into-call vm program groups ig)
    (step-over vm program groups ig)))

(defn step-out [vm program groups ig]
  (step-over-return vm program groups ig))

(defn estimate-breakpoints ; TODO(Richo): Write tests for this function!
  ([step-fn]
   (let [state @state]
     (estimate-breakpoints step-fn
                           (-> state :debugger :vm)
                           (-> state :program :running))))
  ([step-fn vm program]
   (try
     (let [groups (instruction-groups program)
           pc (:pc vm)]
       (if-let [ig (instruction-group-at-pc groups pc)]
         (step-fn vm program groups ig)
         []))
     (catch #?(:clj Throwable :cljs :default) _ []))))

(defn step-over! []
  (let [bpts (estimate-breakpoints step-over)]
    (set-system-breakpoints! bpts)
    (send-continue!)))

(defn step-into! []
  (let [bpts (estimate-breakpoints step-into)]
    (set-system-breakpoints! bpts)
    (send-continue!)))

(defn step-out! []
  (let [bpts (estimate-breakpoints step-out)]
    (set-system-breakpoints! bpts)
    (send-continue!)))

(comment
  (-> @state :debugger)

  (do
    (def program (-> @state :program :running))
    (def vm (-> @state :debugger :vm))
    (def pc (:pc vm))
    (def groups (instruction-groups program))
    (def ig (instruction-group-at-pc groups pc))
    (def next (next-instruction-group groups ig)))

  (estimate-breakpoints step-over vm program)
  (estimate-breakpoints step-into vm program)
  (estimate-breakpoints step-out vm program)
  (next-instruction-group groups (instruction-group-at-pc groups pc))
  (mapv #(dissoc % :script) groups)


  (dc/connect! "COM4")
  (dc/connect! "127.0.0.1:4242")
  (dc/connected?)
  (dc/disconnect!)

  (instruction-group-at-pc (-> @state :program :running)
                           (-> @state :debugger :vm :pc))

  (break!)
  (step-next!)
  (-> @state :debugger)
  (def sf (stack-frames (-> @state :program :running)
                        (-> @state :debugger :vm)))
  (def ig (instruction-groups (-> @state :program :running)))

  (mapv (fn [g] (-> g
                    (dissoc :script)
                    (assoc :trivial? (trivial? g))
                    (assoc :call? (call? g))
                    (assoc :return? (return? g))
                    (assoc :branch? (branch? g))))
        groups)

  (continue!)
  (dc/reset-debugger!)

  (set-user-breakpoints! [9 12])
  (send-all-breakpoints!)
  (-> @state :globals)
  (send-continue!)
  (-> @state :debugger)

  (map-indexed (fn [v i] [v i])
               (program/instructions (-> @state :program :running)))

  )
