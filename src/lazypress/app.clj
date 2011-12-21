(ns lazypress.app
  (:use [compojure core route handler])
  (:require [ring.utils.response :as ring]))


(defroute lazypress-routes
  (GET "/" [] (ring/redirect "/index.html"))
  (route/resources "/"))

(def app
  (site lazypress-routes))

