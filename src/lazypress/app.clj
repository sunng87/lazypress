(ns lazypress.app
  (:use [lazypress vmc])
  (:use [compojure core route handler])
  (:use [net.cgrand.enlive-html])
  (:require [somnium.congomongo :as mongo]))

(declare db-conn)

(deftemplate index "index.html"
  []
  )
(deftemplate post "page.html"
  [ctx]
  [:div#page-body] (:content ctx))

(defn view-index [req]
  (index))
(defn view-post [req]
  )

(defroutes lazypress-routes
  (GET "/" [] view-index)
  (GET "/v/:id" [] view-post)
  (resources "/"))

(defn app-init []
  (def db-conn (mongo/make-connection
                (or (mongo-config "db") "lazypress")
                :host (or (mongo-config "hostname") "localhost")
                :port (or (mongo-config "port") 27017)
                :username (or (mongo-config "username") "")
                :password (or (mongo-config "password") ""))))

(def app
  (site lazypress-routes))

