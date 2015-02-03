(ns clojournal.models.tag
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :as mo]
            [clojournal.db :refer [db]]))

(defn upsert-tags! [tags]
  (doseq [tag tags]
    (mc/update db "tags" {:_id tag} {mo/$inc {:refs 1}} {:upsert true})))

(defn all-tags
  ([] (all-tags 20))
  ([num]
   (mq/with-collection db "tags"
     (mq/find {})
     (mq/sort {:refs -1})
     (mq/limit num))))
