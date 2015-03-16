(ns jughead.skip-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

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

