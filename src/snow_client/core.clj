(ns snow-client.core
  [:require
   [clojure.string :as s]
   [org.httpkit.client :as http]
   [cheshire.core :as j]
   [snow-client.query :as q]])

;; service now always puts a "records" key in front of everything.
(defn parse-records [res] (:records (j/parse-string (:body res) keyword)))

(defmulti request :method)
(defmethod request :get [{:keys [auth url]}]
  (parse-records @(http/get url {:basic-auth auth})))

(defmethod request :post [{:keys [auth url data]}]
  (parse-records @(http/post url (merge {:basic-auth auth} {:body (j/encode data)} {:headers {"Content-Type" "application/json"} }))))

(defmethod request :put [{:keys [auth url data]}]
  (parse-records @(http/put url (merge {:basic-auth auth} {:body (j/encode data)} {:headers {"Content-Type" "application/json"} }))))

;; clojure is crazy awesome or just crazy?

(defmacro deftable [name {:keys [base-url snow-table basic-auth]}]
  "(deftable resource {:base-url domain :basic-auth [un, pass] :snow-table u_resource.do}) generates the following functions:

    (resource-entry 666)                                  => {record1}
    (resource-entries [:or [:name foobar] [:field foo]])  => [{record1} {record2}]
    (resource-create {:field1 val1 :field2 val2})         => [{newrecord}]
    (resource-update {:sys_id 666 :field1 val1})          => [{updatedrecord}]
    (resource-delete 666)                                 => []"
  `(do

     (defn ~(symbol (str name "-entries")) [query#]
          (let [params# (q/parse-query query#)
                url# (str ~base-url ~snow-table "?JSONv2&sysparm_query=" params#)]
            (request {:method :get :url url# :auth ~basic-auth})))

     ;; helper method to not have the entry in an array.
     (defn ~(symbol (str name "-entry")) [id#]
       (first (~(symbol (str name "-entries")) [:sys_id id#])))

     ;; (resource-create {:your "field" :values "are" :sent "over"})
       (defn ~(symbol (str name "-create")) [data#]
         (let [json# (j/encode data#)
               url# (str ~base-url ~snow-table "?JSONv2&sysparm_action=insert")]
           (request {:method :post :url url# :auth ~basic-auth :data data#})))

       ;; (resource-update {:your "updated" :values "are" :sent "over"})
       (defn ~(symbol (str name "-update")) [data#]
         (let [json# (j/encode data#)
               sys_id# (:sys_id data#)
               url# (str ~base-url ~snow-table "?JSONv2&sysparm_action=update&sysparm_query=sys_id=" sys_id#)]
           (request {:method :post :url url# :auth ~basic-auth :data data#})))

       ;; (resource-delete sys_id)
       (defn ~(symbol (str name "-delete")) [id#]
         (let [url# (str ~base-url ~snow-table "?JSONv2&sysparm_action=deleteRecord&sysparm_sys_id=" id#)]
           (request {:method :post :url url# :auth ~basic-auth})))))
