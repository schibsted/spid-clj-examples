;;; Getting started
(ns getting-started.core
  (:require [spid-client-clojure.core :as spid]))

(defn test-run-api [client-id secret]
  (let [options {:spid-base-url "identity-pre.schibsted.com"}
        client (spid/create-client client-id secret options)
        token (spid/create-server-token client)]
    (clojure.pprint/pprint (spid/GET client token "/endpoints"))))
;;;
