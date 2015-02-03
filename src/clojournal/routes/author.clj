(ns clojournal.routes.author
  (:require [compojure.core :refer :all]
            [clojournal.layout :as layout]
            [clojournal.util :as util]
            [clojournal.models.author :as author]
            [noir.session :as session]
            [noir.response :as response]))

(defn login-page []
  (layout/render
    "login.html" {}))

(defn top-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn wrap-auth [app]
  (fn [req]
    (if (session/get :author)
      (app req)
      (response/redirect "/"))))

(def author-routes*
  (-> (routes
        (GET "/" []
             (top-page)))
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
  (context "/author" req
           (author-routes* req)))
