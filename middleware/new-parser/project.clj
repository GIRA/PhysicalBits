(defproject new-parser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "MIT License"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [clj-petitparser "0.1.0-SNAPSHOT"]]
  :profiles {:dev {:dependencies [[proto-repl "0.3.1"]
                                  [org.clojure/core.async "0.6.532"]
                                  [org.clojure/tools.cli "1.0.194"]
                                  [org.clojure/tools.namespace "0.3.1"]
                                  [org.clojars.beppu/clj-audio "0.3.0"]
                                  [org.clojure/test.check "0.9.0"]]
                   :plugins [[com.jakemccrary/lein-test-refresh "0.24.1"]]
                   :global-vars {*unchecked-math* :warn-on-boxed
                                 *warn-on-reflection* true}
                   :source-paths ["env/dev/clj"]
                   :repl-options {:init-ns user}}
             :test {:resource-paths ["env/test/sounds"]
                    :source-paths ["env/test/clj"]}})
