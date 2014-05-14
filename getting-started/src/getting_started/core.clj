;;; Getting started
(ns getting-started.core
  (:require [spid-client-clojure.core :as spid]))

(defn test-run-api [client-id secret]
  (let [options {:spid-base-url "https://stage.payment.schibsted.no"}
        client (spid/create-client client-id secret options)
        token (spid/create-server-token client)]
    (clojure.pprint/pprint (spid/GET client token "/endpoints"))))
;;;
