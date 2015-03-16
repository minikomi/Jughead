(ns jughead.comment-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

(facts "comments"
       (fact "ignore comments inside of [single brackets]"
             (-> "key:value [inline comments] value" parse :key)
             => "value  value")

       (fact "supports multiple inline comments on a single line"
             (-> "key:value [inline comments] value [inline comments] value" parse :key)
             => "value  value  value")

       (fact "supports adjacent comments"
             (-> "key:value [inline comments] [inline comments] value"parse :key)
             => "value   value")

       (fact "supports no-space adjacent comments"
             (-> "key:value [inline comments][inline comments] value" parse :key)
             => "value  value")

       (fact "supports comments at beginning of string"
             (-> "key:[inline comments] value" parse :key)
             => "value")

       (fact "supports comments at end of string"
             (-> "key:value [inline comments]" parse :key)
             => "value")

       (fact "whitespace before a comment that appears at end of line is ignored"
             (-> "key:value [inline comments] value [inline comments]" parse :key)
             => "value  value")

       (fact "unmatched single brackets are preserved"
             (-> "key:value ][ value" parse :key)
             => "value ][ value")

       (fact "inline comments are supported on the first of multi-line values"
             (-> "key:value [inline comments] on\nmultiline\n:end" parse :key)
             => "value  on\nmultiline")

       (fact "inline comments are supported on subsequent lines of multi-line values"
             (-> "key:value\nmultiline [inline comments]\n:end" parse :key)
             => "value\nmultiline")

       (fact "whitespace around comments is preserved, except at the beinning and end of a value"
             (-> "key: [] value [] \n multiline [] \n:end" parse :key)
             => "value  \n multiline")

       (fact "inline comments cannot span multiple lines"
             (-> "key:value [inline\ncomments] value\n:end" parse :key)
             => "value [inline\ncomments] value")

       (fact "inline comments cannot span multiple lines"
             (-> "key:value \n[inline\ncomments] value\n:end" parse :key)
             => "value \n[inline\ncomments] value")

       (fact "text inside [[double brackets]] is included as [single brackets]"
             (-> "key:value [[brackets]] value" parse :key)
             => "value [brackets] value")

       (fact "unmatched double brackets are preserved"
             (-> "key:value ]][[ value" parse :key)
             => "value ]][[ value")

       (fact "comments work in simple arrays"
             (-> "[array]\n*Val[comment]ue" parse :array first)
             => "Value")

       (fact "double brackets work in simple arrays"
             (-> "[array]\n*Val[[real]]ue" parse :array first)
             => "Val[real]ue"))

