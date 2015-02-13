(ns clojournal.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [environ.core :as env]))

(defonce db
  (let [uri (:mongo-url env/env)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    db))

(defn create-indexes []
  (mc/ensure-index db "articles" (array-map :title 1) {:name "by-title"})
  (mc/ensure-index db "articles" (array-map :content 1) {:name "by-content"})
  (mc/ensure-index db "articles" (array-map :author 1) {:name "by-author"})
  (mc/ensure-index db "articles" (array-map :tags 1) {:name "by-tags"}))
