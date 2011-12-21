(ns lazypress.app
  (:use [compojure core route handler])
  (:use [net.cgrand.enlive-html]))


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

(def app
  (site lazypress-routes))

