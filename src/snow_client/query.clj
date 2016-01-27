(ns snow-client.query [:require
                       [clojure.string :as s]])

;; SNOW QUERY/SQL PARSING API ACCESS AS PER
;; http://wiki.servicenow.com/index.php?title=Encoded_Query_Strings#gsc.tab=0JSONv2

(defmulti raw-parse-query first)
(defmethod raw-parse-query :default [[k v]]
  (str (name k) "=" v))

(defmethod raw-parse-query nil [k] "")


(defmethod raw-parse-query :+ [[_ qleft qright]]
  (let [leftq (raw-parse-query qleft)
        rightq (raw-parse-query qright)]
    (str leftq rightq)))


;; (raw-parse-query [:and [:created_at now] [:foo bar])
(defmethod raw-parse-query :and [[_ & query]]
  ;; conditions is the parsed string (k=v)
  (let [conditions (map raw-parse-query query)]
    (s/join "^" conditions)))

;; (raw-parse-query [:or [:created_at now] [:foo bar])
(defmethod raw-parse-query :or [[_ & query]]
  (let [conditions (map raw-parse-query query)]
    (s/join "^OR" conditions)))

;; (raw-parse-query [:orderby :created_at])
(defmethod raw-parse-query :orderby [[_ k query]]
  (let [conditions (raw-parse-query query)]
    (str conditions "^ORDERBY" (name k))))

;; (raw-parse-query [:orderby :created_at])
(defmethod raw-parse-query :orderbydesc [[_ k query]]
  (let [conditions (raw-parse-query query)]
    (str conditions "^ORDERBYDESC" (name k))))

;; <option value="STARTSWITH">starts with</option>
;; <option value="ENDSWITH">ends with</option>
;; <option value="LIKE">contains</option>
;; <option value="NOT LIKE">does not contain</option>
;; <option value="=">is</option>
;; <option value="!=">is not</option>
;; <option value="ISEMPTY">is empty</option>
;; <option value="ISNOTEMPTY">is not empty</option>
;; <option value="ANYTHING">is anything</option>
;; <option value="IN">is one of</option>
;; <option value="EMPTYSTRING">is empty string</option>
;; <option value="&lt;=">less than or is</option>
;; <option value="&gt;=">greater than or is</option>
;; <option value="BETWEEN">between</option>
;; <option value="SAMEAS">is same</option>
;; <option value="NSAMEAS">is different</option>

(defn parse-query [x] (org.httpkit.client/url-encode (raw-parse-query x)))
