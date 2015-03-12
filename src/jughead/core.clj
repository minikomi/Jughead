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

(defn special-tag-isnt [tag]
  #(not (= tag (-> % second first))))

(defn remove-until-endskip [remain]
  (rest (drop-while (special-tag-isnt :EndSkip) remain)))

(defn interpret [parsed]
  (loop [remain parsed    ; remaining lines to interpret
         result {}        ; map to return
         mode   :normal   ; current parsing mode
         state  {}]       ; current parsing state
    (case mode
      :keyblock
      ; Buffering normal lines until we hit an :end or other special case.
      ; ------------------------------------------------------------------------
      (if (empty? remain) 
        ; keep the last key value pair which was found
        (archie-assoc-in result (:keygroup state) (s/trim (:original-value state)))
        (let [[line-type line-data] (first remain)]
          (case line-type
            :Normal
            ; ------------------------------------------------------------------
            (recur (rest remain) 
                   result 
                   :keyblock
                   (update-in state [:buffer]
                              conj (s/replace (second line-data) #"^\\" "")))

            :Special
            ; ------------------------------------------------------------------
            (case (first line-data)

              :Skip 
              ; - drop until :endskip
              (recur (remove-until-endskip remain) result mode state)

              :Ignore
              ; - escape hatch
              ; - keep the last key we found.
              (archie-assoc-in result (:keygroup state) (:original-value state))

              :KeyValuePair 
              ; - reset state, enter keyblock mode
              (let [[kg v] (transform line-data)
                    new-result (archie-assoc-in result (:keygroup state) (s/trim (:original-value state)))]
                (recur (rest remain) new-result :keyblock
                       {:buffer [] :keygroup kg :original-value v}))
              :End
              (recur (rest remain) 
                     (let [{:keys [buffer keygroup original-value]} state] 
                       (archie-assoc-in result 
                                        keygroup
                                        (s/trim
                                         (str original-value "\n"
                                             (s/join "\n" buffer)))))
                     :normal
                     {})
              :EndSkip 
              (recur (rest remain) result mode state)
              (throw (Exception. "Unexpected :Special case"))))))

      ; default
      ; ------------------------------------------------------------------------
      (if (empty? remain) result
        (let [[line-type line-data] (first remain)]
        (case line-type
          :Normal
          ; --------------------------------------------------------------------
          (recur (rest remain) result mode state)
          :Special
          ; --------------------------------------------------------------------
          (case (first line-data)
            :Skip 
            ; - drop until :endskip
            (recur (remove-until-endskip remain) result mode state)

            :Ignore
            ; - escape hatch
            result

            :KeyValuePair 
            ; - reset state, enter keyblock mode
            (let [[kg v] (transform line-data)]
              (recur (rest remain)
                     result
                     :keyblock
                     {:buffer [] :keygroup kg :original-value v}))
            :End
            (recur (rest remain) result mode state)
            :EndSkip 
            (recur (rest remain) result mode state)
            (throw (Exception. "Unexpected :Special case")))))))))


(def line-parser (insta/parser (clojure.java.io/resource "archie.bnf")))

(defn parse-all-lines [input]
  (->> input 
       s/split-lines
       (map line-parser)
       (map first)))

(defn parse [input]
  (->> input
       parse-all-lines
       interpret))
