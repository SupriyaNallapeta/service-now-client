(ns snow-client.query-test
  [:use midje.sweet]
  [:require
   [snow-client.query :as u]])

(fact "parsing query util helper dealybob simple" (u/raw-parse-query [:created_at "today"]) => "created_at=today")
(fact "parsing query util helper dealybob AND" (u/raw-parse-query [:and [:created_at "today"] [:sys_id 666]]) => "created_at=today^sys_id=666")
(fact "parsing query util helper dealybob OR" (u/raw-parse-query [:or [:created_at "today"] [:sys_id 666]]) => "created_at=today^ORsys_id=666")
(fact "parsing query util helper dealybob OR and AND"
      (u/raw-parse-query [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]) => "foo=bar^created_at=today^ORsys_id=666")

(fact "parsing query util helper dealybob nested OR and AND"
      (u/raw-parse-query [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]) => "foo=bar^created_at=today^ORsys_id=666")

(fact "parsing query util helper dealybob nested OR and AND"
      (u/raw-parse-query  [:orderby :created_at [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]]) => "foo=bar^created_at=today^ORsys_id=666^ORDERBYcreated_at")

(fact "parsing query util helper created by DESC"
(u/raw-parse-query  [:orderbydesc :created_at [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]]) => "foo=bar^created_at=today^ORsys_id=666^ORDERBYDESCcreated_at")


(fact "parsing query util helper created by DESC"
      (u/raw-parse-query  [:+  [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]] [:orderbydesc :created_at] ]) => "foo=bar^created_at=today^ORsys_id=666^ORDERBYDESCcreated_at")


(fact "parsing query util helper dealybob empty query"
      (u/raw-parse-query []) => "")
