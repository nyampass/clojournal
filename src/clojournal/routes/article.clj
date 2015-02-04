(ns clojournal.routes.article
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.models.article :as article]
            [clojournal.models.author :as author]))

(defn article-page [id]
  (when-let [article (article/find-article id)]
    (let [author (author/find-author (:author article))]
      (layout/render
        "article.html"
        {:title (:title article)
         :content (:content article)
         :author (:name author)
         :tags (clojure.string/join ", " (:tags article))
         :updated-at (:updated-at article)}))))

(defroutes article-routes
  (GET "/entry/:id" [id]
       (article-page id)))
