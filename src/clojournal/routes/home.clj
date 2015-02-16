(ns clojournal.routes.home
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.util :as util]
            [clojournal.models.article :as article]
            [clojure.string :as str]
            [clj-time
             [core :as t]
             [coerce :as c]])
  (import org.joda.time.DateTime))

(defn home-page [page]
  (layout/render
    "home.html" {:articles (article/latest-articles page 5)}))

(defn about-page []
  (layout/render "about.html"))

(defn- shorten-content [content]
  (let [content (-> content
                    (str/replace "<div>" "")
                    (str/replace "</div>" ""))]
    (subs content 0 (min (count content) 100))))

(defn search-page [words page]
  (let [{:keys [articles] :as result} (article/search-articles words page 10)
        result (assoc result :words words)]
    (layout/render "search.html" result)))

(defn archives-page []
  (let [years (->> (article/all-articles)
                   (map #(t/year (c/from-date (:created-at %))))
                   (into (sorted-set)))]
    (layout/render "archives.html" {:years (rseq years)})))

(defn year-archives-page [year]
  (let [articles (->> (article/find-articles-in-year year)
                      (map #(assoc % :month (t/month (c/from-date (:created-at %)))))
                      (group-by :month)
                      ((fn [as]
                         (->> (for [[k v] as]
                                [k (reverse (sort-by :created-at v))])
                              (into (sorted-map)))))
                      rseq)]
    (layout/render "year-archives.html" {:year year :months articles})))

(defn tags-page [tag]
  (let [articles (article/find-articles-by-tag tag)]
    (layout/render "tags.html" {:tag tag :articles articles})))

(defroutes home-routes
  (GET "/" []
       (home-page 0))
  (GET "/pages/:page" {{:keys [page] :or [page "0"]} :params}
       (try
         (let [page (Long/parseLong page)]
           (home-page page))
         (catch NumberFormatException _)))
  (GET "/about" [] (about-page))
  (GET "/search" {{:keys [q page] :or {page "0"}} :params :as req}
       (try
         (let [page (Long/parseLong page)
               words (str/split q #"\s+")]
           (search-page words page))
         (catch NumberFormatException _)))
  (GET "/archives" []
       (archives-page))
  (GET "/archives/:year" {{:keys [year]} :params}
       (try
         (let [year (Long/parseLong year)]
           (year-archives-page year))
         (catch NumberFormatException _)))
  (GET "/tags/:tag" {{:keys [tag]} :params}
       (tags-page tag)))
