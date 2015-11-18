## SNOW-CLIENT (clojure lib)

Client for the service now API 

[![Clojars Project](http://clojars.org/snow-client/latest-version.svg)](http://clojars.org/snow-client)
[![Circle CI](https://circleci.com/gh/shaiguitar/service-now-client/tree/master.svg?style=svg)](https://circleci.com/gh/shaiguitar/service-now-client/tree/master)
# Local usage

You'll need to setup basic auth keys. See `resources/basic-auth.json.example` - move to `basic-auth.json` modify accordingly, you'll probably want to put
that in your resources folder of your clojure project, after adding the require in project.clj etc.

# Run tests

`lein midje` 

# Example

```clojure

;; set up some variables you can use across service now table definitions
(use 'snow-client.core)
(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]

(def domain "https://your-sandbox.service-now.com/api/now/v1/table")

;; set up api access for a given table (called "resouce").
(def resource (map->SnowTable  { :base-url staging-domain :basic-auth [un, pass] :snow-table "u_resouce.do" :default-limit 10}))

```

The `map->SnowTable` will create record that can be used to interact with the service now api

```
    (entry resource 666)                                  => {record1}
    (entries resource [:or [:name foobar] [:field foo]])  => [{record1} {record2}]
    (entries resource )  => [{record1} {record2}]
    (entries resource [] {:limit 100})  => [{record1} {record2} ... 98 more]
    (create resource {:field1 val1 :field2 val2})         => [{newrecord}]
    (update-entity resource {:sys_id 666 :field1 val1})          => [{updatedrecord}]
    (delete  resource 666)                                 => []"
```

You'll find that some of the data you get are just links to more data, if you would like to traverse those use `walk-links`

```

(walk-links  (entry resource 666) creds)                                  => {record1}
```

So, to illustrate, assume you had a servicenow account, with an `events` table that has two columns: `severity` and `name`. Interacting with the API would look like this:

```clojure

(ns prd
  (:require [snow-client.core :refer :all]
            [cheshire.core :as j]
            [clojure.test :as t]))

(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def domain "https://foo.service-now.com/api/now/v1/table/")

(def service (map->SnowTable  { :base-url domain :basic-auth basic-auth
                               :default-limit 10
                               :snow-table "u_service"}))

(def router  (map->SnowTable  { :base-url domain :basic-auth basic-auth
                               :default-limit 10
                               :snow-table "u_router"}))

(def events  (map->SnowTable  { :base-url domain
                              :default-limit 10
                              :basic-auth basic-auth
                              :snow-table "u_events"}))

(defn events-10 [] ;; default-limit
  (entries events))

```

# Service Now API Docs

- Docs/API

This is mostly based on this api:

[docs](http://wiki.servicenow.com/index.php?title=Legacy:JSON_Web_Service#gsc.tab=0)

There are other apis available though too:

[docs table api](http://wiki.servicenow.com/index.php?title=Table_API#POST_.2Fapi.2Fnow.2Fv1.2Ftable.2F.28tableName.29&gsc.tab=0)
[docs rest api](http://wiki.servicenow.com/index.php?title=REST_API#Security&gsc.tab=0)

# Next todo

- Add in bulk operations (deleteMultiple insertMultiple)
- make query parser more flexible

# Releasing

`lein deploy clojars`
