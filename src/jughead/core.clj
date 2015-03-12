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

(defn generate-keygroup-and-value [initial-line-data remain]
  (let [[kg initial-value] (transform initial-line-data)]
    (loop [buffer [initial-value] new-remain remain]
      (if (empty? new-remain) [kg initial-value (rest remain)]
        (let [[line-type line-data] (first new-remain)]
          (if (= :Special line-type)
            (if (= :End (-> line-data first))
              [kg (clojure.string/join "\n" buffer) (rest new-remain)]
              [kg initial-value (rest remain)])
            (recur (conj buffer (second line-data))
                   (rest new-remain))))))))

(defn special-tag-isnt [tag]
  #(not (= tag (-> % second first))))

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
                     (let [[kg v new-remain] (generate-keygroup-and-value line-data (rest remain))]
                       (recur new-remain
                              (archie-assoc-in result kg v)))
                     :Skip 
                     (recur (rest (drop-while (special-tag-isnt :EndSkip) remain))
                            result)
                     :Ignore
                     result
                     (throw (Exception. "Unexpected :Special case"))
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
