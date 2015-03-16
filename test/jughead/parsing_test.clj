(ns jughead.parsing-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

(facts "Parsing values"
       (fact "archiparsers key : value pairs"
             (-> "key:value" parse :key) 
             => "value")

       (fact "Ignores spaces on either side of the key"
             (-> "  key  :value" parse :key) 
             => "value")

       (fact "Ignores tabs on eight side of the key"
             (-> "\t\tkey\t\t:value" parse :key)
             => "value")

       (fact "Ignores spaces on eight side of the value"
             (-> "key:  value  " parse :key)
             => "value")

       (fact "Ignores tabs on either side of the value"
             (-> "key:\t\tvalue\t\t" parse :key)
             => "value")

       (fact "Duplicate keys are assigned to the last given value"
             (-> "key:value\nkey:newvalue" parse :key)
             => "newvalue")

       (fact "Allows non-letter characters at the start of values"
             (parse "key::value")
             => {:key ":value"})

       (fact "Keys are case sensitive"
             (-> "key:value\nKey:Value" parse keys)
             => (just [:key :Key] :in-any-order true))


       (fact "Non-keys don't affect parsing"
             (-> "other stuff\nkey:value\nother stuff" parse :key)
             => "value"))
