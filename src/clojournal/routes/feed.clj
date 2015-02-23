(ns clojournal.routes.feed
  (:require [compojure.core :refer :all]
            [clojure.string :as st]
            [clj-rss.core :as rss]
            [clojournal.models.article :as article]
            [clojournal.models.author :as author]
            [clj-time.local :as local]))

(defn article->entry [article]
  (let [author (author/find-author (:author article))]
    {:title (:title article)
     ;; FIXME: Don't hardcode hostname
     :link (str "http://clojournal.com/entry/" (:id article))
     :description (:content article)
     :category (st/join ", " (:tags article))
     :pubDate (:updated-at article)}))

(defn render-feed []
  {:headers {"Content-Type" "application/rss+xml; charset=utf-8"}
   :body (rss/channel-xml
          {:title "clojournal" :link "http://clojournal.com/"
           :description "日本のClojureユーザのための情報サイト"}
          (map article->entry
               (:articles (article/latest-articles 0 20))))})

(defroutes feed-routes
  (GET "/feed" []
       (render-feed)))
