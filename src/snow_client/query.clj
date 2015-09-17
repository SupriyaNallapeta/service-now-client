(ns snow-client.query [:require
                       [clojure.string :as s]])

;; SNOW QUERY/SQL PARSING API ACCESS AS PER
;; http://wiki.servicenow.com/index.php?title=Encoded_Query_Strings#gsc.tab=0JSONv2

(defmulti parse-query first)
(defmethod parse-query :default [[k v]]
  (str (name k) "=" v))

(defmethod parse-query nil [k] "")

;; (parse-query [:and [:created_at now] [:foo bar])
(defmethod parse-query :and [[_ & query]]
  ;; conditions is the parsed string (k=v)
  (let [conditions (map parse-query query)]
    (s/join "^" conditions)))

;; (parse-query [:or [:created_at now] [:foo bar])
(defmethod parse-query :or [[_ & query]]
  (let [conditions (map parse-query query)]
    (s/join "^OR" conditions)))

;; (parse-query [:orderby :created_at])
(defmethod parse-query :orderby [[_ k query]]
  (let [conditions (parse-query query)]
    (str conditions "^ORDERBY" (name k))))
