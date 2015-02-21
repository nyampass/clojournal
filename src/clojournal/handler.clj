(ns clojournal.handler
  (:require [compojure.core :refer [defroutes]]
            [clojournal.routes.home :refer [home-routes]]
            [clojournal.routes.author :refer [author-routes]]
            [clojournal.routes.article :refer [article-routes]]
            [clojournal.routes.feed :refer [feed-routes]]
            [clojournal.middleware :refer [load-middleware]]
            [clojournal.session-manager :as session-manager]
            [clojournal.db :as db]
            [noir.response :refer [redirect]]
            [noir.util.middleware :refer [app-handler]]
            [ring.middleware.defaults :refer [site-defaults]]
            [compojure.route :as route]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.rotor :as rotor]
            [selmer.parser :as parser]
            [selmer.filters :as filter]
            [environ.core :refer [env]]
            [cronj.core :as cronj]
            [clojure.string :as str]))

(defroutes base-routes
  (route/resources "/")
  (route/not-found "Not Found"))

;; Selmer filters
(defn remove-all-tags [x]
  (str/replace x #"<[^>]+?>" (fn [[_ tag name]] (if (= name "br") tag ""))))

(defn shorten-content [x n]
  (let [n (if (instance? String n)
            (Long/parseLong n)
            n)]
    (subs x 0 (min (count x) n))))

(defn digest-article [[article n] context]
  (let [{:keys [id content] :as article} (context (keyword article))
        content (remove-all-tags content)
        n (cond (nil? n) 200
                (instance? String n) (Long/parseLong n)
                :else n)]
    (if (< (count content) n)
      content
      (str (shorten-content content n)
           "... <a href=\"/entry/" id "\">[続きを読む]</a>"))))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  []
  (timbre/set-config!
    [:appenders :rotor]
    {:min-level :info
     :enabled? true
     :async? false ; should be always false for rotor
     :max-message-per-msecs nil
     :fn rotor/appender-fn})

  (timbre/set-config!
    [:shared-appender-config :rotor]
    {:path "clojournal.log" :max-size (* 512 1024) :backlog 10})

  (if (env :dev) (parser/cache-off!))
  ;;start the expired session cleanup job
  (cronj/start! session-manager/cleanup-job)
  (db/create-indexes)
  ;; register Selmer filters
  (filter/add-filter! :remove-all-tags remove-all-tags)
  (filter/add-filter! :shorten shorten-content)
  (parser/add-tag! :digest-article digest-article)
  (timbre/info "\n-=[ clojournal started successfully"
               (when (env :dev) "using the development profile") "]=-"))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (timbre/info "clojournal is shutting down...")
  (cronj/shutdown! session-manager/cleanup-job)
  (timbre/info "shutdown complete!"))

(def session-defaults
  {:timeout (* 60 60 3)
   :timeout-response (redirect "/")})

(defn- mk-defaults
       "set to true to enable XSS protection"
       [xss-protection?]
       (-> site-defaults
           (update-in [:session] merge session-defaults)
           (assoc-in [:security :anti-forgery] xss-protection?)))

(def app (app-handler
           ;; add your application routes here
           [#'author-routes #'article-routes #'feed-routes home-routes base-routes]
           ;; add custom middleware here
           :middleware (load-middleware)
           :ring-defaults (mk-defaults false)
           ;; add access rules here
           :access-rules []
           ;; serialize/deserialize the following data formats
           ;; available formats:
           ;; :json :json-kw :yaml :yaml-kw :edn :yaml-in-html
           :formats [:json-kw :edn :transit-json]))
