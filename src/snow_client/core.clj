(ns snow-client.core
  [:require
   [clojure.string :as s]
   [org.httpkit.client :as http]
   [cheshire.core :as j]
   [clojure.walk :as walk]
   [snow-client.utils :as u]
   [snow-client.query :as q]])

;; service now always puts a "results" key in front of everything.
;; (defn parse-records [res] (:results (j/parse-string (:body (u/debug res)) keyword))) with debug
(defn parse-records [res] (:result (j/parse-string (:body res) keyword)))

(defmulti request :method)
(defmethod request :get [{:keys [auth url]}]
  (parse-records @(http/get url {:basic-auth auth :headers {"Accept" "application/json"}})))

(defmethod request :post [{:keys [auth url data]}]
  (parse-records @(http/post url (merge {:basic-auth auth} {:body (j/encode data)} {:headers {"Content-Type" "application/json"} }))))

(defmethod request :put [{:keys [auth url data]}]
  (parse-records @(http/put url (merge {:basic-auth auth} {:body (j/encode data)} {:headers {"Content-Type" "application/json"} }))))

;; clojure is crazy awesome or just crazy?

(defprotocol Tabel
  (entries
   [this]
   [this query]
   [this query limit])
  (entry  [this id])
  (create-entity [this data])
  (update-entity [this data])
  (delete [this id]))

(defrecord SnowTable
    [base-url snow-table basic-auth default-limit]
  Tabel
  (entries [this]
    (entries this []))
  (entries [this query]
    (entries this query {:limit  (or default-limit 10)}))
  (entries [this query {:keys [limit]}]
    (let [params (q/parse-query query)
          url (str base-url snow-table "?sysparm_query=" params "&sysparm_limit=" limit)]
      (request {:method :get :url url :auth basic-auth})))
  (entry [this id]
    (entries this [:sys_id id] 1))
  (create-entity [this data]
    (let [json (j/encode data)
          url (str base-url snow-table "?sysparm_action=insert")]
      (request {:method :post :url url :auth basic-auth :data data})))
  (update-entity [this data]
    (let [json (j/encode data)
          sys_id (:sys_id data)
          url (str base-url snow-table "?sysparm_action=update&sysparm_query=sys_id=" sys_id)]
      (request {:method :post :url url :auth basic-auth :data data})))
  (delete [this id]
    (let [url (str base-url snow-table "?sysparm_action=deleteRecord&sysparm_sys_id=" id)]
      (request {:method :post :url url :auth basic-auth}))))

(defn link? [m]
  (not (nil? (:link m))))

(defn get-links [{:keys [link] :as b} basic-auth]
  (request {:method :get :url link :auth basic-auth}))

(declare walk-links)

(defn maybe-update [basic-auth [k v]]
  (if (link? v)
    [k (get-links v basic-auth)]
    (if (map? v)
      [k (walk-links v basic-auth)]
      [k v])))

(defn walk-links [tree basic-auth]
  (walk/walk (partial maybe-update basic-auth) identity tree))
