(ns smee.theta-test
  (:require [smee.theta :as smee]
            [smee.router :as router]
            [smee.middleware :as middleware]
            [clojure.test :refer [deftest testing is]]))


(deftest url-for-test
  (let [routes (router/routes
                 (smee.middleware/site-routes
                   [:get "/" ::home]
                   [:post "/" ::home-action]
                   [:get "/hello" ::hello]
                   [:get "/hello/:id" ::hello-id]))
        _ (smee/app {:routes routes})]
    (testing "url-for without a map"
      (is (= "/" (smee/url-for ::home))))

    (testing "url-for with a map with no url params"
      (is (= "/hello?key=value" (smee/url-for ::hello {:key "value"}))))

    (testing "url-for with a map with url params"
      (is (= "/hello/1?key=value" (smee/url-for ::hello-id {:id 1 :key "value"}))))

    (testing "url-for with a map, a url param and a #"
      (is (= "/hello/2?key=value#anchor" (smee/url-for ::hello-id {:id 2 :key "value" :# "anchor"}))))))
