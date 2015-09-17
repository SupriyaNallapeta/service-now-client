(ns snow-client.utils
  [:require [clojure.pprint :refer [pprint]]])

  (defn incremented? [before after]
    "bool helper is it incremented by one?"
  (= 1 (- after before)))

(defn debug [val]
  (do
    (pprint val)
    val))

(defn now-timestamp []
  "get the current epoch integer timestamp"
  ;; http://stackoverflow.com/questions/17432032/how-do-i-get-a-unix-timestamp-in-clojure
  (quot (System/currentTimeMillis) 1000))
