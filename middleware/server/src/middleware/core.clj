(ns middleware.core
  (:require [clojure.tools.logging :as log]
            [clojure.tools.cli :as cli]
            [clojure.string :as str]
            [clojure.java.io :as io]
            [clojure.java.browse :refer [browse-url]]
            [middleware.server.http :as http]
            [middleware.server.udp :as udp]
            [middleware.device.controller :as dc]
            [middleware.device.ports.common :as ports]
            [middleware.device.ports.serial :as serial]
            [middleware.device.ports.socket :as socket]
            [middleware.device.ports.scanner :as port-scanner])
  (:gen-class))

(defmacro project-data [key]
  "HACK(Richo): This macro allows to read the project.clj file at compile time"
  `~(let [data (-> "project.clj" slurp read-string)
          name (str (nth data 1))
          version (nth data 2)
          rest (drop 3 data)]
      ((apply assoc {:name name, :version version} rest) key)))

(def project-name
  (let [description (project-data :description)
        version (project-data :version)]
    (format "%s (%s)" description version)))

(def cli-options
  [["-u" "--uzi PATH" "Uzi libraries folder (default: uzi)"
    :default "uzi"
    :validate [#(.exists (io/file %)) "The directory doesn't exist"]]
   ["-w" "--web PATH" "Web resources folder (default: web)"
    :default "web"
    :validate [#(.exists (io/file %)) "The directory doesn't exist"]]
   ["-s" "--server-port PORT" "Server port number"
    :default 3000
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]
   ["-a" "--arduino-port PORT" "Arduino port name"
    :default nil]
   ["-o" "--open-browser" "Open browser flag"]
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
  (log/info msg)
  (System/exit status))

(defn main
  [{:keys [uzi web server-port arduino-port open-browser]}]
  (log/info project-name)
  (port-scanner/start!)
  (ports/register-port socket/open-port
                       serial/open-port)
  (log/info "Starting server...")
  (http/start :uzi-libraries uzi
              :web-resources web
              :server-port server-port)
  (udp/start)
  (log/info "Server started on port" server-port)
  (when open-browser
    (let [url (str "http://localhost:" server-port)]
      (log/info "Opening browser on" url)
      (browse-url url)))
  (println)
  (when arduino-port
    (dc/connect arduino-port)))

(defn -main [& args]
  (let [{:keys [errors options summary]} (cli/parse-opts args cli-options)]
    (when errors
      (exit 1 (error-msg errors)))
    (when (:help options)
      (exit 0 (usage summary)))
    (main options)))

(comment
 (-main "-")

 ( main)
 ,,)
