(ns snow-client.query-test
  [:use midje.sweet]
  [:require
   [snow-client.query :as u]])

(fact "parsing query util helper dealybob simple" (u/parse-query [:created_at "today"]) => "created_at=today")
(fact "parsing query util helper dealybob AND" (u/parse-query [:and [:created_at "today"] [:sys_id 666]]) => "created_at=today^sys_id=666")
(fact "parsing query util helper dealybob OR" (u/parse-query [:or [:created_at "today"] [:sys_id 666]]) => "created_at=today^ORsys_id=666")
(fact "parsing query util helper dealybob OR and AND"
      (u/parse-query [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]) => "foo=bar^created_at=today^ORsys_id=666")

(fact "parsing query util helper dealybob nested OR and AND"
      (u/parse-query [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]) => "foo=bar^created_at=today^ORsys_id=666")

(fact "parsing query util helper dealybob nested OR and AND"
      (u/parse-query  [:orderby :created_at [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]]) => "foo=bar^created_at=today^ORsys_id=666^ORDERBYcreated_at")

(fact "parsing query util helper created by DESC"
(u/parse-query  [:orderbydesc :created_at [:and [:foo "bar"] [:or [:created_at "today"] [:sys_id 666]]]]) => "foo=bar^created_at=today^ORsys_id=666^ORDERBYDESCcreated_at")

(fact "parsing query util helper dealybob empty query"
      (u/parse-query []) => "")
