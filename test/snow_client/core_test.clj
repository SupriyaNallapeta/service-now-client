(ns snow-client.core-test
  [:use midje.sweet]
  (:require [snow-client.core :refer [deftable map->SnowTable entries entry create delete]]
            [cheshire.core :as j]
            [snow-client.utils :as u]
            [clojure.test :as t]))

;; the client would setup:

(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def staging-domain "https://fooopsstg.service-now.com/api/now/v1/table/")

(deftable event {:base-url staging-domain :basic-auth basic-auth :snow-table "u_events"})
(def creds-domain { :base-url staging-domain :basic-auth basic-auth :default-limit 1000})

(deftable incident { :base-url staging-domain :basic-auth basic-auth :snow-table "u_incidents"})
(deftable tablename { :base-url staging-domain :basic-auth basic-auth :snow-table "u_table"})
(deftable resource { :base-url staging-domain :basic-auth basic-auth :snow-table "u_resource"})

(def incident-table (map->SnowTable (assoc creds-domain :snow-table "u_incidents")))
(def table-table (map->SnowTable (assoc creds-domain :snow-table "u_table")))
(def resource-table (map->SnowTable (assoc creds-domain :snow-table "u_resource")))

;; poor man's mocking library (with-redefs request)
(with-redefs [snow-client.core/request (fn [m] (:url m))]
  (fact "resource entries"
        (resource-entries [:and [:created_at "now"] [:name "recordname"]])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_query=created_at%3Dnow%5Ename%3Drecordname&sysparm_limit=1000")
  (fact "resource entries LIMIT"
        (resource-entries [:and [:created_at "now"] [:name "recordname"]] {:limit 10})
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_query=created_at%3Dnow%5Ename%3Drecordname&sysparm_limit=10")
  (fact "incident entries"
        (incident-entries [:sys_id 666])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_incidents?sysparm_query=sys_id%3D666&sysparm_limit=1000")
  (fact "tablename entries"
        (tablename-entries [:or [:created_at "today"] [:name "get-me-any-day"]])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_table?sysparm_query=created_at%3Dtoday%5EORname%3Dget-me-any-day&sysparm_limit=1000")
  (fact "deftable is a macro that should not fail"
        (macroexpand-1 '(deftable resource {:base-url "http://ehel" :snow-table "route"}))
        => anything))


;; poor man's mocking library (with-redefs request)
(with-redefs [snow-client.core/request (fn [m] (:url m))]
  (fact "resource entries"
        (entries resource-table [:and [:created_at "now"] [:name "recordname"]])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_query=created_at%3Dnow%5Ename%3Drecordname&sysparm_limit=1000")
  (fact "resource entries LIMIT"
        (entries resource-table [:and [:created_at "now"] [:name "recordname"]] {:limit 10})
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_query=created_at%3Dnow%5Ename%3Drecordname&sysparm_limit=10")
  (fact "incident entries"
        (entries incident-table [:sys_id 666])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_incidents?sysparm_query=sys_id%3D666&sysparm_limit=1000")
  (fact "tablename entries"
        (entries table-table [:or [:created_at "today"] [:name "get-me-any-day"]])
        => "https://fooopsstg.service-now.com/api/now/v1/table/u_table?sysparm_query=created_at%3Dtoday%5EORname%3Dget-me-any-day&sysparm_limit=1000"))


(defn gen-uniq
  ([prefix] (gen-uniq prefix 0))
  ([prefix add-this] (str prefix (+ add-this (u/now-timestamp)))))

;; INTEGRATION TESTS! (they should work, IF you modify the domains, have access, etc.)
(fact "CREATING"
      (let [applicationname (gen-uniq "shaitest-")
            skeleton {:u_email "true", :sys_created_by "foo-user", :application applicationname , :u_string_1 "FOO",
                      :u_source "Snow Client #2", :u_auto_ignore "true", :sys_updated_by "foo-user", :u_auto_page "false", :u_auto_escalate "false", :business_service "foo bar"}]
        (let [created-resource (first (resource-create skeleton))]
          (:sys_id created-resource) => anything
          (fact "UPDATING"
                (let [newapplicationname (gen-uniq "shaitest-" 10)
                      modified-resource (merge created-resource {:u_email "false" :u_source "snow-client", :application newapplicationname})
                      update-resp (resource-update modified-resource)
                      updated-resource (resource-entry (:sys_id created-resource))]
                  (:application updated-resource) => newapplicationname
                  (fact "DELETING"
                        (resource-delete (:sys_id updated-resource))
                        (resource-entry (:sys_id updated-resource)) => nil))))))

