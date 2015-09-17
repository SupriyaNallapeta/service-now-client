# snow-client

Client for the service now API

# Local usage

You'll need to setup basic auth keys. See `resources/basic-auth.json.example` - move to `basic-auth.json` modify accordingly.

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

# What works

There's a clojure macro expansion, which essentially by using (eg `deftable "foo"`) will create "foo-entries", so this can be expanded to any kind of resource. yay metaprogramming.

# Service Now API Docs

- Docs/API

(docs)[http: //wiki.servicenow.com/index.php?title=Table_API#POST_.2Fapi.2Fnow.2Fv1.2Ftable.2F.28tableName.29&gsc.tab=0]

(docs)[http://wiki.servicenow.com/index.php?title=REST_API#Security&gsc.tab=0]

This is mostly based on this api however:

(docs)[http://wiki.servicenow.com/index.php?title=Legacy:JSON_Web_Service#gsc.tab=0]

# Next todo

Add in bulk operations (deleteMultiple insertMultiple updateMultiple)
