(ns clojournal.models.article
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :as mo]
            [clojournal.util :as util]
            [clojournal.db :refer [db]]
            [clojournal.models.tag :refer [upsert-tags!]])
  (:import java.util.Date
           org.bson.types.ObjectId))

(defn- fix-article [article]
  (util/fix-object article))

(defn latest-articles [num]
  (->> (mq/with-collection db "articles"
         (mq/find {})
         (mq/sort (array-map :created-at -1))
         (mq/limit num))
       (map fix-article)))

(defn find-article [id]
  (fix-article (mc/find-one-as-map db "articles" {:_id (ObjectId. id)})))

(defn add-article! [& {:keys [title content author tags] :as article}]
  (assert (and (seq title) (seq content) (seq author) (>= (count tags) 1)))
  (let [now (Date.)
        tags' (distinct tags)
        article (assoc article :tags tags' :created-at now :updated-at now)]
    (upsert-tags! tags')
    (fix-article (mc/insert-and-return db "articles" article))))

(defn update-article! [& {:keys [id title content author tags] :as article}]
  (assert (and (seq id) (seq title) (seq content) (seq author) (>= (count tags) 1)))
  (let [now (Date.)
        {old-tags :tags} (find-article id)
        tags' (distinct tags)
        tags-diff (remove (set old-tags) tags')
        article (-> article
                    (dissoc :id)
                    (assoc :tags tags' :updated-at now))]
    (upsert-tags! tags-diff)
    (mc/update-by-id db "articles" (ObjectId. id) {mo/$set article})))
