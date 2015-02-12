(ns clojournal.routes.home
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.util :as util]
            [clojournal.models.article :as article]
            [clojure.string :as str]))

(defn home-page [page]
  (layout/render
    "home.html" {:articles (article/latest-articles page 2)}))

(defn about-page []
  (layout/render "about.html"))

(defn- shorten-content [content]
  (let [content (-> content
                    (str/replace "<div>" "")
                    (str/replace "</div>" ""))]
    (subs content 0 (min (count content) 100))))

(defn search-page [words page]
  (let [{:keys [articles] :as result} (article/search-articles words page 10)
        result (-> result
                   (assoc :articles
                          (map #(update-in % [:content] shorten-content) articles))
                   (assoc :words words))]
    (layout/render "search.html" result)))

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
         (catch NumberFormatException _))))
