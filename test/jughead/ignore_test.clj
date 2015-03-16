(ns jughead.ignore-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

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
