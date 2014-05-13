(ns getting-started.core
  (:require [spid-sdk-clojure.core :as sdk]))

(defn test-run-api [client-id secret]
  (let [options {:spid-base-url "https://stage.payment.schibsted.no"}
        client (sdk/create-client client-id secret options)
        token (sdk/create-server-token client)]
    (clojure.pprint/pprint (sdk/GET client token "/endpoints"))))
