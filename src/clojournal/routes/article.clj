(ns clojournal.routes.article
  (:require [compojure.core :refer :all]
            [clojure.string :as str]
            [clojournal.layout :as layout]
            [clojournal.models.article :as article]
            [clojournal.models.author :as author]
            [noir.session :as session]
            [clj-time.local :as local]))

(defn parse-content [content]
  (lazy-seq
    (let [[_ pre code post] (re-matches #"(?s)(?:(.*?)(?:```(.*?)```))?(.*)" content)]
      (cond (= post "") nil
            (nil? code) [[post nil]]
            :else (cons [pre code] (parse-content post))))))

(defn preprocess-content [content]
  (let [content (-> content
                    (str/replace "<div>" "")
                    (str/replace "</div>" "\n"))]
    (->> (for [[text code] (parse-content content)
               :let [text (-> text
                              (str/replace "\n" "")
                              (str/replace "<br>" "</p>\n<p>"))]]
           (if code
             (let [code (str/replace code "&nbsp;" " ")]
               (format "%s</p>\n<pre class=\"brush: clojure\">%s</pre>\n<p>" text code))
             text))
         str/join
         (format "<p>%s</p>")
         ((fn [s] (str/replace s "<p></p>" ""))))))

(defn article-page [id]
  (when-let [article (article/find-article id)]
    (let [author (author/find-author (:author article))
          path (str "entry/" id)]
      (layout/render
        "article.html"
        {:page-title (str (:title article) " - clojournal")
         :page-description (:content article)
         :page-path path
         :author? (boolean (session/get :author))
         :title (:title article)
         :content (preprocess-content (:content article))
         :author (:name author)
         :tags (str/join ", " (:tags article))
         :path path
         :updated-at (local/to-local-date-time (:updated-at article))}))))

(defroutes article-routes
  (GET "/entry/:id" [id]
       (article-page id)))
