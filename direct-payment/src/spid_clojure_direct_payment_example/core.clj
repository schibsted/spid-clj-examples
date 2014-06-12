(ns spid-clojure-direct-payment-example.core
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]
            [clojure.set :as set]
            [clojure.string :as str]
            [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.params]
            [ring.middleware.session]
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
(defonce our-base-url (:our-base-url config))
(defonce create-session-url (str our-base-url "/create-session"))

(defonce authorize-url
  (str spid-base-url "/oauth/authorize"
       "?client_id=" client-id
       "&response_type=code"
       "&redirect_uri=" create-session-url))

(defonce client (spid/create-client client-id
                                    client-secret
                                    {:spid-base-url spid-base-url
                                     :redirect-uri create-session-url}))

(defonce token (spid/create-server-token client))

;;; The entirety of our product catalog right here
(defonce products {"sw4" {:description "Star Wars IV" :price 9900 :vat 2400}
                   "sw5" {:description "Star Wars V"  :price 9900 :vat 2400}
                   "sw6" {:description "Star Wars VI" :price 9900 :vat 2400}})
;;;

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
   :redirectUri (str our-base-url "/success")
   :cancelUri (str our-base-url "/cancel")
   :clientReference (str "Order number " (rand-int 100000))
   :items (create-paylink-items products)})
;;;

;;; Create data to POST to /user/{userId}/charge
(defn create-order-data [user-id products sign-secret]
  (spid/sign-params
   {:requestReference (str "Order for " user-id)
    :items (create-order-items products)}
   sign-secret))

(defn create-order-items [products]
  (json/write-str
   (->> products
        (keep prepare-paylink-item)
        (map #(set/rename-keys % {:description :name})))))
;;;

(defn create-paylink [products]
  (:data (spid/POST client token "/paylink" (create-paylink-data products))))

(defn get-order [order-id]
  (:data (spid/GET client token (str "/order/" order-id "/status"))))

(defn serve-page [body]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body body})

(defn redirect-to [url & [response]]
  (merge {:status 302 :headers {"Location" url}} response))

(defn render-template [template data]
  (reduce (fn [tpl [key val]]
            (str/replace tpl (str "#{" (name key) "}") val))
          template data))

(defn render-template-resource [file data]
  (render-template (slurp (io/resource file)) data))

(defn get-index [request]
  (serve-page
   (if-let [name (-> request :session :user :displayName)]
     (render-template-resource "index.html" {:name name})
     (str "<a href='" authorize-url "'>Log in with SPiD</a>"))))

(defn create-session [code]
  (let [token (spid/create-user-token client code)
        result (spid/GET client token "/me")
        user (:data result)]
    (redirect-to "/" {:session {:token token :user user}})))

(defn log-user-out [request]
  (redirect-to (str spid-base-url "/logout"
                    "?redirect_uri=" our-base-url
                    "&oauth_token=" (-> request :session :token))
               {:session nil}))

;;; Attempting the direct payment, with a Paylink fallback
(defn checkout [request]
  (let [products (:params request)
        user-id (-> request :session :user :userId)
        params (create-order-data user-id products sign-secret)
        charge-url (str "/user/" user-id "/charge")
        result (spid/POST client token charge-url params)]
    (if (:success? result)
      (serve-page
       (str "<h1>Success!</h1>"
            "<p>You are now the proud owner of some quality movies. This is what SPiD says:</p>"
            "<pre>" (with-out-str (json/pprint (:data result))) "</pre>"
            "<p><a href=\"/\">Go again</a></p>"))
      (redirect-to (-> products create-paylink :shortUrl)))))
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
  (GET "/" request (get-index request))
  (GET "/create-session" [code] (create-session code))
  (GET "/logout" request (log-user-out request))
  (POST "/checkout" request (checkout request))
  (GET "/success" [order_id] (success order_id))
  (GET "/cancel" [spid_page] (cancel spid_page)))

(def app
  (-> routes
      (ring.middleware.params/wrap-params)
      (ring.middleware.session/wrap-session)))
