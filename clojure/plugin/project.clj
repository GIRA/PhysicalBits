(defproject plugin "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.6.532"]]
  :main ^:skip-aot plugin.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :test {:resource-paths ["env/test/resources"]}
             :dev {:dependencies [[org.clojure/tools.namespace "0.3.1"]
                                  [pjstadig/humane-test-output "0.10.0"]
                                  [org.clojars.beppu/clj-audio "0.3.0"]]
                   :source-paths ["env/dev/clj"]
                   :resource-paths ["env/dev/resources"]
                   :repl-options {:init-ns user
                                  :timeout 120000}
                   :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]]
                   :injections [(require 'pjstadig.humane-test-output)
                                (pjstadig.humane-test-output/activate!)]}})
