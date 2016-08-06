(ns clj-time.format
  "Utilities for parsing and unparsing DateTimes as Strings.

   Parsing and printing are controlled by formatters. You can either use one
   of the built in ISO 8601 and a single RFC 822 formatters or define your own, e.g.:

     (def built-in-formatter (formatters :basic-date-time))
     (def custom-formatter (formatter \"yyyyMMdd\"))

   To see a list of available built-in formatters and an example of a date-time
   printed in their format:

    (show-formatters)

   Once you have a formatter, parsing and printing are straightforward:

     => (parse custom-formatter \"20100311\")
     #object[java.time.ZonedDateTime 0x57b2c1c9 \"2010-03-11T00:00Z\"]

     => (unparse custom-formatter (date-time 2010 10 3))
     \"20101003\"

   By default the parse function always returns a ZonedDateTime instance with a UTC
   time zone, and the unparse function always represents a given ZonedDateTime
   instance in UTC. A formatter can be modified to different timezones, locales,
   etc with the functions with-zone, with-locale, with-chronology,
   with-default-year and with-pivot-year."
  (:refer-clojure :exclude [extend second])
  (:require [clj-time.core :refer :all]
            [clojure.set :refer [difference]])
  (:import [java.util Locale]
           [java.time LocalDateTime LocalDate LocalTime ZoneId ZonedDateTime]
           [java.time.format DateTimeFormatter DateTimeFormatterBuilder]
           [java.time.chrono Chronology]
           [java.time.temporal TemporalQuery]))

(defmacro fromt
    [^Class klass]
    `(reify TemporalQuery
       (queryFrom [_ temporal#]
         (. ~klass (from temporal#)))))

(def zoned-date-time-from
  (fromt ZonedDateTime))

(def local-date-time-from
  (fromt LocalDateTime))

(def local-date-from
  (fromt LocalDate))

(defn parse-best
  [^DateTimeFormatter fmt ^CharSequence s]
  (.parseBest fmt s (into-array TemporalQuery [zoned-date-time-from local-date-time-from local-date-from])))

(declare formatters)
;; The formatters map and show-formatters idea are strait from chrono.

(defn formatter
  "Returns a custom formatter for the given date-time pattern or keyword."
  ([fmts]
     (formatter fmts utc))
  ([fmts ^ZoneId dtz]
   (cond (keyword? fmts) (.withZone ^DateTimeFormatter (get formatters fmts) dtz)
         (string?  fmts) (.withZone (DateTimeFormatter/ofPattern fmts) dtz)
         :else           (.withZone ^DateTimeFormatter fmts dtz)))
  ([^ZoneId dtz fmts & more]
    (let [fms (map #(formatter % dtz) (cons fmts more))]
      (-> ^DateTimeFormatterBuilder (reduce #(.appendOptional %1 %2) (DateTimeFormatterBuilder.) fms)
        (.toFormatter)
        (.withZone dtz)))))

(defn formatter-local
  "Returns a custom formatter with no time zone info."
  ([^String fmt]
     (DateTimeFormatter/ofPattern fmt)))

(defn with-chronology
  "Return a copy of a formatter that uses the given Chronology."
  [^DateTimeFormatter f ^Chronology c]
  (.withChronology f c))

(defn with-locale
  "Return a copy of a formatter that uses the given Locale."
  [^DateTimeFormatter f ^Locale l]
  (.withLocale f l))

#_(defn with-pivot-year
  "Return a copy of a formatter that uses the given pivot year."
  [^DateTimeFormatter f ^Long pivot-year]
  (.withPivotYear f pivot-year))

(defn with-zone
  "Return a copy of a formatter that uses the given ZoneId."
  [^DateTimeFormatter f ^ZoneId zid]
  (.withZone f zid))

#_(defn with-default-year
  "Return a copy of a formatter that uses the given default year."
  [^DateTimeFormatter f ^Integer default-year]
  (.withDefaultYear f default-year))

(def ^{:doc "Map of ISO 8601 and a single RFC 822 formatters that can be used for parsing and, in most
             cases, printing."}
  formatters
  (into {} (map
    (fn [[k ^DateTimeFormatter f]] [k (.withZone f utc)])
    {:basic-date (DateTimeFormatter/BASIC_ISO_DATE)
     :basic-date-time (formatter-local "yyyyMMdd'T'HHmmss.SSS[X]")
     :basic-date-time-no-ms (formatter-local "yyyyMMdd'T'HHmmss[X]")
;     :basic-ordinal-date (ISODateTimeFormat/basicOrdinalDate)
;     :basic-ordinal-date-time (ISODateTimeFormat/basicOrdinalDateTime)
;     :basic-ordinal-date-time-no-ms (ISODateTimeFormat/basicOrdinalDateTimeNoMillis)
     :basic-time (formatter-local "HH:mm:ss.SSS[X][XXX]")
     :basic-time-no-ms (formatter-local "HH:mm:ss[X][XXX]")
;     :basic-t-time (ISODateTimeFormat/basicTTime)
;     :basic-t-time-no-ms (ISODateTimeFormat/basicTTimeNoMillis)
;     :basic-week-date (ISODateTimeFormat/basicWeekDate)
;     :basic-week-date-time (ISODateTimeFormat/basicWeekDateTime)
;     :basic-week-date-time-no-ms (ISODateTimeFormat/basicWeekDateTimeNoMillis)
     :date-best (formatter-local "yyyy-MM-dd[[ ]['T']HH:mm[:ss][.SSS][X][XXX]]")
     :date (formatter-local "yyyy-MM-dd")
;     :date-element-parser (ISODateTimeFormat/dateElementParser)
;     :date-hour (ISODateTimeFormat/dateHour)
;     :date-hour-minute (ISODateTimeFormat/dateHourMinute)
;     :date-hour-minute-second (ISODateTimeFormat/dateHourMinuteSecond)
;     :date-hour-minute-second-fraction (ISODateTimeFormat/dateHourMinuteSecondFraction)
;     :date-hour-minute-second-ms (ISODateTimeFormat/dateHourMinuteSecondMillis)
;     :date-opt-time (ISODateTimeFormat/dateOptionalTimeParser)
;     :date-parser (ISODateTimeFormat/dateParser)
     :date-time (formatter-local "yyyy-MM-dd'T'HH:mm:ss.SSSX")
;     :date-time-no-ms (ISODateTimeFormat/dateTimeNoMillis)
;     :date-time-parser (ISODateTimeFormat/dateTimeParser)
;     :hour (ISODateTimeFormat/hour)
;     :hour-minute (ISODateTimeFormat/hourMinute)
;     :hour-minute-second (ISODateTimeFormat/hourMinuteSecond)
;     :hour-minute-second-fraction (ISODateTimeFormat/hourMinuteSecondFraction)
;     :hour-minute-second-ms (ISODateTimeFormat/hourMinuteSecondMillis)
;     :local-date-opt-time (ISODateTimeFormat/localDateOptionalTimeParser)
;     :local-date (ISODateTimeFormat/localDateParser)
;     :local-time (ISODateTimeFormat/localTimeParser)
;     :ordinal-date (ISODateTimeFormat/ordinalDate)
;     :ordinal-date-time (ISODateTimeFormat/ordinalDateTime)
;     :ordinal-date-time-no-ms (ISODateTimeFormat/ordinalDateTimeNoMillis)
;     :time (ISODateTimeFormat/time)
;     :time-element-parser (ISODateTimeFormat/timeElementParser)
;     :time-no-ms (ISODateTimeFormat/timeNoMillis)
;     :time-parser (ISODateTimeFormat/timeParser)
;     :t-time (ISODateTimeFormat/tTime)
;     :t-time-no-ms (ISODateTimeFormat/tTimeNoMillis)
;     :week-date (ISODateTimeFormat/weekDate)
;     :week-date-time (ISODateTimeFormat/weekDateTime)
;     :week-date-time-no-ms (ISODateTimeFormat/weekDateTimeNoMillis)
;     :weekyear (ISODateTimeFormat/weekyear)
;     :weekyear-week (ISODateTimeFormat/weekyearWeek)
;     :weekyear-week-day (ISODateTimeFormat/weekyearWeekDay)
;     :year (ISODateTimeFormat/year)
;     :year-month (ISODateTimeFormat/yearMonth)
     :year-month-day (formatter-local "yyyy-MM-dd")
     :rfc822 (with-locale (formatter "EEE, dd MMM yyyy HH:mm:ss Z") Locale/US)
     :mysql (formatter "yyyy-M-dd HH:mm:ss")})))

(def ^{:private true} parsers
  #{:date-element-parser :date-opt-time :date-parser :date-time-parser
    :local-date-opt-time :local-date :local-time :time-element-parser
    :time-parser})

(def ^{:private true} printers
  (difference (set (keys formatters)) parsers))

(defn parse-zoned
  "Returns a ZonedDateTime instance in the UTC time zone obtained by parsing the
   given string according to the given formatter."
  ([^DateTimeFormatter fmt ^CharSequence s]
     (ZonedDateTime/parse s fmt)))

(defn ^LocalDateTime to-local-date-time
  [x]
  (condp instance? x
    ZonedDateTime (.toLocalDateTime x)
    LocalDateTime x
    LocalDate (.atStartOfDay x)
    LocalTime (.with (LocalDateTime/now) x)))

(defn parse-local
  "Returns a LocalDateTime instance obtained by parsing the
   given string according to the given formatter."
  ([^DateTimeFormatter fmt ^CharSequence s]
     (to-local-date-time (parse-best fmt s)))
  ([^String s]
     (first
      (for [f (vals formatters)
            :let [d (try (parse-local f s) (catch Exception _ nil))]
            :when d] d))))

(defn parse-local-date
  "Returns a LocalDate instance obtained by parsing the
   given string according to the given formatter."
  ([^DateTimeFormatter fmt ^CharSequence s]
     (LocalDate/parse s fmt))
  ([^String s]
     (first
      (for [f (vals formatters)
            :let [d (try (parse-local-date f s) (catch Exception _ nil))]
            :when d] d))))

(defn parse-local-time
  "Returns a LocalTime instance obtained by parsing the
  given string according to the given formatter."
  ([^DateTimeFormatter fmt ^CharSequence s]
   (LocalTime/parse s fmt))
  ([^String s]
   (first
     (for [f (vals formatters)
           :let [d (try (parse-local-time f s) (catch Exception _ nil))]
           :when d] d))))

(defn ^ZonedDateTime to-zoned-date-time
  ([x]
    (to-zoned-date-time x utc))
  ([x zone]
   (condp instance? x
     ZonedDateTime x
     LocalDateTime (.atZone x zone)
     LocalDate (-> x .atStartOfDay (to-zoned-date-time zone))
     LocalTime (.with (now) x))))

(defn parse
  "Returns a ZonedDateTime instance in the UTC time zone obtained by parsing the
   given string according to the given formatter."
  ([^DateTimeFormatter fmt ^CharSequence s]
   (when-let [d (try (parse-best fmt s) (catch Exception _ nil))]
     (to-zoned-date-time d (.getZone fmt))))
  ([^CharSequence s]
   (first
     (for [f (vals formatters)
           :let [d (try (parse f s) (catch Exception _ nil))]
           :when d] d))))

(defn unparse
  "Returns a string representing the given ZonedDateTime instance in UTC and in the
  form determined by the given formatter."
  [^DateTimeFormatter fmt ^ZonedDateTime dt]
  (.format fmt dt))

(defn unparse-local
  "Returns a string representing the given LocalDateTime instance in the
  form determined by the given formatter."
  [^DateTimeFormatter fmt ^LocalDateTime dt]
  (.format fmt dt))

(defn unparse-local-date
  "Returns a string representing the given LocalDate instance in the form
  determined by the given formatter."
  [^DateTimeFormatter fmt ^LocalDate ld]
  (.format fmt (.atZone (.atStartOfDay ld) utc)))

(defn unparse-local-time
  "Returns a string representing the given LocalTime instance in the form
  determined by the given formatter."
  [^DateTimeFormatter fmt ^LocalTime lt]
  (.format fmt lt))


(defn show-formatters
  "Shows how a given ZonedDateTime, or by default the current time, would be
  formatted with each of the available printing formatters."
  ([] (show-formatters (now)))
  ([^ZonedDateTime dt]
    (doseq [p (sort printers)]
      (let [fmt (formatters p)]
        (printf "%-40s%s\n" p (unparse fmt dt))))))

(defprotocol Mappable
  (instant->map [instant] "Returns a map representation of the given instant.
                          It will contain the following keys: :years, :months,
                          :days, :hours, :minutes and :seconds."))

(defn- to-map [years months days hours minutes seconds]
  {:years   years
   :months  months
   :days    days
   :hours   hours
   :minutes minutes
   :seconds seconds})

(extend-protocol Mappable
  ZonedDateTime
  (instant->map [dt]
    (to-map
      (.getYear dt)
      (.getMonthValue dt)
      (.getDayOfMonth dt)
      (.getHour dt)
      (.getMinute dt)
      (.getSecond dt))))

#_(extend-protocol Mappable
  Period
  (instant->map [period]
    (to-map
      (.getYears period)
      (.getMonths period)
      (.getDays period)
      (.getHours period)
      (.getMinutes period)
      (.getSeconds period))))

#_(extend-protocol Mappable
  Interval
  (instant->map [it]
    (instant->map (.toPeriod it (PeriodType/yearMonthDayTime)))))
