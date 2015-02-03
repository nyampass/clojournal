(ns clojournal.util
  (:require [noir.io :as io]
            [markdown.core :as md]))

(defn md->html
  "reads a markdown file from public/md and returns an HTML string"
  [filename]
  (md/md-to-html-string (io/slurp-resource filename)))

(defn fix-object [object]
  (let [id (:_id object)]
    (-> object
        (assoc :id (str id))
        (dissoc :_id))))
