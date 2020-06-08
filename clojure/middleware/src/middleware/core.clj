(ns middleware.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [middleware.server.server :as server]
            [middleware.device.controller :as dc])
  (:gen-class))

(def project-name
  ; TODO(Richo): Figure out a way of reading this values from the project.clj
  (let [project-name "UziScript middleware"
        project-version "0.3.0-SNAPSHOT"]
    (format "%s (%s)" project-name project-version)))

(def cli-options
  [["-u" "--uzi PATH" "Uzi libraries folder (default: uzi)"
    :default "uzi"
    :validate [#(.exists (io/file %)) "The directory doesn't exist"]]
   ["-w" "--web PATH" "Web resources folder (default: web)"
    :default "web"
    :validate [#(.exists (io/file %)) "The directory doesn't exist"]]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> [project-name
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [errors options summary]} (cli/parse-opts args cli-options)]
    (when errors
      (exit 1 (error-msg errors)))
    (when (:help options)
      (exit 0 (usage summary)))
    (let [uzi (:uzi options)
          web (:web options)]
      (time (do
              (println project-name)
              (println "Starting server...")
              (server/start :uzi-libraries uzi
                            :web-resources web)
              (println "Server started."))))))
