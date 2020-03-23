(ns plugin.compiler.encoder)

(defmulti encode-node :__class__)

(defn encode [program]
  (vec (encode-node program)))

(def default-globals #{0 1 -1})

(defn encode-globals [globals]
  (let [all-globals (into (map :value globals)
                          default-globals)
        to-encode (filter (complement default-globals)
                          all-globals)]
    (concat [(count to-encode)])))

(defmethod encode-node "UziProgram"
  [{:keys [scripts variables] :as program}]
  (concat [(count scripts)]
          (encode-globals variables)
          (mapcat encode-node scripts)))

(defn encode-script-header
  [{:keys [arguments delay locals ticking]}]
  (concat [0]))

(defn encode-instructions [instructions]
  (concat [(count instructions)]
          (mapcat encode-node instructions)))

(defmethod encode-node "UziScript"
  [{:keys [instructions] :as script}]
  (concat (encode-script-header script)
          (encode-instructions instructions)))

(defmethod encode-node :default [o] [])

#_(
   (def src "")
   (def program (plugin.compiler.core/compile-uzi-string src))

    (encode program)
   (encode-script-count (:scripts program))
   (encode-globals (:globals program))
   (mapcat encode-node (:scripts program))




















   )
