(ns snow-client.core-test
  [:use midje.sweet org.httpkit.fake]
  (:require [snow-client.core :refer [deftable map->SnowTable entries update-entity entry create-entity delete]]
            [cheshire.core :as j]
            [snow-client.utils :as u]
            [clojure.test :as t]))

;; the client would setup:

;;(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def basic-auth ["un", "pass"]) ; ["un", "pass"]
(def staging-domain "https://fooopsstg.service-now.com/api/now/v1/table/")

(def creds-domain { :base-url staging-domain :basic-auth basic-auth :default-limit 1000})

(def incident-table (map->SnowTable (assoc creds-domain :snow-table "u_incidents")))
(def table-table (map->SnowTable (assoc creds-domain :snow-table "u_table")))
(def resource-table (map->SnowTable (assoc creds-domain :snow-table "u_resource")))


(defn gen-uniq
  ([prefix] (gen-uniq prefix 0))
  ([prefix add-this] (str prefix (+ add-this (u/now-timestamp)))))

(defn gen-fake [applicationname]
  {:u_email "true", :sys_created_by "foo-user", :application applicationname , :u_string_1 "FOO",
   :u_source "Snow Client #2", :u_auto_ignore "true", :sys_updated_by "foo-user", :u_auto_page "false", :u_auto_escalate "false", :business_service "foo bar"}
  )


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

(with-fake-http [{:url "https://fooopsstg.service-now.com/api/now/v1/table/u_incidents?sysparm_action=insert"
                  :method :post
                  :basic-auth basic-auth
                  :headers {"Content-Type" "application/json"}}
                 {:status 201
                  :body (j/encode  {:result {:sys_id 1234}})}]
  (fact "incident entries"
        (create-entity incident-table (gen-fake "test")) => {:sys_id 1234}))


(def mock-item (gen-fake "shaitest-1"))
(def changes {:u_email "false" :u_source "snow-client", :application "shaitest-2"})


;; INTEGRATION TESTS! (they should work, IF you modify the domains, have access, etc.)
(with-fake-http [{:url "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_action=insert"
                  :method :post
                  :basic-auth basic-auth
                  :headers {"Content-Type" "application/json"}}
                 {:status 201
                  :body (j/encode  {:result (assoc mock-item  :sys_id 1234)})}

                 {:url "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_action=update&sysparm_query=sys_id=1234"
                  :method :post
                  :basic-auth basic-auth
                  :headers {"Content-Type" "application/json"}}
                 {:status 200
                  :body (j/encode  {:result (merge (assoc mock-item  :sys_id 1234) changes)})}

                 {:url "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_query=sys_id%3D1234&sysparm_limit="
                  :method :get
                  :basic-auth basic-auth
                  :headers {"Accept" "application/json"}}
                 {:status 200
                  :body (j/encode  {:result (merge  (assoc mock-item  :sys_id 1234 ) changes)})}

                 {:url "https://fooopsstg.service-now.com/api/now/v1/table/u_resource?sysparm_action=deleteRecord&sysparm_sys_id=1234"
                  :method :post
                  :basic-auth basic-auth
                  :headers {"Content-Type" "application/json"}}
                 {:status 200
                  :body (j/encode  {:result :ok})}
                 ]

(fact "CREATING should return sys_id"
      (let [created-resource (create-entity resource-table mock-item)]
          (:sys_id created-resource) => 1234
          (fact "UPDATING"
                (let [modified-resource (merge created-resource changes)
                      update-resp (update-entity resource-table modified-resource)
                      updated-resource (entry resource-table (:sys_id created-resource))]
                  (:application  updated-resource) => "shaitest-2" 
                  (fact "DELETING"
                        (delete resource-table (:sys_id updated-resource)) => "ok"))))))





