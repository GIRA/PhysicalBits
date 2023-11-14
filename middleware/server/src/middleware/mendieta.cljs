(ns middleware.mendieta
  (:require ["express" :as express]
            ["express-ws" :as ws]))

(defn start! [port]
  (let [app (express)]
    (ws app)
    (.use app (.json express))
    (.use app (.urlencoded express #js {:extended true}))
    (.use app (.static express "frontend"))

    (.listen app port #(println "Server started on" (str "http://localhost:" port)))))

(defn stop! []
  )

(defn main [& args]
  (println "Hola mundo")
  (start! 5000))

(comment
  
  (start! 5000)
  (stop!)

  (.close *1)

  )