(ns ^:figwheel-always sprague-grundy.core
    (:require [clojure.set :refer [union difference]]))

(enable-console-print!)

;;;
;; A nim-like game has a game identifier (usually a keyword), a title, a start state, an end state, a set of rules that indicate which states
;; may follow a given state (or set of states). It may also have a partition mechanism which
;; allows the game to be split into a disjunctive sum of nim heaps.
;;
;; Fields in this record are designed to be easily configurable (e.g. in a url), so follower rules
;; and heap-equivalent rules are generated from the game id.
;;;


(defrecord Game [title start target limit])

(defn mex
  "return the minimum excludant - the first number n in (range) such that (not (p n))
usually, the predicate p determines whether n is a member of a set of integers."
  ([p]
   (mex p 0))

  ([p n]
   (loop [m n]
     (if (p m)
       (recur (inc m))
       m))))
;; (mex #{0 1 3 5 2}) => 4)
;; (mex #{1 3 5 2}) => 0

(defn nim-sum
  ([a b & c]
   (nim-sum (conj c a b)))

  ([heaps]
   (apply bit-xor heaps))
  )

(defn msb
  "most significant bit of a positive integer or zero"
  [n]
  (last (take-while #(>= n (Math.pow 2 %)) (range)))
  )

(defn game-nimber
  "calculate the single nimber of a state"
  [heaps]
  (map nim-sum heaps))

(defn winner
  "winner is either the Next player :N or the Previous player :P"
  [nimber]
)


(defn optimal-outcome
  "choose an optimal outcome"
  [outcomes heaps]
  (let [outcomes-by-nimber (zipmap (game-nimber heaps) outcomes)]

    (if-let [optimal (outcomes-by-nimber 0)]
      optimal
      (rand-nth outcomes))
    )
  )
