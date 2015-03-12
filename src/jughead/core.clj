(ns jughead.core
  (:require [instaparse.core :as insta]))


(defn value-group-to-string [value-group]
  (apply str (map second value-group)))

(defn transform [data]
  (insta/transform
    {:KeyValuePair vector
     :Key (fn [& keystrings] (mapv keyword keystrings))
     :Value str}
    data))

(defn archie-assoc [m k v]
  "Same as clojure assoc unless the map is non-associative - in which case
  a new map containing just key / value is returned."
  (if (associative? m)
    (assoc m k v)
    {k v}))

(defn archie-assoc-in
  "Similar to clojure's base assoc-in but replaces non-associatives with
  maps rather than erroring out."
  [m [k & ks] v]
  (if ks
    (archie-assoc m k (archie-assoc-in (get m k) ks v))
    (archie-assoc m k v)))

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
                            (let [[kg v] (transform line-data)]
                              (archie-assoc-in result kg v)))
                     :Skip 
                     (recur (rest (drop-while #(not (= :EndSkip (-> % second first))) remain))
                            result
                            )

                     (str "Unknown Special Case: " (first line-data))
                     ))))))



(def line-parser (insta/parser (clojure.java.io/resource "archie.bnf")))

(defn parse-all-lines [input]
  (->> input 
       clojure.string/split-lines
       (map line-parser)
       (map first)))

(defn parse [input]
  (->> input
       parse-all-lines
       interpret))
