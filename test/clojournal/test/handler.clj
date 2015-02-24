(ns clojournal.test.handler
  (:use clojure.test
        [ring.mock.request :refer [request]]
        [clojournal.handler :refer [app init]]))

(deftest test-app
  (init)
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response))))))
