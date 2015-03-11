(ns jughead.core-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))


(facts "Parsing values"
       (fact "archiparsers key : value pairs"
             (archie-parser "key:value")
              => {:key "value"})

       (fact "Ignores spaces on either side of the key"
             (archie-parser "  key  :value") 
              => {:key "value"})

       (fact "Ignores tabs on eight side of the key"
             (archie-parser "\t\tkey\t\t:value")
              => {:key "value"})

       (fact "Ignores spaces on eight side of the value"
             (archie-parser "key:  value  ")
              => {:key "value"})

       (fact "Ignores tabs on either side of the value"
             (archie-parser "key:\t\tvalue\t\t")
              => {:key "value"})

       (fact "Duplicate keys are assigned to the last given value"
             (archie-parser "key:value\nkey:newvalue")
              => {:key "newvalue"})

       (fact "Allows non-letter characters at the start of values"
             (archie-parser "key::value")
              => {:key ":value"})

       (fact "Keys are case sensitive"
             (keys (archie-parser "key:value\nKey:Value"))
              => (just [:key :Key] :in-any-order true))


       (fact "Non-keys don't affect parsing"
             (archie-parser "other stuff\nkey:value\nother stuff")
              => {:key "value"}))

(facts "Valid keys"

       (fact "letters, numbers, dashes and underscores are valid key components"
             (archie-parser "a-_1:value")
             => {:a-_1 "value"})


       (fact "spaces are not allowed in keys"
             (-> "k ey:value" archie-parser keys count)
             => 0)

       (fact "symbols are not allowed in keys"
            (-> "k&ey:value" archie-parser keys count)
            => 0)

       (fact "keys can be nested using dot-notation"
             (-> "scope.key:value" archie-parser)
             => {:scope {:key "value"}})

       (fact "earlier keys within scopes aren't deleted when using dot-notation"
             (-> "scope.key:value\nscope.otherkey:value"
                 archie-parser
                 :scope
                 :key)
             => "value")

       (fact "the value of key that used to be a string object should be replaced with an object if necessary" 
             (-> "scope.level:value\nscope.level.level:value"
                 archie-parser
                 :scope
                 :level
                 :level)
             => "value")

       (fact "the value of key that used to be a parent object should be replaced with a string if necessary" 
         (-> "scope.level.level:value\nscope.level:value"
             archie-parser
             :scope
             :level)
         => "value")

       )
