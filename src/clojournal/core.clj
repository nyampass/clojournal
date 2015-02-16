(ns clojournal.core
  (:require [clojournal.handler :as handler]
            [ring.adapter.jetty :refer [run-jetty]]))

(defn -main []
  (let [port (Long/parseLong (get (System/getenv) "PORT" "8080"))]
    (handler/init)
    (run-jetty handler/app {:port port :join? false})))
