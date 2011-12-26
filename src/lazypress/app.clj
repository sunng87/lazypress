(ns lazypress.app
  (:use [lazypress vmc utils])
  (:use [compojure core route handler])
  (:use [net.cgrand.enlive-html])
  (:use [somnium.congomongo])
  (:use [ring.util.response])
  (:use [clojure.string :only [blank? lower-case]])
  (:import [java.util Date])
  (:import [org.apache.commons.codec.digest DigestUtils]))

(declare db-conn)

(deftemplate index "index.html"
  []
  )
(deftemplate page "page.html"
  [ctx]
  [:div#page-body] (html-content (:content ctx))
  [:span#author_display] (content (if-not (blank? (:author ctx))
                            (:author ctx) "anonymous"))
  [:span#pubdate] (content (.toString (:date ctx)))
  [:span#title] (content (if-not (blank? (:title ctx))
                           (:title ctx) "untitled"))
  [:title] (content (:title ctx) " - LazyPress")
  [:input#id] (set-attr :value (:id ctx))
  [:input#author] (set-attr :value (:author ctx)))

(deftemplate edit "edit.html"
  [ctx]
  [:textarea#content] (content (:content ctx))
  [:input#id] (set-attr :value (:id ctx))
  [:input#author] (set-attr :value (:author ctx))
  [:input#title] (set-attr :value (:title ctx))
  [:title] (content (:title ctx) " - LazyPress"))

(defn view-index [req]
  (index))

(defn view-post [req]
  (let [id (-> req :params :id)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (if (nil? page-obj)
      {:status 404}
      (page (assoc page-obj :content (md->html (:content page-obj)))))))

(defn- article-id [id]
  (if (blank? id)
    (let [id-obj (with-mongo db-conn
                   (fetch-and-modify :counter
                                     {:name "post-key"}
                                     {:$inc {:counter 1}}))]
      (base62 (long (:counter id-obj))))
    id))

(defn save-post [req]
  (let [{content :content title :title
         uid :author rid :id} (:params req)
        id (article-id rid)
        page {:content content :id id :title title
              :author uid :date (Date.)}]
    (if (blank? rid)
      (if (or (blank? uid) (= uid (-> req :session :author)))
        (do
          (with-mongo db-conn (insert! :pages page))
          (json-response {:result "ok" :id id} nil))
        (do
          (json-response {:result "failed"} nil)))
      (do
        (with-mongo db-conn
          (update! :pages {:id rid} page))
        (json-response {:result "ok" :id rid})))))

(defn preview-post [req]
  (let [content (-> req :params :content)]
    (md->html content)))

(defn edit-post [req]
  (let [id (-> req :params :id)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (edit page-obj)))

(defn delete-post [req]
  (let [id (-> req :params :id)
        user (-> req :session :author)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (if (= user (:author page-obj))
      (do
        (with-mongo db-conn
          (destroy! :pages page-obj))
        (json-response {:result "ok"}))
      (do
        (json-response {:result "failed"})))))

(defn login [req]
  (if (blank? (-> req :params :author))
    (json-response {:result "failed"})
    (let [{user :author passwd :password} (:params req)
          epasswd (DigestUtils/sha256Hex passwd)
          user-obj (with-mongo db-conn
                     (fetch-one :authors :where {:id (lower-case user)}))]
      (if (nil? user-obj)
        (do
          (with-mongo db-conn
            (insert! :authors {:id (lower-case user)
                               :passwd epasswd
                               :display user}))
          (assoc (json-response {:result "ok" :id user} nil)
            :session {:author user}))
        (do
          (if (= epasswd (:passwd user-obj))
            (assoc (json-response {:result "ok" :id user} nil)
              :session {:author user})
            (json-response {:result "failed"} nil)))))))

(defroutes lazypress-routes
  (GET "/" [] view-index)
  (GET "/p/:id" [] view-post)
  (GET "/e/:id" [] edit-post)
  (POST "/d/:id" [] delete-post)
  (POST "/login" [] login)
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

