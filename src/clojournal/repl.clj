(ns clojournal.repl
  (:use [clojournal.handler :refer [app init destroy]]
        [ring.server.standalone :refer [serve]]
        [ring.middleware
         [file :refer [wrap-file]]
         [content-type :refer [wrap-content-type]]
         [not-modified :refer [wrap-not-modified]]])
  (:import org.eclipse.jetty.server.Server))

(defonce server (atom nil))

(defn get-handler []
  ;; #'app expands to (var app) so that when we reload our code,
  ;; the server is forced to re-resolve the symbol in the var
  ;; rather than having its own copy. When the root binding
  ;; changes, the server picks it up without having to restart.
  (-> #'app
      ; Makes static assets in $PROJECT_DIR/resources/public/ available.
      (wrap-file "resources")
      ; Content-Type and Last Modified headers for files in body
      (wrap-content-type)
      (wrap-not-modified)))

(defn start-server
  "used for starting the server in development mode from REPL"
  [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (reset! server
            (serve (get-handler)
                   {:port port
                    :init init
                    :auto-reload? true
                    :destroy destroy
                    :join? false}))
    (println (str "You can view the site at http://localhost:" port))))

(defn stop-server []
  (.stop ^Server @server)
  (reset! server nil))
