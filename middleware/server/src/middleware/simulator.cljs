(ns middleware.simulator
  (:require [clojure.core.async :as a :refer [go go-loop <! timeout]]
            [middleware.core :as core]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.simulator :as simulator]
            [middleware.utils.fs.common :as fs]
            [middleware.utils.fs.browser :as browser]
            [middleware.utils.async :refer [go-try <? chan->promise]]))

(comment

  (def components (js->clj js/LayoutManager.components
                           :keywordize-keys true))

  
  (defn get-path [endpoint data]
    (cond (= endpoint data) []
          (list? data) (some (fn [[i v]]
                               (when-let [p (get-path endpoint v)]
                                 (cons i p)))
                             (map-indexed vector (drop 1 data)))
          (keyword? data) nil))

  (defn simplify-layout [layout]
    (if (and (= "stack" (:type layout))
             (= 1 (count (:content layout))))
      (let [content (first (:content layout))
            {stack-w :width stack-h :height} layout
            {content-w :width content-h :height} content]
        (simplify-layout (cond-> content
                           (and stack-w (not content-w)) (assoc :width stack-w)
                           (and stack-h (not content-h)) (assoc :height stack-h))))
      (let [{:keys [id type content width height]}
            (update layout :content #(mapv simplify-layout %))]
        (if id
          (keyword id)
          (vary-meta (apply list (symbol (or type :content)) content)
                     assoc :width width :height height)))))

  (defn complicate-layout [simple-layout]
    (if (keyword? simple-layout)
      (components simple-layout)
      (let [[type & content] simple-layout
            {:keys [width height]} (meta simple-layout)
            content* (mapv complicate-layout content)]
        (cond-> (case type
                  content {"content" content*}
                  row {"type" "row" "content" content*}
                  column {"type" "column" "content" content*}
                  stack {"type" "stack" "content" content*})
          width (assoc "width" width)
          height (assoc "height" height)))))

  (defn insert-in
    ([layout path element] 
     (insert-in layout path element nil))
    ([layout path element parent]
     (println ">>>" layout)
     (if (seqable? layout)
       (let [[key & content] layout
             [idx & rpath] path]
         (concat [key]
                 (if (seq content)
                   (if (>= idx (count content))
                     (concat content [element])
                     (if (empty? rpath)
                       (let [[l r] (split-at idx content)]
                         (concat l [element] r))
                       (map-indexed (fn [i v]
                                      (if (= i idx)
                                        (insert-in v rpath element key)
                                        v))
                                    content)))
                   [element])))
       (list (case parent
               row 'column
               column 'row
               nil 'row)
             layout element))))

  
  
  (insert-in adv [0 0 1] :a)

  (min 10 9)
  (split-at 1 [:a :b :c])

  (get-path :a '(content :b :c (row :a)))
  (get-path :a '(content (row :a)))
  (next '(content (row)))
  (insert-in '(content) [10] :a)
  (insert-in '(content (row)) [0 1] :a)
  (insert-in '(content (row :a :c)) [0 1] :b)

  update-in

  (defonce basic (-> (js->clj (js/LayoutManager.getLayoutConfig)
                              :keywordize-keys true)
                     simplify-layout))

  (defonce adv (-> (js->clj (js/LayoutManager.getLayoutConfig)
                            :keywordize-keys true)
                   simplify-layout))

  

  (get-path :inspector adv)
  (def output-path (get-path :output adv))

  (insert-in basic output-path :output)

  (insert-in '(content (row (column :controls :inspector) (column :blocks) (column :code)))
             output-path :richo)


  (meta (vary-meta '(foo bar baz) assoc :a 1))



  (js/LayoutManager.setLayoutConfig (clj->js (complicate-layout basic)))

  (js/LayoutManager.setLayoutConfig (clj->js (complicate-layout adv)))

  (def config (js/LayoutManager.getLayoutConfig))
  (js->clj config)


  (def new-config (clj->js
                   {"content"
                    [{"type" "row",
                      "isClosable" true,
                      "reorderEnabled" true,
                      "title" "",
                      "content"
                      [{"type" "stack",
                        "width" 29.5,
                        "height" 100,
                        "isClosable" true,
                        "reorderEnabled" true,
                        "title" "",
                        "activeItemIndex" 0,
                        "content"
                        [{"id" "controls",
                          "type" "component",
                          "height" 30,
                          "componentName" "DOM",
                          "componentState" {"id" "#controls-panel"},
                          "title" "Controls",
                          "isClosable" true,
                          "reorderEnabled" true}]}
                       {"type" "column",
                        "width" 70.5,
                        "isClosable" true,
                        "reorderEnabled" true,
                        "title" "",
                        "content"
                        [{"type" "stack",
                          "width" nil,
                          "height" 50,
                          "isClosable" true,
                          "reorderEnabled" true,
                          "title" "",
                          "activeItemIndex" 0,
                          "content"
                          [{"id" "blocks",
                            "type" "component",
                            "componentName" "DOM",
                            "componentState" {"id" "#blocks-panel"},
                            "title" "Blocks",
                            "isClosable" true,
                            "reorderEnabled" true}]}
                         {"type" "row",
                          "isClosable" true,
                          "reorderEnabled" true,
                          "title" "",
                          "height" 50,
                          "content"
                          [{"width" 50,
                            "height" 50,
                            "isClosable" true,
                            "reorderEnabled" true,
                            "activeItemIndex" 0,
                            "content"
                            [{"id" "code",
                              "type" "component",
                              "componentName" "DOM",
                              "componentState" {"id" "#code-panel"},
                              "title" "Code",
                              "isClosable" true,
                              "reorderEnabled" true}],
                            "title" "",
                            "type" "stack",
                            "header" {}}]}]}]}]}))

  (js/LayoutManager.setLayoutConfig new-config)
  )



(defn init-dependencies []
  (fs/register-fs! #'browser/file)
  (ports/register-constructors! #'simulator/open-port))

(defn init []
  (init-dependencies)
  (println "Controller started successfully!")
  (.then (.-ready js/Simulator)
         (fn []
           (println "READY TO CONNECT!")
           (js/Simulator.start 16))))

(defn ^:export on-update [update-fn]
  (core/start-update-loop! (comp update-fn clj->js)))

(defn ^:export connect [update-fn]
  (chan->promise
   (go-try
    (<? (core/connect! "simulator")))))

(defn ^:export disconnect []
  (chan->promise
   (go-try
    (<? (core/disconnect!)))))

(defn ^:export compile [src type silent?]
  (chan->promise
   (go-try
    (clj->js (<? (core/compile! src type silent?))))))

(defn ^:export run [src type silent?]
  (chan->promise
   (go-try
    (clj->js (<? (core/compile-and-run! src type silent?))))))

(defn ^:export install [src type]
  (chan->promise
   (go-try
     (clj->js (<? (core/compile-and-install! src type))))))

(defn ^:export set-pin-report [pins report]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert pins and report are the same size
    (<? (core/set-pin-report! (map vector pins report))))))

(defn ^:export set-global-report [globals report]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert globals and report are the same size
    (<? (core/set-global-report! (map vector globals report))))))

(defn ^:export set-pin-values [pins values]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert pins and values are the same size
    (<? (core/set-pin-values! (map vector pins values))))))

(defn ^:export set-global-values [globals values]
  (chan->promise
   (go-try
    ; TODO(Richo): Assert globals and values are the same size
    (<? (core/set-global-values! (map vector globals values))))))

(defn ^:export set-profile [enabled?]
  (chan->promise
   (go-try
    (<? (core/set-profile! enabled?)))))

(defn ^:export debugger-set-breakpoints [breakpoints]
  (chan->promise
   (go-try
    (<? (core/set-breakpoints! breakpoints)))))

(defn ^:export debugger-break []
  (chan->promise
   (go-try
    (<? (core/debugger-break!)))))

(defn ^:export debugger-continue []
  (chan->promise
   (go-try
    (<? (core/debugger-continue!)))))

(defn ^:export debugger-step-over []
  (chan->promise
   (go-try
    (<? (core/debugger-step-over!)))))

(defn ^:export debugger-step-into []
  (chan->promise
   (go-try
    (<? (core/debugger-step-into!)))))

(defn ^:export debugger-step-out []
  (chan->promise
   (go-try
    (<? (core/debugger-step-out!)))))

(defn ^:export debugger-step-next []
  (chan->promise
   (go-try
    (<? (core/debugger-step-next!)))))
