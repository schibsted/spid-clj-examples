(ns spid-clojure-direct-payment-example.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.string :as str]
            [spid-client-clojure.core :as spid]))

(defonce config
  (try
    (read-string (slurp (clojure.java.io/resource "config.edn")))
    (catch Exception e
      (prn "Make sure you have a valid configuration in config/config.edn")
      (System/exit 0))))

(defonce client-id (:client-id config))
(defonce client-secret (:client-secret config))
(defonce sign-secret (:client-sign-secret config))
(defonce spid-base-url (:spid-base-url config))

(defonce client (spid/create-client client-id
                                    client-secret
                                    {:spid-base-url spid-base-url
                                     :redirect-uri "http://localhost:8080"}))

(defonce token (spid/create-server-token client))

;;; Order status codes
(defonce order-status {"-3" "Expired"
                       "-2" "Cancelled"
                       "-1" "Failed"
                       "0" "Created"
                       "1" "Pending"
                       "2" "Complete"
                       "3" "Credited"
                       "4" "Authorized"})
;;;

;;; Payment identifier types
(defonce payment-identifier-types {"2" "Credit card"
                                   "4" "SMS"
                                   "8" "PayEx Invoice"
                                   "16" "Voucher"
                                   "32" "Klarna Invoice"})
;;;

;;; Create data to POST to /user/{userId}/charge
(defn create-order-data [user-id subscription sign-secret]
  (spid/sign-params
   {:requestReference (str "Order #" (rand-int 100000))
    :items (json/write-str [subscription])}
   sign-secret))
;;;

(defn render-template [template data]
  (reduce (fn [tpl [key val]]
            (str/replace tpl (str "#{" (name key) "}") (str (or val ""))))
          template data))

(defn render-template-resource [file data]
  (render-template (slurp (io/resource file)) data))

;;; Extracting order data for the order summary view
(defn get-order-view [order]
  {:clientReference (:clientReference order)
   :status (order-status (:status order))
   :currency (:currency order)
   :totalPrice (format "%.2f" (/ (read-string (:totalPrice order)) 100.0))
   :paymentIdentifier (payment-identifier-types (:identifierType order))})
;;;

;;; Attempting the direct payment
(defn charge-user-for-subscription [user-id subscription]
  (let [params (create-order-data user-id subscription sign-secret)
        charge-url (str "/user/" user-id "/charge")
        result (spid/POST client token charge-url params)]
    (if (:success? result)
      (render-template-resource "receipt.txt" (get-order-view (:data result)))
      (str "Failed to charge user " result))))
;;;

;; If you want to run this sample, insert a known existing user id in the
;; subscribers vector below.

(defn -main [& [args]]
;;; Charge subscribers
  (let [subscribers [{:user-id 238342}]
        subscription {:name "Ants Monthly" :price 9900 :vat 2400}]
    (doseq [user-id (map :user-id subscribers)]
      (print "User ID" (str user-id ":") (charge-user-for-subscription user-id subscription))))
;;;
)
