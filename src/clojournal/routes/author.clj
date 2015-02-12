(ns clojournal.routes.author
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.util :as util]
            [clojournal.models.author :as author]
            [clojournal.models.article :as article]
            [noir.session :as session]
            [noir.response :as response]))

(defn login-page []
  (layout/render
    "login.html" {}))

(defn top-page []
  (layout/render
    "author/home.html" {:articles (article/latest-articles 0 10)}))

(defn post-article [author-id title content tags]
  (let [tags' (clojure.string/split tags #"\s*,\s*")]
    (article/add-article! :title title :content content :author author-id :tags tags')
    (response/redirect "/author")))

(defn wrap-auth [app]
  (fn [req]
    (if-let [id (session/get :author)]
      (let [author (author/find-author id)
            req (assoc req :author author)]
        (app req))
      (response/redirect "/"))))

(def author-routes*
  (-> (routes
        (GET "/" []
             (top-page))
        (POST "/article" {{:keys [title content tags]} :params}
              (let [author-id (session/get :author)]
                (post-article author-id title content tags))))
      wrap-auth))

(defn authenticate [email password]
  (if-let [author (author/authenticate email password)]
    (do (session/put! :author (:id author))
        (response/redirect "/author"))
    (response/redirect "/login")))

(defroutes author-routes
  (GET "/login" []
       (login-page))
  (POST "/login" {{:keys [email password]} :params}
        (authenticate email password))
  (context "/author" _
           author-routes*))
