(defproject spid-clojure-direct-payment-example "0.1.0-SNAPSHOT"
  :description "SPiD Paylinks example"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/core.incubator "0.1.3"]
                 [spid-client-clojure "1.1.0"]
                 [ring "1.2.1"]
                 [compojure "1.1.3"]
                 [clj-http "0.7.9"]
                 [cheshire "5.3.1"]]
  :ring {:handler spid-clojure-direct-payment-example.core/app
         :port 3015}
  :profiles {:dev {:plugins [[lein-ring "0.8.7"]]
                   :resource-paths ["config"]}})
