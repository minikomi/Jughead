(ns jughead.keys-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

(facts "Valid keys"
       (fact "letters, numbers, dashes and underscores are valid key components"
             (-> "a-_1:value" parse :a-_1)
             => "value")

       (fact "spaces are not allowed in keys"
             (-> "k ey:value" parse keys)
             => empty?)

       (fact "symbols are not allowed in keys"
             (-> "k&ey:value" parse keys)
             => empty?)

       (fact "keys can be nested using dot-notation"
             (-> "scope.key:value" parse :scope :key)
             => "value")

       (fact "earlier keys within scopes aren't deleted when using dot-notation"
             (-> "scope.key:value\nscope.otherkey:value" parse :scope :key)
             => "value")

       (fact "the value of key that used to be a string object should be replaced with an object if necessary" 
             (-> "scope.level:value\nscope.level.level:value" parse
                 :scope
                 :level
                 :level)
             => "value")

       (fact "the value of key that used to be a parent object should be replaced with a string if necessary" 
             (-> "scope.level.level:value\nscope.level:value"
                 parse
                 :scope
                 :level)
             => "value"))
