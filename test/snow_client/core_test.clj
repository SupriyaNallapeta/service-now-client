(ns snow-client.core-test
  [:use midje.sweet]
  (:require [snow-client.core :refer [deftable]]
            [cheshire.core :as j]
            [snow-client.utils :as u]
            [clojure.test :as t]))

;; the client would setup:

(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def domain "https://fooops.service-now.com/")
(def staging-domain "https://fooopsstg.service-now.com/")

(deftable incident { :base-url staging-domain :basic-auth basic-auth :snow-table "u_incidents.do"})
(deftable tablename { :base-url staging-domain :basic-auth basic-auth :snow-table "u_table.do"})
(deftable resource { :base-url staging-domain :basic-auth basic-auth :snow-table "u_resource.do"})

;; poor man's mocking library (with-redefs request)
(with-redefs [snow-client.core/request (fn [m] (:url m))]
  (fact "resource entries"
        (resource-entries [:and [:created_at "now"] [:name "recordname"]])
        => "https://fooopsstg.service-now.com/u_resource.do?JSONv2&sysparm_query=created_at=now^name=recordname")
  (fact "incident entries"
        (incident-entries [:sys_id 666])
        => "https://fooopsstg.service-now.com/u_incidents.do?JSONv2&sysparm_query=sys_id=666")
  (fact "tablename entries"
        (tablename-entries [:or [:created_at "today"] [:name "get-me-any-day"]])
        => "https://fooopsstg.service-now.com/u_table.do?JSONv2&sysparm_query=created_at=today^ORname=get-me-any-day")
  (fact "deftable is a macro that should not fail"
        (macroexpand-1 '(deftable resource {:base-url "http://ehel" :snow-table "route.do"}))
        => anything))

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

