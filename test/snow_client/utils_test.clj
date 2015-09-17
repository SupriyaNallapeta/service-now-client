(ns snow-client.utils-test
  [:use midje.sweet]
  [:require
   [snow-client.utils :as u]
   [cheshire.core :as json]])

(fact "time-now returns a unix epoch int"
      (number? (u/now-timestamp)) => true )

(fact "incremented? checks to see if two numbers are incremented"
      (u/incremented? 290 291) => true)
