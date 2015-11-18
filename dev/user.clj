(ns user
  (:require [snow-client.core :refer [map->SnowTable request] :as c]
            [cheshire.core :as j]
            [clojure.test :as t]))

(def domain "https://foocloudops.service-now.com/api/now/v1/table")
(def staging-domain "https://foocloudopsstg.service-now.com/api/now/v1/table")
(def basic-auth ["u" "p"])

(def service (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth :snow-table "u_service"}))
(def router  (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth :snow-table "u_router"}))
