(ns jughead.scope-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

(facts "scopes"
       (fact "{scope} creates an empty object at 'scope'"
             (-> "{scope}" parse :scope)
             => map?)

       (fact "ignores spaces on either side of {scope}"
             (-> "  {scope}  " parse :scope)
             => map?)

       (fact "ignores tabs on either side of {scope}"
             (-> "\t\t{scope}\t\t" parse :scope)
             => map?)

       (fact "ignores spaces on either side of {scope} variable name"
             (-> "{  scope  }" parse :scope)
             => map?)

       (fact "ignores tabs on either side of {scope} variable name"
             (-> "{\t\tscope\t\t}" parse :scope)
             => map?)

       (fact "ignores text after {scope}"
             (-> "{scope}a" parse :scope)
             => {})

       (fact "items before a {scope} are not namespaced"
             (-> "key:value\n{scope}" parse :key)
             => "value")

       (fact "items after a {scope} are namespaced"
             (-> "{scope}\nkey:value" parse :scope :key)
             => "value")

       (fact "scopes can be nested using dot-notaion"
             (-> "{scope.scope}\nkey:value" parse :scope :scope :key)
             => "value")

       (fact "scopes can contain multiple keys"
             (-> "{scope}\nkey:value\nother:value" parse :scope keys)
             => (just [:key :other] :in-any-order true))

       (fact "scopes can be reopened"
             (-> "{scope}\nkey:value\n{}\n{scope}\nother:value" parse :scope keys)
             => (just [:key :other] :in-any-order true))

       (fact "scopes do not overwrite existing values"
             (-> "{scope.scope}\nkey:value\n{scope.otherscope}key:value" 
                 parse :scope :scope :key)
             => "value")

       (fact "{} resets to the global scope"
             (-> "{scope}\n{}\nkey:value" parse :key)
             => "value")

       (fact "ignore spaces inside {}"
             (-> "{scope}\n{  }\nkey:value" parse :key)
             => "value")

       (fact "ignore tabs inside {}"
             (-> "{scope}\n{\t\t}\nkey:value" parse :key)
             => "value")

       (fact "ignore spaces on either side of {}"
             (-> "{scope}\n  {}  \nkey:value" parse :key)
             => "value")

       (fact "ignore tabs on either side of {}"
             (-> "{scope}\n\t\t{}\t\t\nkey:value" parse :key)
             => "value"))
