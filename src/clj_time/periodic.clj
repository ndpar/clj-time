(ns clj-time.periodic
  (:require [clj-time.core :as ct])
  (:import [java.time LocalDateTime Duration Period]
           [java.time.temporal TemporalAmount]))

(defprotocol MultipliableTemporalAmount
  (multiplied-by [this multiplicand] "Un umbrella for Period and Duration."))

(extend-protocol MultipliableTemporalAmount
  Period
  (multiplied-by [this multiplicand] (.multipliedBy this multiplicand))
  Duration
  (multiplied-by [this multiplicand] (.multipliedBy this multiplicand)))

(defn periodic-seq
  "Returns a sequence of date-time values growing over specific period.
  The 2 argument function takes as input the starting value and the growing value,
  returning a lazy infinite sequence.
  The 3 argument function takes as input the starting value, the upper bound value,
  and the growing value, return a lazy sequence."
  ([^LocalDateTime start ^TemporalAmount period-like]
   (map #(ct/plus start (multiplied-by period-like %))
        (iterate inc 0)))
  ([^LocalDateTime start ^LocalDateTime end ^TemporalAmount period-like]
   (take-while (partial ct/after? end) (periodic-seq start period-like))))
