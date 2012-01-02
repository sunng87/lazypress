(ns lazypress.views
  (:use [net.cgrand.enlive-html])
  (:use [clojure.string :only [blank?]]))

(deftemplate index "index.html"
  [ctx]
  [:span#user] (content (:author ctx))
  [:span#counter] (content (str (:counter ctx))))

(deftemplate page "page.html"
  [ctx]
  [:div#page-body] (html-content (:content ctx))
  [:a#author-display] (do->
                          (content (if-not (blank? (:author ctx))
                                     (:author ctx) "anonymous"))
                          (set-attr :href
                                    (if-not (blank? (:author ctx))
                                      (str "/a/" (:author ctx))
                                      "#")))
  [:span#pubdate] (content (.toString (:date ctx)))
  [:span#title] (content (if-not (blank? (:title ctx))
                           (:title ctx) "untitled"))
  [:title] (content (:title ctx) " - LazyPress")
  [:input#id] (set-attr :value (:id ctx))
  [:input#author] (set-attr :value (:author ctx))
  [:div#author-box] (if-not (:editable ctx) (html-content "") identity))

(deftemplate edit "edit.html"
  [ctx]
  [:textarea#content] (content (:content ctx))
  [:input#id] (set-attr :value (:id ctx))
  [:input#author] (set-attr :value (:author ctx))
  [:input#title] (set-attr :value (:title ctx))
  [:title] (content (:title ctx) " - LazyPress"))


(defsnippet article-model "author.html"
  [:.article-item]
  [ctx]
  [:a.article-link] (do-> (content (:title ctx))
                          (set-attr :href (str "/p/" (:id ctx))))
  [:span.article-date] (content (.toString (:date ctx))))

(deftemplate author "author.html"
  [ctx]
  [:h2#author-name] (content (:author ctx))
  [:ul#article-list] (content (map article-model (:pages ctx))))


