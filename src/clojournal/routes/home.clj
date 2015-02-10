(ns clojournal.routes.home
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.util :as util]
            [clojournal.models.article :as article]))

(defn home-page [page]
  (layout/render
    "home.html" {:articles (article/latest-articles page 2)}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" []
       (home-page 0))
  (GET "/pages/:page" {{:keys [page] :or [page "0"]} :params}
       (try
         (let [page (Long/parseLong page)]
           (home-page page))
         (catch NumberFormatException _)))
  (GET "/about" [] (about-page)))
