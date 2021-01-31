(defproject middleware "1.0.0-SNAPSHOT"
  :description "Physical Bits server"
  :url "https://gira.github.io/PhysicalBits/"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.6.532"]
                 [org.clojure/tools.logging "1.1.0"]
                 [ch.qos.logback/logback-classic "1.1.3"]
                 [org.clojure/tools.cli "1.0.194"]
                 [clj-serial "2.0.5"]
                 [compojure "1.6.1"]
                 [aleph "0.4.6"]
                 [cheshire "5.9.0"]
                 [instaparse "1.4.10"]]
  :main ^:skip-aot middleware.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.compiler.elide-meta=[:doc :file :line :added]"]}

             :test [:project/test :user/test]
             :dev [:project/dev :user/dev]

             ; NOTE(Richo): To use atom and proto-repl (place in profiles.clj)
             ; :user/dev {:dependencies [[proto-repl "0.3.1"]]}

             ; NOTE(Richo): To use sound notifications with test-refresh (place in profiles.clj)
             ; :user/test {:resource-paths ["env/test/sounds"]}

             :project/test {:resource-paths ["env/test/resources"]}

             :project/dev {:dependencies [[org.clojure/tools.namespace "0.3.1"]
                                          [org.clojars.beppu/clj-audio "0.3.0"]]
                           :source-paths ["env/dev/clj"]
                           :resource-paths ["env/dev/resources"]
                           :repl-options {:init-ns user
                                          :timeout 120000}
                           ;:middleware [ultra.plugin/middleware]
                           :plugins [;[venantius/ultra "0.6.0"]
                                     [com.jakemccrary/lein-test-refresh "0.24.1"]]}})
