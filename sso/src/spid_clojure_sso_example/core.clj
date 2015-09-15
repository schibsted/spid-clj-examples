(ns spid-clojure-sso-example.core
  (:require [cheshire.core :as json]
            [clj-http.client :as http]
            [compojure.core :refer [defroutes GET]]
            [ring.middleware.params]
            [ring.middleware.session]
            [spid-client-clojure.core :as spid]))

(defonce config (read-string (slurp (clojure.java.io/resource "config.edn"))))

(defonce client-id (:client-id config))
(defonce client-secret (:client-secret config))
(defonce spid-base-url (:spid-base-url config))
(defonce our-base-url (:our-base-url config))

;;; Build login URL
(def create-session-url (str our-base-url "/create-session"))

(def authorize-url
  (str spid-base-url "/flow/auth"
       "?client_id=" client-id
       "&response_type=code"
       "&redirect_uri=" create-session-url))
;;;

(defn get-index [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body
   (if-let [name (-> request :session :user :displayName)]
     (str "Hello " name "! <a href='/logout'>Log out</a>")
     (str "<a href='" authorize-url "'>Log in with SPiD</a>"))})

;;; Create user client
(defn create-client []
  (spid/create-client client-id client-secret
                      {:spid-base-url spid-base-url
                       :redirect-uri create-session-url}))
;;;

;;; Fetch user information and add to session
(defn create-session [code]
  (let [client (create-client)
        token (spid/create-user-token client code)
        user (:data (spid/GET client token "/me"))]
    {:status 302
     :headers {"Location" "/"}
     :session {:token token
               :user user}}))
;;;

;;; Log user out
(defn get-logout-url [request]
  (str spid-base-url "/logout"
       "?redirect_uri=" our-base-url
       "&oauth_token=" (-> request :session :token)))

(defn log-user-out [request]
  {:status 302
   :headers {"Location" (get-logout-url request)}
   :session {}})
;;;

(defroutes routes
  (GET "/" request (get-index request))
  (GET "/create-session" [code] (create-session code))
  (GET "/logout" request (log-user-out request)))

(def app
  (-> routes
      (ring.middleware.params/wrap-params)
      (ring.middleware.session/wrap-session)))
