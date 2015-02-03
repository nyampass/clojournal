(ns clojournal.models.author
  (:require [monger.collection :as mc]
            [monger.query :as mq]
            [monger.operators :as mo]
            [clojournal.util :as util]
            [clojournal.db :refer [db]]
            [crypto.password.bcrypt :refer [encrypt check]]))

(defn- fix-author [author]
  (-> (util/fix-object author)
      (dissoc :password)))

(defn find-author [id]
  (fix-author (mc/find-one-as-map db "authors" {:_id id})))

(defn register-author! [& {:keys [name email password]}]
  (assert (and (seq name) (seq email) (seq password)))
  (let [password (encrypt password)
        author {:_id email :name name
                :created-at (java.util.Date.) :password password}]
    (-> (mc/insert-and-return db "authors" author)
        fix-author)))

(defn authenticate [email password]
  (let [{crypted-pass :password :as author}  (mc/find-one-as-map db "authors" {:_id email})]
    (when (check password crypted-pass)
      (fix-author author))))
