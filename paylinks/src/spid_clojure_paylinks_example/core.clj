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

(def order-status {"-3" "Expired"
                   "-2" "Cancelled"
                   "-1" "Failed"
                   "0" "Created"
                   "1" "Pending"
                   "2" "Complete"
                   "3" "Credited"
                   "4" "Authorized"})

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

(defn create-paylink-data [products]
  {:title "Quality movies"
   :redirectUri (str our-base-url "/success")
   :cancelUri (str our-base-url "/cancel")
   :clientReference (str "Order number " (rand-int 100000))
   :items (json/write-str (keep prepare-paylink-item products))})
;;;

;;; Create Paylink
(defn create-paylink [products]
  (:data (spid/POST client token "/paylink" (create-paylink-data products))))
;;;

(defn get-order [order-id]
  (:data (spid/GET client token (str "/order/" order-id "/status"))))

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

(defn success [order-id]
  (let [order (get-order order-id)]
    (serve-page
     (str "<h1>Success!</h1>"
          "<p>"
          (:clientReference order)
          " is "
          "<strong>" (order-status (:status order)) "</strong> "
          "</p>"))))

(defn cancel [cancel-page]
  (serve-page
   (str "<h1>Cancelled</h1>"
        "<p>You left at "
        "<strong>" cancel-page "</strong>."
        "</p>")))

(defroutes routes
  (GET "/" request (get-index))
  (POST "/checkout" request (checkout request))
  (GET "/success" [order_id] (success order_id))
  (GET "/cancel" [spid_page] (cancel spid_page)))

(def app
  (-> routes
      (ring.middleware.params/wrap-params)
      (ring.middleware.session/wrap-session)))
