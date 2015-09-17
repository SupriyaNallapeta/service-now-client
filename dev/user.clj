(ns user
  (:require [snow-client.core :refer [deftable request]]
            [cheshire.core :as j]
            [clojure.test :as t]))

(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def domain "https://foocloudops.service-now.com/")
(def staging-domain "https://foocloudopsstg.service-now.com/")

(deftable service { :base-url staging-domain :basic-auth basic-auth :snow-table "u_service.do"})
(deftable event { :base-url staging-domain :basic-auth basic-auth :snow-table "u_events.do"})
(deftable router { :base-url staging-domain :basic-auth basic-auth
                  :snow-table "u_router.do"})
