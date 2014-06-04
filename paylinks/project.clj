(defproject spid-clojure-paylinks-example "0.1.0-SNAPSHOT"
  :description "SPiD Paylinks example"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [spid-client-clojure "1.0.0"]
                 [ring "1.2.1"]
                 [compojure "1.1.3"]
                 [clj-http "0.7.9"]
                 [cheshire "5.3.1"]]
  :ring {:handler spid-clojure-paylinks-example.core/app
         :port 3014}
  :profiles {:dev {:plugins [[lein-ring "0.8.7"]]
                   :resource-paths ["config"]}})
