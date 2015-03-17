(ns jughead.multi-line-test
  (:require [clojure.test :refer :all]
            [jughead.core :refer :all])
  (:use midje.sweet))

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

       (fact "arrays within a multi-line value breaks up the value"
             (-> "key:value\ntext\n[array]\nmore text\n:end" parse :key)
             => "value")

       (fact "objects within a multi-line value breaks up the value"
             (-> "key:value\ntext\n{scope}\nmore text\n:end" parse :key)
             => "value")

       (fact "Stray :end does not affect result"
             (-> "key:value\ntext\n{scope}\nmore text\n:end" parse)
             => {:scope {}, :key "value"})

       (fact "bullets within a multi-line value do not break up the value"
             (-> "key:value\ntext\n* value\nmore text\n:end" parse :key)
             => "value\ntext\n* value\nmore text")

       (fact "skips within a multi-line value do not break up the value"
             (-> "key:value\ntext\n:skip\n:endskip\nmore text\n:end" parse :key)
             => "value\ntext\nmore text")

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
