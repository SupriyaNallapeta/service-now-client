(ns user
  (:require [snow-client.core :refer [map->SnowTable request] :as c]
            [cheshire.core :as j]
            [clojure.test :as t]))

(def domain "https://foocloudops.service-now.com/")
(def staging-domain "https://foocloudopsstg.service-now.com/")
(def basic-auth ["u" "p"])

(def service (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth :snow-table "u_service.do"}))
(def event (map->SnowTable   { :base-url staging-domain :basic-auth basic-auth :snow-table "u_events.do"}))
(def router (map->SnowTable  { :base-url staging-domain :basic-auth basic-auth
                              :snow-table "u_router.do"}))
