(ns jughead.core
  (:require [instaparse.core :as insta]))


(defn value-group-to-string [value-group]
  (apply str (map second value-group)))

(defn transform [data]
  (insta/transform
    {:KeyValuePair hash-map
     :Key keyword
     :Value str}
    data))

(defn interpret [parsed]
  (loop [remain parsed
         result {}]
    (if (empty? remain) result
     (let [current-line (first remain)
           [line-type line-data] current-line]
      (case line-type
        :Normal (recur (rest remain) result)
        :Special (case (first line-data)
                   :KeyValuePair 
                   (recur (rest remain)
                          (into result (transform line-data)))
                   ))))))

(def line-parser (insta/parser (clojure.java.io/resource "archie.bnf")))

(defn archie-parser [input]
  (->> input
       (clojure.string/split-lines)
       (map line-parser)
       (map first)
       (interpret)))
