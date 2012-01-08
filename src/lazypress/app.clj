(ns lazypress.app
  (:use [lazypress vmc utils views browserid])
  (:use [compojure core route handler])
  (:use [somnium.congomongo])
  (:use [clojure.string :only [blank? lower-case]])
  (:import [java.util Date])
  (:import [com.sun.syndication.feed.rss Channel Item Content])
  (:import [com.sun.syndication.io WireFeedOutput]))

(def web-root "http://lazypress.cloudfoundry.com/")
(declare db-conn)

(defn view-index [req]
  (index {:user (-> req :session :author)}))

(defn view-post [req]
  (let [id (-> req :params :id)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (if (nil? page-obj)
      {:status 404}
      (page (assoc page-obj
              :content (md->html (:content page-obj))
              :editable (= (:author page-obj) (-> req :session :author))
              :user (-> req :session :author))))))

(defn- article-id [id]
  (if (blank? id)
    (let [id-obj (with-mongo db-conn
                   (fetch-and-modify :counter
                                     {:name "post-key"}
                                     {:$inc {:counter 1}}))]
      (base62 (long (:counter id-obj))))
    id))

(defn save-post [req]
  (let [{content :content title :title rid :id} (:params req)
        id (article-id rid)
        session-author (-> req :session :author)
        page {:content content :id id :title title
              :author session-author :date (Date.)}]
    (if (blank? rid)
      (do
        (with-mongo db-conn (insert! :pages page))
        (json-response {:result "ok" :id id} nil))
      (do        
        (with-mongo db-conn
          (update! :pages {:id rid :author session-author} page))
        (json-response {:result "ok" :id rid})))))

(defn preview-post [req]
  (let [content (-> req :params :content)]
    (md->html content)))

(defn edit-post [req]
  (let [id (-> req :params :id)
        page-obj (with-mongo db-conn
                   (fetch-one :pages :where {:id id}))]
    (edit (assoc page-obj :user (-> req :session :author)))))

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
  (let [assertion (-> req :params :assertion)
        result (verify assertion)]
    (if (= "okay" (:status result))
      (do
        (assoc
          (json-response {:result "ok" :id (:email result)})
          :session {:author (:email result)}))
      (do
        (json-response {:result "failed"})))))

(defn logout [req]
  (assoc
      (json-response {:result "ok"})
    :session {:author nil}))

(defn view-author [req]
  (let [uid (-> req :params :id)
        pages (with-mongo db-conn
                (fetch :pages
                       :where {:author uid}
                       :only [:id :title :date]
                       :sort {:date -1}))]
    (author {:author uid :pages pages
             :user (-> req :session :author)})))

(defn view-author-atom [req]
  (let [uid (-> req :params :id)
        pages (with-mongo db-conn
                (fetch :pages
                       :where {:author uid}
                       :sort {:date -1}
                       :limit 20))]
    (.outputString (WireFeedOutput.)
                   (doto (Channel. "rss_2.0")
                     (.setLink (str web-root "a/" uid))
                     (.setLastBuildDate (:date (first pages)))
                     (.setTitle (str "LazyPress: " uid))
                     (.setDescription (str uid " on LazyPress"))
                     (.setItems
                      (map #(doto (Item.)
                              (.setPubDate (:date %))
                              (.setTitle (:title %))
                              (.setLink (str web-root "p/" (:id %)))
                              
                              (.setContent
                                (doto (Content.)
                                   (.setType Content/HTML)
                                   (.setValue (md->html (:content %))))))
                           pages))))))

(defroutes lazypress-routes
  (GET "/" [] view-index)
  (GET "/p/:id" [] view-post)
  (GET "/a/:id" [] view-author)
  (GET "/a/:id/feed" [] view-author-atom)
  (GET "/e/:id" [] edit-post)
  (POST "/d/:id" [] delete-post)
  (POST "/login" [] login)
  (POST "/logout" [] logout)
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
  ;; create counter if not exists
  (when (nil? (with-mongo db-conn
                (fetch-one :counter :where {:name "post-key"})))
    (with-mongo db-conn
      (update! :counter {:name "post-key"}
               {:$inc {:counter 1}} :upsert? true))))

(def app
  (site lazypress-routes))

