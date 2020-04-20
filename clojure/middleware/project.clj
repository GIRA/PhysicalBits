(defproject middleware "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.6.532"]
                 [clj-serial "2.0.5"]
                 [compojure "1.6.1"]
                 [aleph "0.4.6"]
                 [cheshire "5.9.0"]
                 [instaparse "1.4.10"]]
  :main ^:skip-aot middleware.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}

             :test [:project/test :user/test]
             :dev [:project/dev :user/dev]

             :project/test {:resource-paths ["env/test/resources"]}

             :project/dev {:dependencies [[org.clojure/tools.namespace "0.3.1"]
                                          [org.clojars.beppu/clj-audio "0.3.0"]]
                           :source-paths ["env/dev/clj"]
                           :resource-paths ["env/dev/resources"]
                           :repl-options {:init-ns user
                                          :timeout 120000}
                           :middleware [ultra.plugin/middleware]
                           :plugins [[venantius/ultra "0.6.0"]
                                     [com.jakemccrary/lein-test-refresh "0.24.1"]]}})
