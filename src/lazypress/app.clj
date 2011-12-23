(ns lazypress.app
  (:use [lazypress vmc utils])
  (:use [compojure core route handler])
  (:use [net.cgrand.enlive-html])
  (:use [somnium.congomongo])
  (:use [ring.util.response])
  (:use [clj-markdown.core]))

(declare db-conn)

(deftemplate index "index.html"
  []
  )
(deftemplate page "page.html"
  [ctx]
  [:div#page-body] (html-content (:content ctx)))

(defn view-index [req]
  (index))

(defn view-post [req]
  (let [id (-> req :params :id)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (page (assoc page-obj :content (md->html (:content page-obj))))))

(defn save-post [req]
  (let [content (-> req :params :content)
        id-obj (with-mongo db-conn
                 (fetch-and-modify :counter
                                   {:name "post-key"}
                                   {:$inc {:counter 1}}))
        id (base62 (long (:counter id-obj)))]
    (with-mongo db-conn
      (insert! :pages {:content content :id id}))
    (json-response {:result "ok" :id id} nil)))

(defn preview-post [req]
  (let [content (-> req :params :content)]
    (md->html content)))

(defroutes lazypress-routes
  (GET "/" [] view-index)
  (GET "/v/:id" [] view-post)
  (POST "/save" [] save-post)
  (POST "/preview" [] preview-post)
  (resources "/"))

(defn app-init []
  (def db-conn (make-connection
                (or (mongo-config "db") "lazypress")
                :host (or (mongo-config "hostname") "localhost")
                :port (or (mongo-config "port") 27017)
                :username (or (mongo-config "username") "")
                :password (or (mongo-config "password") "")))
  (when-not (nil? (mongo-config "username"))
    (authenticate db-conn
                  (mongo-config "username")
                  (mongo-config "password")))
  (with-mongo db-conn
    (update! :counter {:name "post-key"}
             {:$inc {:counter 1}} :upsert? true)))

(def app
  (site lazypress-routes))

