(ns getting-started.core
  (:require [spid-sdk-clojure.core :refer [create-server-client GET]]))

(defn test-run-api [client-id secret]
  (let [options {:spid-base-url "https://stage.payment.schibsted.no"}
        client (create-server-client client-id secret options)]
    (clojure.pprint/pprint (GET client "/endpoints"))))
