(ns jughead.core-test
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


(facts "skip"

       (fact "ignores spaces on either side of :skip"
             (-> "  :skip  \nkey:value\n:endskip" parse keys)
             => empty?)

       (fact "ignores tabs on either side of :skip"

             (-> "\t\t:skip\t\t\nkey:value\n:endskip" parse keys)
             => empty?)

       (fact "ignores spaces on either side of :endskip"
             (-> ":skip\nkey:value\n  :endskip  " parse keys)
             => empty?)

       (fact "ignores tabs on either side of :endskip"
             (-> ":skip\nkey:value\n\t\t:endskip\t\t" parse keys)
             => empty?)

       (fact "starts parsing again after :endskip"
             (-> ":skip\n:endskip\nkey:value" parse :key)
             => "value")

       (fact ":skip and :endskip are case insensitive"
             (-> ":sKiP\nkey:value\n:eNdSkIp" parse keys)
             => empty?)

       (fact "parse :skip as a special command even if more is appended to word"
             (-> ":skipthis\nkey:value\n:endskip" parse keys)
             => empty?)

       (fact "ignores all content on line after :skip + space"
             (-> ":skip this text  \nkey:value\n:endskip" parse keys)
             => empty?)

       (fact "ignores all content on line after :skip + tab"
             (-> ":skip\tthis text\t\t\nkey:value\n:endskip" parse keys)
             => empty?)


       (fact "parse :endskip as a special command even if more is appended to word" 
             (-> ":skip\n:endskiptheabove\nkey:value" parse :key)
             => "value")

       (fact "ignores all content on line after :endskip + space"
             (-> ":skip\n:endskip the above\nkey:value" parse :key)
             => "value")

       (fact "ignores all content on line after :endskip + tab"
             (-> ":skip\n:endskip\tthe above\nkey:value" parse :key)
             => "value")

       (fact "does not parse :end as an :endskip"
             (-> ":skip\n:end\tthe above\nkey:value" parse keys)
             => empty?)

       (fact "ignores keys within a skip block"
             (-> "key1:value1\n:skip\nother:value\n\n:endskip\n\nkey2:value2" 
                 parse
                 keys) 
             => (just [:key1 :key2] :in-any-order true)))


(facts "ignore"

       (fact "text before ':ignore' should be included"
             (-> "key:value\n:ignore" parse :key)
             => "value")

       (fact "text after ':ignore' should be ignored"
             (-> ":ignore\nkey:value" parse keys)
             => empty?)

       (fact "':ignore' is case insensitive"
             (-> ":iGnOrE\nkey:value" parse keys)
             => empty?)

       (fact "ignores spaces on either side of :ignore"
             (-> "  :ignore  \nkey:value" parse keys)
             => empty?)

       (fact "ignores tabs on either side of :ignore"
             (-> "\t\t:ignore\t\t\nkey:value" parse keys)
             => empty?)

       (fact "parses :ignore as a special command even if more is appended to word"
             (-> ":ignorethis\nkey:value" parse keys)
             => empty?)

       (fact "ignores all content on line after :ignore + space"
             (-> ":ignore the below\nkey:value" parse keys)
             => empty?)

       (fact "ignores all content on line after :ignore + tab"
             (-> ":ignore\tthe below\nkey:value" parse keys)
             => empty?))




