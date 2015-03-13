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


(facts "multi line values"
       (fact "adds additional lines to value if followed by an \":end\""
             (-> "key:value\nextra\n:end" parse :key)
             => "value\nextra")

       (fact "value\nextra", "\":end\" is case insensitive"
             (-> "key:value\nextra\n:EnD" parse :key)
             => "value\nextra")

       (fact "preserves blank lines and whitespace lines in the middle of content"
             (-> "key:value\n\n\t \nextra\n:end" parse :key)
             => "value\n\n\t \nextra")

       (fact "doesn't preserve whitespace at the end of the last line"
             (-> "key:value\nextra\t \n:end" parse :key)
             => "value\nextra")

       (fact "preserves whitespace at the end of the other lines"
             (-> "key:value\t \nextra\n:end" parse :key)
             => "value\t \nextra")

       (fact "ignores whitespace and newlines before the \":end\""
             (-> "key:value\nextra\n \n\t\n:end" parse :key)
             => "value\nextra")

       (fact "ignores spaces on either side of :end"
             (->"key:value\nextra\n  :end  " parse :key)
             => "value\nextra")

       (fact "ignores tabs on either side of :end"
             (-> "key:value\nextra\n\t\t:end\t\t" parse :key)
             => "value\nextra")

       (fact "parses :end as a special command even if more is appended to word" 
             (-> "key:value\nextra\n:endthis" parse :key)
             => "value\nextra")

       (fact "does not parse :endskip as an :end"
             (-> "key:value\nextra\n:endskip" parse :key) 
             => "value")

       (fact "ordinary text that starts with a colon is included"
             (-> "key:value\n:notacommand\n:end" parse :key)
             => "value\n:notacommand")

       (fact "ignores all content on line after :end + space" 
             (-> "key:value\nextra\n:end this" parse :key)
             => "value\nextra")
       (fact "ignores all content on line after :end + tab"
             (-> "key:value\nextra\n:end\tthis" parse :key)
             => "value\nextra")

       (fact "doesn't escape colons on first line"
             (-> "key::value\n:end" parse :key)
             => ":value")

       (fact "doesn't escape colons on first line"
             (-> "key:\\:value\n:end" parse :key)
             => "\\:value")

       (fact "does not allow escaping keys"
             (-> "key:value\nkey2\\:value\n:end" parse :key)
             => "value\nkey2\\:value")

       (fact "allows escaping key lines with a leading backslash"
             (-> "key:value\n\\key2:value\n:end" parse :key)
             => "value\nkey2:value")

       (fact "allows escaping commands at the beginning of lines"
             (-> "key:value\n\\:end\n:end" parse :key)
             => "value\n:end")

       (fact "allows escaping commands with extra text at the beginning of lines"
             (-> "key:value\n\\:endthis\n:end" parse :key)
             => "value\n:endthis")

       (fact "allows escaping of non-commands at the beginning of lines" 
             (-> "key:value\n\\:notacommand\n:end" parse :key)
             => "value\n:notacommand")

       (fact "allows simple array style lines" 
             (-> "key:value\n* value\n:end" parse :key)
             => "value\n* value")

       (fact "escapes '*' within multi-line values when not in a simple array" 
             (-> "key:value\n\\* value\n:end" parse :key)
             => "value\n* value")

       (fact "allows escaping {scopes} at the beginning of lines"
             (-> "key:value\n\\{scope}\n:end" parse :key)
             => "value\n{scope}")

       (fact "allows escaping [comments] at the beginning of lines"
             (-> "key:value\n\\[comment]\n:end" parse :key)
             => "value")

       (fact "allows escaping [[arrays]] at the beginning of lines"
             (-> "key:value\n\\[[array]]\n:end" parse :key)
             => "value\n[array]")

       (fact "allows escaping initial backslash at the beginning of lines" 
             (-> "key:value\n\\\\:end\n:end" parse :key)
             => "value\n\\:end")

       (fact "escapes only one initial backslash"
             (-> "key:value\n\\\\\\:end\n:end" parse :key)
             => "value\n\\\\:end")

       (fact "allows escaping multiple lines in a value" 
             (-> "key:value\n\\:end\n\\:ignore\n\\:endskip\n\\:skip\n:end" parse :key)
             => "value\n:end\n:ignore\n:endskip\n:skip")

       (fact "doesn't escape colons after beginning of lines" 
            (-> "key:value\nLorem key2\\:value\n:end" parse :key)
            => "value\nLorem key2\\:value"))


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
