(ns program-rewriter
  (:require [clojure.walk :as w]
            [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.pprint :as pprint]))

(defmulti ^:private rewrite-node :__class__)

(defmethod rewrite-node :default [node] node)

(defmethod rewrite-node "UziProgram" [{:keys [globals scripts]}]
  (list 'emit/program :globals globals :scripts scripts))

(defmethod rewrite-node "UziVariable" [{:keys [name value]}]
   (if name
     (if value
       (list 'emit/variable name value)
       (list 'emit/variable name))
     (list 'emit/constant value)))

(defmethod rewrite-node "UziScript" [{:keys [name arguments delay instructions locals running?]}]
   (apply list
     (cond-> ['emit/script :name name]
             (not (empty? arguments)) (conj :arguments arguments)
             delay (conj :delay (:value delay))
             running? (conj :running? running?)
             (not (empty? locals)) (conj :locals locals)
             (not (empty? instructions)) (conj :instructions instructions))))

(defmethod rewrite-node "UziPopInstruction" [node]
   (list 'emit/write-global (-> node :argument :name)))

(defmethod rewrite-node "UziPrimitiveCallInstruction" [node]
   (list 'emit/prim-call (-> node :argument :name)))

(defmethod rewrite-node "UziPushInstruction" [node]
   (if (-> node :argument :name)
     (list 'emit/read-global (-> node :argument :name))
     (list 'emit/push-value (-> node :argument :value))))

(defmethod rewrite-node "UziStartScriptInstruction" [node]
   (list 'emit/start (:argument node)))

(defmethod rewrite-node "UziStopScriptInstruction" [node]
   (list 'emit/stop (:argument node)))

(defmethod rewrite-node "UziPauseScriptInstruction" [node]
   (list 'emit/pause (:argument node)))

(defmethod rewrite-node "UziResumeScriptInstruction" [node]
   (list 'emit/resume (:argument node)))

(defmethod rewrite-node "UziWriteLocalInstruction" [node]
   (list 'emit/write-local (-> node :argument :name)))

(defmethod rewrite-node "UziReadLocalInstruction" [node]
   (list 'emit/read-local (-> node :argument :name)))

(defmethod rewrite-node "UziScriptCallInstruction" [node]
   (list 'emit/script-call (:argument node)))

(defmethod rewrite-node "UziJMPInstruction" [node]
   (list 'emit/jmp (:argument node)))

(defmethod rewrite-node "UziJZInstruction" [node]
   (list 'emit/jz (:argument node)))

(defmethod rewrite-node "UziJNZInstruction" [node]
   (list 'emit/jnz (:argument node)))

(defmethod rewrite-node "UziJLTEInstruction" [node]
   (list 'emit/jlte (:argument node)))

(defmethod rewrite-node "UziReadInstruction" [node]
   (list 'emit/read-pin (:argument node)))

(defmethod rewrite-node "UziWriteInstruction" [node]
   (list 'emit/write-pin (:argument node)))

(defmethod rewrite-node "UziTurnOnInstruction" [node]
   (list 'emit/turn-on-pin (:argument node)))

(defmethod rewrite-node "UziTurnOffInstruction" [node]
   (list 'emit/turn-off-pin (:argument node)))

(defn- rewrite-tree [code]
  (w/prewalk rewrite-node code))

(defn rewrite-file [input-path output-path]
  (println "BEWARE! This doesn't preserve comments and the pretty print sucks.")
  (with-open [reader (java.io.PushbackReader. (io/reader input-path))
              writer (io/writer output-path)]
    (loop []
      (when-let [next (edn/read {:eof nil} reader)]
        (pprint/write (rewrite-tree next)
                      :stream writer
                      :dispatch pprint/code-dispatch
                      :pretty true
                      :readably* true)
        (.write writer "\n\n")
        (recur)))))
