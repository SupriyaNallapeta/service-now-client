(ns user
  (:require [snow-client.core :refer [map->SnowTable request] :as c]
            [cheshire.core :as j]
            [clojure.test :as t]))

(def domain "https://foocloudops.service-now.com/api/now/v1/table")
(def staging-domain "https://foocloudopsstg.service-now.com/api/now/v1/table")
(def basic-auth ["u" "p"])



(def creds (  (juxt :username :password) config))

(def service (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth :snow-table "u_service"}))
(def event   (map->SnowTable  { :base-url (:url config) :basic-auth ((juxt :username :password ) config) :snow-table "u_events" :default-limit 2}))
 
(def router  (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth
                              :snow-table "u_router"}))
