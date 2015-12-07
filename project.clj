(defproject clojournal "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [lib-noir "0.9.5"]
                 [ring-server "0.3.1"]
                 [selmer "0.7.7"]
                 [com.taoensso/timbre "3.3.1"]
                 [com.taoensso/tower "3.0.2"]
                 [markdown-clj "0.9.58"
                  :exclusions [com.keminglabs/cljx]]
                 [environ "1.0.0"]
                 [im.chit/cronj "1.4.3"]
                 [noir-exception "0.2.3"]
                 [prone "0.8.0"]
                 [com.novemberain/monger "3.0.1"]
                 [lib-noir "0.9.5"]
                 [crypto-password "0.1.3"]
                 [clj-time "0.9.0"]
                 [clj-rss "0.1.9"]]
  :uberjar-name "clojournal.jar"
  :repl-options {:init-ns clojournal.repl}
  :jvm-opts ["-server"]
  :plugins [[lein-ring "0.9.0"]
            [lein-environ "1.0.0"]
            [lein-ancient "0.5.5"]]
  :ring {:handler clojournal.handler/app
         :init    clojournal.handler/init
         :destroy clojournal.handler/destroy
         :uberwar-name "clojournal.war"}
  :profiles
  {:uberjar {:omit-source true
             :env {:production true}
             :aot :all}
   :production {:ring {:open-browser? false
                       :stacktraces?  false
                       :auto-reload?  false}}
   :dev {:dependencies [[ring-mock "0.1.5"]
                        [ring/ring-devel "1.3.2"]
                        [pjstadig/humane-test-output "0.6.0"]]
         :injections [(require 'pjstadig.humane-test-output)
                      (pjstadig.humane-test-output/activate!)]
         :env {:dev true
               :mongo-url "mongodb://localhost/clojournal"}}}
  :main clojournal.core
  :eastwood  {:exclude-linters  [:constant-test]}
  :min-lein-version "2.0.0")
