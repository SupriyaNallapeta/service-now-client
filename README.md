## SNOW-CLIENT (clojure lib)

Client for the service now API 

[![Clojars Project](http://clojars.org/snow-client/latest-version.svg)](http://clojars.org/snow-client)

# Local usage

You'll need to setup basic auth keys. See `resources/basic-auth.json.example` - move to `basic-auth.json` modify accordingly, you'll probably want to put
that in your resources folder of your clojure project, after adding the require in project.clj etc.

# Run tests

`lein midje :filter -slow` 

This won't work unless you modify the basic auth and domain parameters.

# Example

```clojure

;; set up some variables you can use across service now table definitions
(def basic-auth (j/parse-string (slurp "resources/basic-auth.json"))) ; ["un", "pass"]
(def domain "https://your-sandbox.service-now.com/")
(def staging-domain "https://your-sandbox-staging.service-now.com/")

;; set up api access for a given table (called "resouce").
(deftable resource { :base-url staging-domain :basic-auth [un, pass] :snow-table "u_resouce.do"})
```

The `deftable`s above will automagically get you these functions you can use on those tables:

```
    (resource-entry 666)                                  => {record1}
    (resource-entries [:or [:name foobar] [:field foo]])  => [{record1} {record2}]
    (resource-create {:field1 val1 :field2 val2})         => [{newrecord}]
    (resource-update {:sys_id 666 :field1 val1})          => [{updatedrecord}]
    (resource-delete 666)                                 => []"
```

So, to illustrate, assume you had a servicenow account, with an `events` table that has two columns: `severity` and `name`. Interacting with the API would look like this:

```clojure

;; setup the api client:

(deftable event
  { :base-url "http://your-name.service-now.com/"
    :basic-auth ["foo", "foopass"] :snow-table "u_events.do"})

;; That's it!
;; Now you have magical functions you can now use.

;; List events in the table:
(event-entries []) ;; you can also give it a query here, like (event-entries [:name "foo"])

;; create a new event:
(event-create {:name "something is bad" :severity "warn" }) ;; returns the created event

;; list that event (looks on name)
(event-entries [:name "something is bad"])

;; or get the specific event
(event-entry "1234") ;; the sys_id of the created event

;; update that event
(event-update {:sys_id "1234" :severity "critical"}) ;; needs the sys_id of the created event

;; delete the event
(event-delete "1234")

 ;; won't be here now, cause it's deleted
(event-entry "1234")

```

# How it works

There's a clojure macro expansion, which essentially by using (eg `deftable "foo"`) will create "foo-entries", "foo-entry", "foo-delete", "foo-update" etc, so this can be expanded to any kind of table name. yay metaprogramming.

# Service Now API Docs

- Docs/API

This is mostly based on this api:

[docs](http://wiki.servicenow.com/index.php?title=Legacy:JSON_Web_Service#gsc.tab=0)

There are other apis available though too:

[docs table api](http: //wiki.servicenow.com/index.php?title=Table_API#POST_.2Fapi.2Fnow.2Fv1.2Ftable.2F.28tableName.29&gsc.tab=0)
[docs rest api](http://wiki.servicenow.com/index.php?title=REST_API#Security&gsc.tab=0)

# Next todo

- Add in bulk operations (deleteMultiple insertMultiple)
- make query parser more flexible
