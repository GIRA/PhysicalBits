(ns user
  (:require [plugin.core :refer :all]
            [plugin.device.core :as device :refer [state]]
            [plugin.server.core :as server :refer [server]]
            [plugin.compiler.core :as compiler]
            [clojure.core.async :as a :refer [go-loop <! <!! timeout]]
            [clojure.tools.namespace.repl :as repl])
  (:use [clojure.repl]))

(defn reload []
  (device/disconnect)
  (server/stop)
  (repl/refresh))

(defn millis [] (System/currentTimeMillis))

(defn print-a0
  ([] (print-a0 5000 10))
  ([ms] (print-a0 ms 10))
  ([ms interval]
   (let [begin (millis)
         end (+ begin ms)]
     (time
      (<!! (go-loop []
             (let [now (millis)]
               (when (< now end)
                 (println (- now begin) ":" (device/get-pin-value "A0"))
                 (<! (timeout interval))
                 (recur)))))))))

(def programs
  {:empty [0 0]
   :blink13 [0 1 2 4 13 5 3 232 192 4 2 131 162]
   :seconds-counter [0 2 3 12 0 13 200 192 5 2 132 162 128 2 169 147]
   :ticking-test [0 3 4 16 5 9 13 200 192 6 2 133 162 128 2 132 162 128 6 131 250 29 225 131 250 29 209]})


(def asts
  {:empty {:__class__ "UziProgramNode"
           :globals []
           :id "a1049b64-6166-ae43-b295-6848823eb0ed"
           :imports []
           :primitives []
           :primitivesDict []
           :scripts []}
   :blink13 {:__class__ "UziProgramNode"
             :globals []
             :id "86912dca-e66d-434e-b890-01d439b72cc0"
             :imports []
             :primitives []
             :primitivesDict []
             :scripts
             [{:__class__ "UziTaskNode"
               :arguments []
               :body
               {:__class__ "UziBlockNode"
                :id "1952b934-1526-8546-8348-782bca532aa0"
                :statements
                [{:__class__ "UziCallNode"
                  :arguments
                  [{:__class__ "Association"
                    :key nil
                    :value {:__class__ "UziPinLiteralNode", :id "6e904e8b-94f0-0440-8746-30f1d73f3f6b", :number 13, :type "D"}}]
                  :id "69ec4419-0414-394a-9874-f6eafa2ce5fd"
                  :primitiveName "toggle"
                  :selector "toggle"}]}
               :id "c339059d-e055-0e41-9641-a4205e3aca4c"
               :name "blink13"
               :state "running"
               :tickingRate {:__class__ "UziTickingRateNode", :id "d877bd32-1b0a-374e-b1a7-80c4830418b7", :scale "s", :value 1}}]}
   :seconds-counter {:__class__ "UziProgramNode"
                     :globals
                     [{:__class__ "UziVariableDeclarationNode", :id "2c5d96ea-862f-7d4f-aefe-5d95ddb621da", :name "seconds", :value nil}]
                     :id "8deaa207-fcbb-c74d-b7e3-9372a606be24"
                     :imports []
                     :primitives []
                     :primitivesDict []
                     :scripts
                     [{:__class__ "UziTaskNode"
                       :arguments []
                       :body
                       {:__class__ "UziBlockNode"
                        :id "9340ea07-d88c-fa43-8654-9d6410c55a17"
                        :statements
                        [{:__class__ "UziCallNode"
                          :arguments
                          [{:__class__ "Association"
                            :key nil
                            :value {:__class__ "UziPinLiteralNode", :id "7bed1747-220c-5848-9b71-0965b35ae010", :number 13, :type "D"}}]
                          :id "a0609194-a289-de40-aa57-6f201895e44b"
                          :primitiveName "toggle"
                          :selector "toggle"}]}
                       :id "54ceee1a-6a92-c84c-bf00-57779af51725"
                       :name "blink13"
                       :state "running"
                       :tickingRate {:__class__ "UziTickingRateNode", :id "3dc329da-4a30-1146-9661-d2b69f148b59", :scale "s", :value 4}}
                      {:__class__ "UziTaskNode"
                       :arguments []
                       :body
                       {:__class__ "UziBlockNode"
                        :id "1640d177-fccd-2945-bf11-a29c8fbea6b0"
                        :statements
                        [{:__class__ "UziAssignmentNode"
                          :id "f51ab2ea-aae9-4e47-9556-671c4320c027"
                          :left {:__class__ "UziVariableNode", :id "0d5c6f6c-5cda-fc4a-925e-8abf33b42171", :name "seconds"}
                          :right
                          {:__class__ "UziCallNode"
                           :arguments
                           [{:__class__ "Association"
                             :key nil
                             :value {:__class__ "UziVariableNode", :id "11462f78-1e06-9c4e-af18-b73558428680", :name "seconds"}}
                            {:__class__ "Association"
                             :key nil
                             :value {:__class__ "UziNumberLiteralNode", :id "1b01211d-a3ff-0b4b-a418-091546a39f4b", :value 1}}]
                           :id "4e69c357-207e-2648-8ffb-08d6cc800830"
                           :primitiveName "add"
                           :selector "+"}}]}
                       :id "35206919-2c34-ca43-9c1c-6a54a549c79c"
                       :name "secondcounter"
                       :state "running"
                       :tickingRate {:__class__ "UziTickingRateNode", :id "2ebd413b-6fcc-614f-aa79-f22d00924680", :scale "s", :value 1}}]}

   })
