(ns getting-started.core
  (:require [spid-sdk-clojure.core :refer [create-client GET]]))

(defn test-run-api [client-id secret]
  (let [options {:spid-base-url "https://stage.payment.schibsted.no"}
        client (create-client client-id secret options)]
    (clojure.pprint/pprint (GET client "/endpoints"))))
