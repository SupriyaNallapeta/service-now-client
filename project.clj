(defproject snow-client "0.1.3-SNAPSHOT"
  :description "API client for service now's api"
  :license "MIT"
  :url "http://github.com/shaiguitar/service-now-client"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [http-kit "2.1.18"]
                 [cheshire "5.5.0"]
                 [midje "1.7.0"]
                 [org.clojure/clojure "1.7.0"]]
  :plugins [[lein-midje "3.1.3"]]
  :source-paths ["dev", "src", "test"]
  :init-ns user)
