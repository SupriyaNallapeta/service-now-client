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

;; setup the api client:
;; That's it!
;; Now you have magical functions you can now use.

;; List events in the table:
(entires event []) ;; you can also give it a query here, like (event-entries [:name "foo"])

;; create a new event:
(create-entity event- {:name "something is bad" :severity "warn" }) ;; returns the created event

;; list that event (looks on name)
(entires event [:name "something is bad"])

;; or get the specific event
(entry event "1234") ;; the sys_id of the created event

;; update that event
(update-entity event {:sys_id "1234" :severity "critical"}) ;; needs the sys_id of the created event

;; delete the event
(delete event"1234")

 ;; won't be here now, cause it's deleted
(entry event "1234")

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
