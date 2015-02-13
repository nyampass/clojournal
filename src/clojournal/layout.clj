(ns clojournal.layout
  (:require [selmer.parser :as parser]
            [clojure.string :as s]
            [ring.util.response :refer [content-type response]]
            [compojure.response :refer [Renderable]]
            [environ.core :refer [env]]
            [clojournal.models.article :as article]
            [clojournal.models.tag :as tag]))

(parser/set-resource-path!  (clojure.java.io/resource "templates"))

(deftype RenderableTemplate [template params]
  Renderable
  (render [this request]
    (content-type
      (->> (assoc params
                  (keyword (s/replace template #".html" "-selected")) "active"
                  :dev (env :dev)
                  :servlet-context
                  (if-let [context (:servlet-context request)]
                    ;; If we're not inside a serlvet environment (for
                    ;; example when using mock requests), then
                    ;; .getContextPath might not exist
                    (try (.getContextPath context)
                         (catch IllegalArgumentException _ context)))
                  ;; FIXME: the following line always issues db query even if not necessary
                  :latest-articles (:articles (article/latest-articles 0 10))
                  :tag-cloud-tags (map #(assoc % :size (long (+ 20 (* (:group %) 20))))
                             (tag/tag-cloud)))
        (parser/render-file (str template))
        response)
      "text/html; charset=utf-8")))

(defn render [template & [params]]
  (RenderableTemplate. template params))

