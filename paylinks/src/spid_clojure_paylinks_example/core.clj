(ns spid-clojure-paylinks-example.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.params]
            [ring.middleware.session]
            [spid-client-clojure.core :as spid]))

(defonce config (read-string (slurp (clojure.java.io/resource "config.edn"))))

(defonce client-id (:client-id config))
(defonce client-secret (:client-secret config))
(defonce spid-base-url (:spid-base-url config))
(defonce our-base-url (:our-base-url config))

;;; Create SPiD client
(defonce client (spid/create-client client-id
                                    client-secret
                                    {:spid-base-url spid-base-url}))

(defonce token (spid/create-server-token client))
;;;

;;; Order status codes
(def order-status {"-3" "Expired"
                   "-2" "Cancelled"
                   "-1" "Failed"
                   "0" "Created"
                   "1" "Pending"
                   "2" "Complete"
                   "3" "Credited"
                   "4" "Authorized"})
;;;

;;; The entirety of our product catalog right here
(def products {"sw4" {:description "Star Wars IV" :price 9900 :vat 2400}
               "sw5" {:description "Star Wars V"  :price 9900 :vat 2400}
               "sw6" {:description "Star Wars VI" :price 9900 :vat 2400}})
;;;

;;; Create data to POST to /paylink
(defn prepare-paylink-item [[product-id quantity]]
  (when (> (Integer/parseInt quantity) 0)
    (assoc (products product-id)
      :quantity quantity)))

(defn create-paylink-items [products]
  (json/write-str
   (keep prepare-paylink-item products)))

(defn create-paylink-data [products]
  {:title "Quality movies"
   :redirectUri (str our-base-url "/callback")
   :cancelUri (str our-base-url "/cancel")
   :clientReference (str "Order number " (rand-int 100000))
   :items (create-paylink-items products)})
;;;

;;; Create Paylink
(defn create-paylink [products]
  (:data (spid/POST client token "/paylink" (create-paylink-data products))))
;;;

(defn serve-page [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body body})

(defn redirect-to [url]
  {:status 302
   :headers {"Location" url}})

(defn get-index []
  (serve-page (slurp (io/resource "index.html"))))

;;; Create Paylink and redirect to SPiD
(defn checkout [request]
  (redirect-to (-> request :params create-paylink :shortUrl)))
;;;

;;; Fetch order info
(defn get-order [order-id]
  (:data (spid/GET client token (str "/order/" order-id "/status"))))
;;;

(defn success [order-id request]
  (let [order (get-order order-id)
        name (-> request :session :user :displayName)]
    (serve-page
     (str "<h1>Welcome back, " name "!</h1>"
          "<p>"
          (:clientReference order)
          " is "
          "<strong>" (order-status (:status order)) "</strong> "
          "</p>"))))

(defn create-client []
  (spid/create-client client-id client-secret
                      {:spid-base-url spid-base-url
                       :redirect-uri our-base-url}))

;;; Handle callback from SPiD, make sure we've got the right user
(defn callback [code order-id]
  (let [client (create-client)
        token (spid/create-user-token client code)
        user (:data (spid/GET client token "/me"))]
    {:status 302
     :headers {"Location" (str "/success?order-id=" order-id)}
     :session {:token token
               :user user}}))
;;;

(defn cancel [cancel-page]
  (serve-page
   (str "<h1>Cancelled</h1>"
        "<p>You left at "
        "<strong>" cancel-page "</strong>."
        "</p>")))

(defroutes routes
  (GET "/" request (get-index))
  (POST "/checkout" request (checkout request))
  (GET "/success" [order-id :as request] (success order-id request))
  (GET "/callback" [code order_id] (callback code order_id))
  (GET "/cancel" [spid_page] (cancel spid_page)))

(def app
  (-> routes
      (ring.middleware.params/wrap-params)
      (ring.middleware.session/wrap-session)))
