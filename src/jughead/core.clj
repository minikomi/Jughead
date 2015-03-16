(ns jughead.core
  (:require [instaparse.core :as insta]
            [clojure.string :as s]
            ))


(defn value-group-to-string [value-group]
  (apply str (map second value-group)))

(defn transform [data]
  (insta/transform
    {:KeyValuePair vector
     :Key (fn [& keystrings] (mapv keyword keystrings))
     :Value str
     :OpenScope identity
     :OpenArray identity
     }
    data))

(defn archie-assoc [m k v]
  "Same as clojure assoc unless the map is non-associative - in which case
  a new map containing just key / value is returned."
  (if (associative? m)
    (assoc m k v)
    {k v}))

(defn archie-assoc-in
  "Similar to clojure's base assoc-in but replaces non-associatives with
  maps rather than erroring out. Also allows nesting in a vector by 
  looking ahead for '0' keys.
  "
  [m [k & ks] v]
  (if ks
    (archie-assoc m k (archie-assoc-in (get m k) ks v))
    (archie-assoc m k v)))

(defn remove-until-endskip [remain]
  (->> remain 
       (drop-while #(not= :endskip (first %)) remain)
       rest))

(defn remove-comments [value]
  (-> value
      (s/replace #"(?:^\\)?\[[^\[\]\n\r]*\](?!\])" "")
      (s/replace #"\[\[([^\[\]\n\r]*)\]\]" "[$1]")))

(defn format-normal [value]
  (-> value
      (s/replace #"^\\" "")
      remove-comments))

(defn format-value [value]
  (-> value
      remove-comments
      (s/trim)))

(def line-parser (insta/parser (clojure.java.io/resource "archie.bnf")))

(defn interpret [lines]
  (loop [remain lines
         result {}
         state {:scope [] :buffer [] :last-key []}
         ]

    (if (empty? remain) result

      (let [; unpack state
            {:keys [scope buffer last-key]} state
            ; get current line
            line (first remain)
            ; parse line & look at line type
            [line-type & line-data] (first (line-parser line))
            ; get current target
            target (get-in result scope {})
            ; if the current target is a map, or an array of maps
            ; we can make or add a map.
            should-make-map (or (map? target)
                                (and (vector? target)
                                  (or (empty? target)
                                      (map? (first target)))))
            ; If the current target is a vector and contains strings,
            ; we can add more strings.
            should-make-text (and (vector? target) 
                                  (or (empty? target) (string? (first target))))]

        (case line-type
          :Ignore
          result

          :Normal
          (recur (rest remain) 
                 result 
                 (update-in state 
                            [:buffer] conj (format-normal (first line-data))))

          :Skip
          (recur (remove-until-endskip remain) result state)

          :EndSkip
          (recur (rest remain) result state)

          :KeyValuePair
          (if should-make-map
            (let [[k v] (transform line-data)
                  new-v (format-value v)
                  new-last-key (into scope k)
                  new-result (archie-assoc-in result new-last-key new-v)
                  new-buffer [new-v]
                  ]
              (recur (rest remain)
                     new-result
                     (assoc state :buffer new-buffer :last-key new-last-key)))
            (let [new-buffer (conj buffer line)]
              (recur (rest remain)
                     result
                     (assoc state :buffer new-buffer))))

          :ArrayMember
          (if should-make-text
            (let [new-v (format-value (first line-data))
                  new-target (or target [])
                  new-last-key (conj scope (count new-target))
                  new-result (archie-assoc-in result new-last-key new-v)
                  new-buffer [new-v]
                  ]
              (recur (rest remain)
                     new-result
                     (assoc state :buffer new-buffer :last-key new-last-key)))
            (let [new-buffer (conj buffer line)]
              (recur (rest remain)
                     result
                     (assoc state :buffer new-buffer))))

          :OpenObject
          (let [k (transform (first line-data))
                new-target (get-in result k {})
                new-result (archie-assoc-in result k new-target)]
            (recur (rest remain) new-result (assoc state :scope k)))

          :OpenArray
          (let [k (transform (first line-data))
                new-target (get-in result k [])
                new-result (archie-assoc-in result k new-target)]
            (recur (rest remain) new-result (assoc state :scope k)))

          :EndObject
          (recur (rest remain) result (assoc state :scope []
                                             :buffer [] 
                                             :last-key []))

          :EndArray
          (recur (rest remain) result (assoc state :scope []
                                             :buffer []
                                             :last-key []))

          :End
          (let [new-result (archie-assoc-in result 
                                            last-key 
                                            (->> buffer (s/join "\n") (s/trim)))]
            (recur (rest remain)
                   new-result 
                   (assoc state :buffer []
                          :last-key [])))

          )
        ))))


(defn parse-all-lines [input]
  (->> input 
       s/split-lines
       (map line-parser)
       (map first)))

(defn parse [input]
  (->> input
       (s/split-lines)
       interpret))
