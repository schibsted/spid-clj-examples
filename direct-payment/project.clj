(defproject spid-clojure-direct-payment-example "0.1.0-SNAPSHOT"
  :description "SPiD Paylinks example"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [spid-client-clojure "1.1.0"]]
  :profiles {:dev {:plugins [[lein-ring "0.8.7"]]
                   :resource-paths ["config"]}}
  :main spid-clojure-direct-payment-example.core)
