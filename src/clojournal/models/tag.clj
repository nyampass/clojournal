(ns clojournal.models.tag
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :as mo]
            [clojournal.db :refer [db]])
  (:import com.mongodb.DB))

(defn upsert-tags! [tags]
  (doseq [tag tags]
    (mc/update db "tags" {:_id tag} {mo/$inc {:refs 1}} {:upsert true})))

(defn all-tags
  ([] (all-tags 20))
  ([limit]
   (mq/with-collection #^DB db "tags"
     (mq/find {})
     (mq/sort {:refs -1})
     (mq/limit limit))))

(defn tag-cloud
  ([] (tag-cloud 100))
  ([limit]
   (let [tags (mq/with-collection #^DB db "tags"
                (mq/find {})
                (mq/sort {:_id 1})
                (mq/limit limit))
         refs (map :refs tags)]
     (when (seq refs)
       (let [min (double (apply min refs))
             max (double (apply max refs))
             f (fn [n] (if (= min max) 1 (/ (- n min) (- max min))))]
         (map #(assoc % :group (f (:refs %))) tags))))))
