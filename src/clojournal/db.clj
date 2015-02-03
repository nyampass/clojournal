(ns clojournal.db
  (:require [monger.core :as mg]
            [environ.core :as env]))

(defonce db
  (let [uri (:mongo-url env/env)
        {:keys [conn db]} (mg/connect-via-uri uri)]
    db))
