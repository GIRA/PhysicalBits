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
            frame {;:program program ; Do we need the program? Seems redundant...
                   :script script
                   :pc pc
                   :fp fp
                   :stack stack
                   :arguments (vec (map-indexed (fn [index {var-name :name}]
                                                  {:name var-name
                                                   :value (conversions/bytes->float
                                                           (nth stack (+ index fp)))})
                                                arguments))
                   :locals (vec (map-indexed (fn [index {var-name :name}]
                                               {:name var-name
                                                :value (conversions/bytes->float
                                                        (nth stack (+ (count arguments) index fp)))})
                                             locals))}
            val (conversions/bytes->uint32
                 (nth stack (+ fp (count arguments) (count locals))))]
        (lazy-seq
         (cons frame
               (stack-frames program
                             {:stack (take fp stack)
                              :pc (bit-and 0xFFFF val)
                              :fp (bit-and 0xFFFF (bit-shift-right val 16))})))))))

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
                       (let [start (apply min (map first @current))
                             instrs (map second @current)]
                         (vswap! groups conj {:start start
                                              :instructions instrs
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

(defn instruction-group-at-pc [program pc]
  (let [groups (instruction-groups program)]
    (seek (fn [{:keys [start instructions]}]
            (let [stop (+ start (count instructions))]
              (and (>= pc start)
                   (<= pc stop))))
          groups)))

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

(defn branch-instruction [{:keys [instructions]}])

(defn- step-over-trivial [ig])
(defn- step-over-call [ig])
(defn- step-over-return [ig])
(defn- step-over-branch [ig])
(defn- step-over-regular [ig])

(defn step-over-breakpoints []
  (let [program (-> @state :program :running)
        pc (-> @state :debugger :vm :pc)]
    (if-let [ig (instruction-group-at-pc program pc)]
      (cond
        (trivial? ig) (step-over-trivial ig)
        (call? ig) (step-over-call ig)
        (return? ig) (step-over-return ig)
        (branch? ig) (step-over-branch ig)
        :else (step-over-regular ig))
      [])))

(comment

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
