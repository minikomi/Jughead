(ns jughead.array-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

(facts "arrays"
       (fact "[array] creates an empty array at "array""
             (-> "[array]" parse :array)
             => [])

       (fact "ignores spaces on either side of [array]"
             (-> "  [array]  " parse :array)
             => [])

       (fact "ignores tabs on either side of [array]"
             (-> "\t\t[array]\t\t" parse :array)
             => [])

       (fact "ignores spaces on either side of [array] variable name"
             (-> "[  array  ]" parse :array)
             => [])

       (fact "ignores tabs on either side of [array] variable name"
             (-> "[\t\tarray\t\t]" parse :array)
             => [])

       (fact "ignores text after [array]"
             (-> "[array]a" parse :array)
             => [])

       (fact "arrays can be nested using dot-notaion"
             (-> "[scope.array]" parse :scope :array)
             => [])

       (fact "array values can be nested using dot-notaion"
             (-> "[array]\nscope.key: value\nscope.key: value" parse :array)
             => [{:scope {:key "value"}} {:scope {:key "value"}}])

       (fact "[] resets to the global scope"
             (-> "[array]\n[]\nkey:value" parse :key)
             => "value")

       (fact "ignore spaces inside []"
             (-> "[array]\n[  ]\nkey:value" parse :key)
             => "value")

       (fact "ignore tabs inside []"
             (-> "[array]\n[\t\t]\nkey:value" parse :key)
             => "value")

       (fact "ignore spaces on either side of []"
             (-> "[array]\n  []  \nkey:value" parse :key)
             => "value")

       (fact "ignore tabs on either side of []"
             (-> "[array]\n\t\t[]\t\t\nkey:value" parse :key)
             => "value"))

(facts "simple arrays"
       (fact "creates a simple array when an \"*\" is encountered first"
             (-> "[array]\n*Value" parse :array)
             => ["Value"])

       (fact "ignores spaces on either side of \"*\""
             (-> "[array]\n  *  Value" parse :array)
             => ["Value"])

       (fact "ignores tabs on either side of \"*\""
             (-> "[array]\n\t\t*\t\tValue" parse :array)
             => ["Value"])

       (fact "adds multiple elements"
             (-> "[array]\n*Value1\n*Value2" parse :array count)
             => 2)

       (fact "ignores all other text between elements"
             (-> "[array]\n*Value1\nNon-element\n*Value2" parse :array)
             => ["Value1" "Value2"])

       (fact "ignores key:value pairs between elements"
             (-> "[array]\n*Value1\nkey:value\n*Value2" parse :array)
             => ["Value1" "Value2"])

       (fact "parses key:values normally after an end-array"
             (-> "[array]\n*Value1\n[]\nkey:value" parse :key)
             => "value")

       (fact "multi-line values are allowed"
             (-> "[array]\n*Value1\nextra\n:end" parse :array)
             => ["Value1\nextra"])

       (fact "allows escaping of '*' within multi-line values in simple arrays"
             (-> "[array]\n*Value1\n\\* extra\n:end" parse :array)
             => ["Value1\n* extra"])

       (fact "allows escaping of command keys within multi-line values"
             (-> "[array]\n*Value1\n\\:end\n:end" parse :array)
             => ["Value1\n:end"])

       (fact "does not allow escaping of keys within multi-line values"
             (-> "[array]\n*Value1\nkey\\:value\n:end" parse :array)
             => ["Value1\nkey\\:value"])

       (fact "allows escaping key lines with a leading backslash"
             (-> "[array]\n*Value1\n\\key:value\n:end" parse :array)
             => ["Value1\nkey:value"])

       (fact "does not allow escaping of colons not at the beginning of lines"
             (-> "[array]\n*Value1\nword key\\:value\n:end" parse :array)
             => ["Value1\nword key\\:value"])


       (fact "arrays that are reopened add to existing array"
             (-> "[array]\n*Value\n[]\n[array]\n*Value" parse :array count)
             => 2)

       (fact "simple arrays that are reopened remain simple"
             (-> "[array]\n*Value\n[]\n[array]\nkey:value" parse :array)
             => ["Value"]))

(facts "complex arrays"
       (fact "keys after an [array] are included as items in the array"
             (-> "[array]\nkey:value" parse :array first :key)
             => "value")

       (fact "array items can have multiple keys"
             (-> "[array]\nkey:value\nsecond:value2" parse :array first :second)
             => "value2")

       (fact "when a duplicate key is encountered, a new item in the array is started"
             (-> "[array]\nkey:value\nsecond:value\nkey:value" parse :array count)
             => 2)

       (fact "when a duplicate key is encountered, a new item in the array is started"
             (-> "[array]\nkey:first\nkey:second" parse :array second :key)
             => "second")

       (fact "when a duplicate key is encountered, a new item in the array is started"
             (-> "[array]\nscope.key:first\nscope.key:second" parse :array second :scope :key)
             => "second")

       (fact "duplicate keys must match on dot-notation scope"
             (-> "[array]\nkey:value\nscope.key:value" parse :array keys count)
             => 1)

       (fact "duplicate keys must match on dot-notation scope"
             (-> "[array]\nscope.key:value\nkey:value\notherscope.key:value" parse :array keys count)
             => 1)


       (fact "arrays that are reopened add to existing array"
             (-> "[array]\nkey:value\n[]\n[array]\nkey:value" parse :array count)
             => 2)

       (fact "complex arrays that are reopened remain complex"
             (-> "[array]\nkey:value\n[]\n[array]\n*Value" parse :array)
             => [{:key "value"}]))
