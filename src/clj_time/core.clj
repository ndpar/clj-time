(ns clj-time.core
  "The core namespace for date-time operations in the clj-time library.

   Create a ZonedDateTime instance with date-time (or a LocalDateTime instance with local-date-time),
   specifying the year, month, day, hour, minute, second, and millisecond:

     => (date-time 1986 10 14 4 3 27 456)
     #object[java.time.ZonedDateTime 0x744d8fac \"1986-10-14T04:03:27.000000456Z\"]

     => (local-date-time 1986 10 14 4 3 27 456)
     #object[java.time.LocalDateTime 0x4a9a47e6 \"1986-10-14T04:03:27.000000456\"]

   Less-significant fields can be omitted:

     => (date-time 1986 10 14)
     #object[java.time.ZonedDateTime 0x482ed47d \"1986-10-14T00:00Z\"]

     => (local-date-time 1986 10 14)
     #object[java.time.LocalDateTime 0x925475a \"1986-10-14T00:00\"]

   Get the current time with (now) and the start of the Unix epoch with (epoch).

   Once you have a date-time, use accessors like hour and second to access the
   corresponding fields:

     => (hour (date-time 1986 10 14 22))
     22

     => (hour (local-date-time 1986 10 14 22))
     22

   The date-time constructor always returns times in the UTC time zone. If you
   want a time with the specified fields in a different time zone, use
   from-time-zone:

     => (from-time-zone (date-time 1986 10 22) (time-zone-for-offset -2))
     #object[java.time.ZonedDateTime 0x6c2e919c \"1986-10-22T00:00-02:00\"]

   If on the other hand you want a given absolute instant in time in a
   different time zone, use to-time-zone:

     => (to-time-zone (date-time 1986 10 22) (time-zone-for-offset -2))
     #object[java.time.ZonedDateTime 0x11b1d3fc \"1986-10-21T22:00-02:00\"]

   In addition to time-zone-for-offset, you can use the time-zone-for-id and
   default-time-zone functions and the utc Var to construct or get ZoneId
   instances.

   The functions after? and before? determine the relative position of two
   ZonedDateTime instances:

     => (after? (date-time 1986 10) (date-time 1986 9))
     true

     => (after? (local-date-time 1986 10) (local-date-time 1986 9))
     true

   Often you will want to find a date some amount of time from a given date. For
   example, to find the time 1 month and 3 weeks from a given date-time:

     => (plus (date-time 1986 10 14) (months 1) (weeks 3))
     #object[java.time.ZonedDateTime 0x234517c1 \"1986-12-05T00:00Z\"]

     => (plus (local-date-time 1986 10 14) (months 1) (weeks 3))
     #object[java.time.LocalDateTime 0x6eea9202 \"1986-12-05T00:00\"]

   An Interval is used to represent the span of time between two ZonedDateTime
   instances. Construct one using interval, then query them using within?,
   overlaps?, and abuts?

     => (within? (interval (date-time 1986) (date-time 1990))
                 (date-time 1987))
     true

   To find the amount of time encompassed by an interval, use in-seconds and
   in-minutes:

     => (in-minutes (interval (date-time 1986 10 2) (date-time 1986 10 14)))
     17280

   The overlap function can be used to get an Interval representing the
   overlap between two intervals:

     => (overlap (t/interval (t/date-time 1986) (t/date-time 1990))
                             (t/interval (t/date-time 1987) (t/date-time 1991)))
     #clj_time.core.Interval{:start #object[java.time.ZonedDateTime 0x1d525b87 \"1987-01-01T00:00Z\"],
     :end #object[java.time.ZonedDateTime 0x6b9d9059 \"1990-01-01T00:00Z\"]}

   Note that all functions in this namespace work with Java Time objects or ints. If
   you need to print or parse date-times, see clj-time.format. If you need to
   coerce date-times to or from other types, see clj-time.coerce."
  (:refer-clojure :exclude [extend second])
  (:import [java.time Instant LocalDate LocalDateTime LocalTime Period ZoneId
                      Duration Clock ZonedDateTime OffsetDateTime YearMonth ZoneOffset DayOfWeek]
           [java.time.temporal TemporalAmount ChronoUnit WeekFields]
           [java.time.chrono ChronoLocalDate ChronoLocalDateTime ChronoZonedDateTime]
           [java.util Locale]))

(defprotocol DateTimeProtocol
  "Interface for various date time functions"
  (year [this] "Return the year component of the given date/time.")
  (month [this] "Return the month component of the given date/time.")
  (day [this] "Return the day of month component of the given date/time.")
  (day-of-week [this] "Return the day of week component of the given date/time. Monday is 1 and Sunday is 7")
  (hour [this] "Return the hour of day component of the given date/time. A time of 12:01am will have an hour component of 0.")
  (minute [this] "Return the minute of hour component of the given date/time.")
  (second [this] "Return the second of minute component of the given date/time.")
  (nano [this] "Return the millisecond of second component of the given date/time.")
  (equal? [this that] "Returns true if ReadableDateTime 'this' is strictly equal to date/time 'that'.")
  (after? [this that] "Returns true if ReadableDateTime 'this' is strictly after date/time 'that'.")
  (before? [this that] "Returns true if ReadableDateTime 'this' is strictly before date/time 'that'.")
  (plus- [this ^TemporalAmount period]
    "Returns a new date/time corresponding to the given date/time moved forwards by the given Period(s).")
  (minus- [this ^TemporalAmount period]
    "Returns a new date/time corresponding to the given date/time moved backwards by the given Period(s).")
  (first-day-of-the-month- [this] "Returns the first day of the month")
  (last-day-of-the-month- [this] "Returns the last day of the month")
  (week-number-of-year [this] "Returs the number of weeks in the year"))

(defprotocol InTimeUnitProtocol
  "Interface for in-<time unit> functions"
  (in-millis [this] "Return the time in milliseconds.")
  (in-seconds [this] "Return the time in seconds.")
  (in-minutes [this] "Return the time in minutes.")
  (in-hours [this] "Return the time in hours.")
  (in-days [this] "Return the time in days.")
  (in-weeks [this] "Return the time in weeks.")
  (in-months [this] "Return the time in months.")
  (in-years [this] "Return the time in years."))

(extend-protocol DateTimeProtocol
  OffsetDateTime
  (year [this] (.getYear this))
  (month [this] (.getMonthValue this))
  (day [this] (.getDayOfMonth this))
  (day-of-week [this] (-> this .getDayOfWeek .getValue))
  (hour [this] (.getHour this))
  (minute [this] (.getMinute this))
  (second [this] (.getSecond this))
  (nano [this] (.getNano this))
  (equal? [this ^OffsetDateTime that] (.isEqual this that))
  (after? [this ^OffsetDateTime that] (.isAfter this that))
  (before? [this ^OffsetDateTime that] (.isBefore this that))
  (plus- [this ^TemporalAmount period] (.plus this period))
  (minus- [this ^TemporalAmount period] (.minus this period))
  (first-day-of-the-month- [this]
    (.withDayOfMonth ^OffsetDateTime this 1))
  (last-day-of-the-month- [this]
    (.. ^OffsetDateTime this (withDayOfMonth 1) (plusMonths 1) (minusDays 1)))
  (week-number-of-year [this]
    (.get this (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault)))))

  ZonedDateTime
  (year [this] (.getYear this))
  (month [this] (.getMonthValue this))
  (day [this] (.getDayOfMonth this))
  (day-of-week [this] (-> this .getDayOfWeek .getValue))
  (hour [this] (.getHour this))
  (minute [this] (.getMinute this))
  (second [this] (.getSecond this))
  (nano [this] (.getNano this))
  (equal? [this ^ChronoZonedDateTime that] (.isEqual this that))
  (after? [this ^ChronoZonedDateTime that] (.isAfter this that))
  (before? [this ^ChronoZonedDateTime that] (.isBefore this that))
  (plus- [this ^TemporalAmount period] (.plus this period))
  (minus- [this ^TemporalAmount period] (.minus this period))
  (first-day-of-the-month- [this]
    (.withDayOfMonth ^ZonedDateTime this 1))
  (last-day-of-the-month- [this]
    (.. ^ZonedDateTime this (withDayOfMonth 1) (plusMonths 1) (minusDays 1)))
  (week-number-of-year [this]
    (.get this (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault)))))

  YearMonth
  (year [this] (.getYear this))
  (month [this] (.getMonthValue this))

  LocalDateTime
  (year [this] (.getYear this))
  (month [this] (.getMonthValue this))
  (day [this] (.getDayOfMonth this))
  (day-of-week [this] (-> this .getDayOfWeek .getValue))
  (hour [this] (.getHour this))
  (minute [this] (.getMinute this))
  (second [this] (.getSecond this))
  (nano [this] (.getNano this))
  (equal? [this ^ChronoLocalDateTime that] (.isEqual this that))
  (after? [this ^ChronoLocalDateTime that] (.isAfter this that))
  (before? [this ^ChronoLocalDateTime that] (.isBefore this that))
  (plus- [this ^TemporalAmount period] (.plus this period))
  (minus- [this ^TemporalAmount period] (.minus this period))
  (first-day-of-the-month- [this]
    (.withDayOfMonth ^LocalDateTime this 1))
  (last-day-of-the-month- [this]
    (.. ^LocalDateTime this (withDayOfMonth 1) (plusMonths 1) (minusDays 1)))
  (week-number-of-year [this]
    (.get this (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault)))))

  LocalDate
  (year [this] (.getYear this))
  (month [this] (.getMonthValue this))
  (day [this] (.getDayOfMonth this))
  (day-of-week [this] (-> this .getDayOfWeek .getValue))
  (equal? [this ^ChronoLocalDate that] (.isEqual this that))
  (after? [this ^ChronoLocalDate that] (.isAfter this that))
  (before? [this ^ChronoLocalDate that] (.isBefore this that))
  (plus- [this ^TemporalAmount period] (.plus this period))
  (minus- [this ^TemporalAmount period] (.minus this period))
  (first-day-of-the-month- [this]
    (.withDayOfMonth ^LocalDate this 1))
  (last-day-of-the-month- [this]
    (.. ^LocalDate this (withDayOfMonth 1) (plusMonths 1) (minusDays 1)))
  (week-number-of-year [this]
    (.get this (.weekOfWeekBasedYear (WeekFields/of (Locale/getDefault)))))

  LocalTime
  (hour [this] (.getHour this))
  (minute [this] (.getMinute this))
  (second [this] (.getSecond this))
  (nano [this] (.getNano this))
  (equal? [this ^Object that] (.equals this that))
  (after? [this ^LocalTime that] (.isAfter this that))
  (before? [this ^LocalTime that] (.isBefore this that))
  (plus- [this ^TemporalAmount period] (.plus this period))
  (minus- [this ^TemporalAmount period] (.minus this period))
  )

(def ^{:doc "ZoneId for UTC."}
      utc
  (ZoneOffset/UTC))

(def ^:dynamic ^Clock *clock*
  (Clock/systemUTC))

(defn now
  "Returns a ZonedDateTime for the current instant in the UTC time zone."
  []
  (ZonedDateTime/now *clock*))

#_(defn time-now
  "Returns a LocalTime for the current instant without date or time zone
  using ISOChronology in the current time zone."
  []
  (LocalTime/now *clock*))

(defn ^ZonedDateTime at-start-of-day
  "Returns a ZonedDateTime representing the start of the day. Normally midnight,
  but not always true, as in some time zones with daylight savings."
  [dt]
  (.with dt (LocalTime/of 0 0)))

(defn epoch
  "Returns a ZonedDateTime for the begining of the Unix epoch in the UTC time zone."
  []
  (ZonedDateTime/ofInstant (Instant/ofEpochMilli 0) utc))

(defn min-date
  "Minimum of the provided DateTimes."
  [dt & dts]
  (reduce #(if (before? %1 %2) %1 %2) dt dts))

(defn max-date
  "Maximum of the provided DateTimes."
  [dt & dts]
  (reduce #(if (after? %1 %2) %1 %2) dt dts))

(defn ^YearMonth year-month
  "Constructs and returns a new YearMonth.
   Specify the year and month of year. Month is 1-indexed and defaults
   to January (1)."
  ([year]
   (year-month year 1))
  ([^Integer year ^Integer month]
   (YearMonth/of year month)))

(defn ^ZonedDateTime zoned-date-time
  "Constructs and returns a new ZonedDateTime in UTC.
   Specify the year, month of year, day of month, hour of day, minute of hour,
   second of minute, and millisecond of second. Note that month and day are
   1-indexed while hour, second, minute, and millis are 0-indexed.
   Any number of least-significant components can be ommited, in which case
   they will default to 1 or 0 as appropriate."
  ([year]
   (zoned-date-time year 1 1 0 0 0 0))
  ([year month]
   (zoned-date-time year month 1 0 0 0 0))
  ([year month day]
   (zoned-date-time year month day 0 0 0 0))
  ([year month day hour]
   (zoned-date-time year month day hour 0 0 0))
  ([year month day hour minute]
   (zoned-date-time year month day hour minute 0 0))
  ([year month day hour minute second]
   (zoned-date-time year month day hour minute second 0))
  ([^Integer year ^Integer month ^Integer day ^Integer hour
    ^Integer minute ^Integer second ^Integer nanos]
   (zoned-date-time year month day hour minute second nanos utc))
  ([^Integer year ^Integer month ^Integer day ^Integer hour
    ^Integer minute ^Integer second ^Integer nanos ^ZoneId zoneId]
   (ZonedDateTime/of year month day hour minute second nanos zoneId)))

(def ^ZonedDateTime date-time zoned-date-time)

(defn ^LocalDateTime local-date-time
  "Constructs and returns a new LocalDateTime.
   Specify the year, month of year, day of month, hour of day, minute of hour,
   second of minute, and millisecond of second. Note that month and day are
   1-indexed while hour, second, minute, and millis are 0-indexed.
   Any number of least-significant components can be ommited, in which case
   they will default to 1 or 0 as appropriate."
  ([year]
   (local-date-time year 1 1 0 0 0 0))
  ([year month]
   (local-date-time year month 1 0 0 0 0))
  ([year month day]
   (local-date-time year month day 0 0 0 0))
  ([year month day hour]
   (local-date-time year month day hour 0 0 0))
  ([year month day hour minute]
   (local-date-time year month day hour minute 0 0))
  ([year month day hour minute second]
   (local-date-time year month day hour minute second 0))
  ([^Integer year ^Integer month ^Integer day ^Integer hour
    ^Integer minute ^Integer second ^Integer nanos]
   (LocalDateTime/of year month day hour minute second nanos)))

(defn ^LocalDate local-date
  "Constructs and returns a new LocalDate.
   Specify the year, month, and day. Does not deal with timezones."
  [^Integer year ^Integer month ^Integer day]
  (LocalDate/of year month day))

(defn ^LocalTime local-time
  "Constructs and returns a new LocalTime.
   Specify the hour of day, minute of hour, second of minute, and millisecond of second.
   Any number of least-significant components can be ommited, in which case
   they will default to 1 or 0 as appropriate."
  ([hour]
   (local-time hour 0 0 0))
  ([hour minute]
   (local-time hour minute 0 0))
  ([hour minute second]
   (local-time hour minute second 0))
  ([^Integer hour ^Integer minute ^Integer second ^Integer nanos]
   (LocalTime/of hour minute second nanos)))

(defn ^LocalDate today
  "Constructs and returns a new LocalDate representing today's date.
   LocalDate objects do not deal with timezones at all."
  []
  (LocalDate/now *clock*))

(defn time-zone-for-offset
  "Returns a ZoneOffset for the given offset, specified either in hours or
   hours and minutes."
  ([hours]
   (time-zone-for-offset hours 0))
  ([hours minutes]
   (ZoneOffset/ofHoursMinutes hours minutes)))

(defn time-zone-for-id
  "Returns a ZoneId for the given ID, which must be in long form, e.g.
   'America/Matamoros'."
  [^String id]
  (ZoneOffset/of id {"UTC" "Z"}))

(defn available-ids
  "Returns a set of available IDs for use with time-zone-for-id."
  []
  (ZoneId/getAvailableZoneIds))

(defn default-time-zone
  "Returns the default ZoneId for the current environment."
  []
  (ZoneId/systemDefault))

(defn ^ZonedDateTime
  to-time-zone
  "Returns a new ReadableDateTime corresponding to the same absolute instant in time as
   the given ReadableDateTime, but with calendar fields corresponding to the given
   TimeZone."
  [^ZonedDateTime dt ^ZoneId tz]
  (.withZoneSameInstant dt tz))

(defn ^ZonedDateTime
  from-time-zone
  "Returns a new ReadableDateTime corresponding to the same point in calendar time as
   the given ReadableDateTime, but for a correspondingly different absolute instant in
   time."
  [^ZonedDateTime dt ^ZoneId tz]
  (.withZoneSameLocal dt tz))

(defn years
  "Given a number, returns a Period representing that many years.
   Without an argument, returns a PeriodType representing only years."
  #_([]
   (ChronoUnit/YEARS))
  ([^Integer n]
   (Period/ofYears n)))

(defn months
  "Given a number, returns a Period representing that many months.
   Without an argument, returns a PeriodType representing only months."
  #_([]
   (ChronoUnit/MONTHS))
  ([^Integer n]
   (Period/ofMonths n)))

(defn weeks
  "Given a number, returns a Period representing that many weeks.
   Without an argument, returns a PeriodType representing only weeks."
  #_([]
   (ChronoUnit/WEEKS))
  ([^Integer n]
   (Period/ofWeeks n)))

(defn days
  "Given a number, returns a Period representing that many days.
   Without an argument, returns a PeriodType representing only days."
  #_([]
   (ChronoUnit/DAYS))
  ([^Long n]
   (Period/ofDays n)))

(defn hours
  "Given a number, returns a Period representing that many hours.
   Without an argument, returns a PeriodType representing only hours."
  #_([]
   (ChronoUnit/HOURS))
  ([^Long n]
   (Duration/ofHours n)))

(defn minutes
  "Given a number, returns a Period representing that many minutes.
   Without an argument, returns a PeriodType representing only minutes."
  #_([]
   (ChronoUnit/MINUTES))
  ([^Long n]
   (Duration/ofMinutes n)))

(defn seconds
  "Given a number, returns a Period representing that many seconds.
   Without an argument, returns a PeriodType representing only seconds."
  ([]
   (ChronoUnit/SECONDS))
  ([^Long n]
   (Duration/ofSeconds n)))

(defrecord Interval [start end])

(extend-protocol InTimeUnitProtocol
  Interval
  (in-millis [{from :start to :end}] (.between ChronoUnit/MILLIS from to))
  (in-seconds [{from :start to :end}] (.between ChronoUnit/SECONDS from to))
  (in-minutes [{from :start to :end}] (.between ChronoUnit/MINUTES from to))
  (in-hours [{from :start to :end}] (.between ChronoUnit/HOURS from to))
  (in-days [{from :start to :end}] (.between ChronoUnit/DAYS from to))
  (in-weeks [{from :start to :end}] (.between ChronoUnit/WEEKS from to))
  (in-months [{from :start to :end}] (.between ChronoUnit/MONTHS from to))
  (in-years [{from :start to :end}] (.between ChronoUnit/YEARS from to))
  Duration
  (in-millis [this] (.toMillis this))
  (in-seconds [this] (.get this (seconds)))
  (in-minutes [this] (.toMinutes this))
  (in-hours [this] (.toHours this))
  (in-days [this] (.toDays this))
  (in-weeks [this] (quot (in-days this) 7))
  (in-months [this] (throw
                      (UnsupportedOperationException.
                        "Cannot convert to Months because months vary in length.")))
  (in-years [this] (throw
                     (UnsupportedOperationException.
                       "Cannot convert to Months because months vary in length.")))
  Period
  (in-millis [this] (* 1000 (in-seconds this)))
  (in-seconds [this] (* 60 (in-minutes this)))
  (in-minutes [this] (* 60 (in-hours this)))
  (in-hours [this] (* 24 (in-days this)))
  (in-days [this] (if (or (pos? (.getYears this)) (pos? (.getMonths this)))
                    (throw
                      (UnsupportedOperationException.
                        "Cannot get the number of days."))
                    (.getDays this)))
  (in-weeks [this] (quot (in-days this) 7))
  (in-months [this] (if (pos? (.getDays this))
                      (throw
                        (UnsupportedOperationException.
                          "Cannot convert to Months because months vary in length."))
                      (+ (* 12 (.getYears this)) (.getMonths this))))
  (in-years [this] (if (pos? (.getDays this))
                     (throw
                       (UnsupportedOperationException.
                         "Cannot convert to Years because years vary in length."))
                     (+ (.getYears this) (quot (.getMonths this) 12)))))


#_(defn millis
  "Given a number, returns a Period representing that many milliseconds.
   Without an argument, returns a PeriodType representing only milliseconds."
  ([]
   (ChronoUnit/MILLIS))
  ([^Integer n]
   (Duration/ofMillis n)))

(defn nanos
    "Given a number, returns a Period representing that many milliseconds.
     Without an argument, returns a PeriodType representing only milliseconds."
    #_([]
     (ChronoUnit/NANOS))
    ([^Integer n]
     (Duration/ofNanos n)))

(defn plus
  "Returns a new date/time corresponding to the given date/time moved forwards by
   the given Period(s)."
  ([dt ^TemporalAmount p]
     (plus- dt p))
  ([dt p & ps]
     (reduce plus- (plus- dt p) ps)))

(defn minus
  "Returns a new date/time object corresponding to the given date/time moved backwards by
   the given Period(s)."
  ([dt ^TemporalAmount p]
   (minus- dt p))
  ([dt p & ps]
     (reduce minus- (minus- dt p) ps)))

(defn ago
  "Returns a ZonedDateTime a supplied period before the present.
  e.g. (-> 5 years ago)"
  [^TemporalAmount period]
  (minus (now) period))

(defn yesterday
  "Returns a ZonedDateTime for yesterday relative to now"
  []
  (-> 1 days ago))

(defn from-now
  "Returns a ZonedDateTime a supplied period after the present.
  e.g. (-> 30 minutes from-now)"
  [^TemporalAmount period]
  (plus (now) period))

(defn earliest
  "Returns the earliest of the supplied DateTimes"
  ([^Comparable dt1 ^Comparable dt2]
     (if (pos? (compare dt1 dt2)) dt2 dt1))
  ([dts]
     (reduce (fn [dt1 dt2]
               (if (pos? (compare dt1 dt2)) dt2 dt1)) dts)))

(defn latest
  "Returns the latest of the supplied DateTimes"
  ([^Comparable dt1 ^Comparable dt2]
     (if (neg? (compare dt1 dt2)) dt2 dt1))
  ([dts]
     (reduce (fn [dt1 dt2]
               (if (neg? (compare dt1 dt2)) dt2 dt1)) dts)))

(defn interval
  "Returns an interval representing the span between the two given ReadableDateTimes.
   Note that intervals are closed on the left and open on the right."
  [^ZonedDateTime start ^ZonedDateTime end]
  (Interval. start end))

(defn start
  "Returns the start ZonedDateTime of an Interval."
  [^Interval in]
  (:start in))

(defn end
  "Returns the end ZonedDateTime of an Interval."
  [^Interval in]
  (:end in))

(defn extend
  "Returns an Interval with an end ReadableDateTime the specified Period after the end
   of the given Interval"
  [^Interval in & by]
  (update-in in [:end] #(apply plus % by)))


(defn within?
  "With 2 arguments: Returns true if the given Interval contains the given
   ReadableDateTime. Note that if the ReadableDateTime is exactly equal to the
   end of the interval, this function returns false.
   With 3 arguments: Returns true if the start ReadablePartial is
   equal to or before and the end ReadablePartial is equal to or after the test
   ReadablePartial."
  ([^Interval i dt]
     (and (before? (start i) dt) (after? (end i) dt)))
  ([start end test]
     (or (equal? start test)
         (equal? end test)
         (and (before? start test) (after? end test)))))

(defn overlaps?
  "With 2 arguments: Returns true of the two given Intervals overlap.
   Note that intervals that satisfy abuts? do not satisfy overlaps?
   With 4 arguments: Returns true if the range specified by start-a and end-a
   overlaps with the range specified by start-b and end-b."
  ([^Interval i-a ^Interval i-b]
   (let [start-a (start i-a) end-a (end i-a)
         start-b (start i-b) end-b (end i-b)]
     (or (and (before? start-b end-a) (after? end-b start-a))
         (and (after? end-b start-a) (before? start-b end-a)))))
  ([start-a end-a start-b end-b]
     (or (and (before? start-b end-a) (after? end-b start-a))
         (and (after? end-b start-a) (before? start-b end-a))
         (or (equal? start-a end-b) (equal? start-b end-a)))))

(defn overlap
  "Returns an Interval representing the overlap of the specified Intervals.
   Returns nil if the Intervals do not overlap.
   The first argument must not be nil.
   If the second argument is nil then the overlap of the first argument
   and a zero duration interval with both start and end times equal to the
   current time is returned."
  [^Interval i-a ^Interval i-b]
     ;; joda-time AbstractInterval.overlaps:
     ;;    null argument means a zero length interval 'now'.
     (cond (nil? i-b) (let [n (now)] (overlap i-a (interval n n)))
           (overlaps? i-a i-b) (interval (latest (start i-a) (start i-b))
                                         (earliest (end i-a) (end i-b)))
           :else nil))

(defn abuts?
  "Returns true if Interval i-a abuts i-b, i.e. then end of i-a is exactly the
   beginning of i-b."
  [^Interval i-a ^Interval i-b]
  (or (equal? (start i-a) (end i-b))
      (equal? (start i-b) (end i-a))))

#_(defn years?
  "Returns true if the given value is an instance of Years"
  [val]
  (instance? Years val))

#_(defn months?
  "Returns true if the given value is an instance of Months"
  [val]
  (instance? Months val))

#_(defn weeks?
  "Returns true if the given value is an instance of Weeks"
  [val]
  (instance? Weeks val))

#_(defn days?
  "Returns true if the given value is an instance of Days"
  [val]
  (instance? Days val))

#_(defn hours?
  "Returns true if the given value is an instance of Hours"
  [val]
  (instance? Hours val))

#_(defn minutes?
  "Returns true if the given value is an instance of Minutes"
  [val]
  (instance? Minutes val))

#_(defn seconds?
  "Returns true if the given value is an instance of Seconds"
  [val]
  (instance? Seconds val))

(defn mins-ago
  [d]
  (in-minutes (interval d (now))))

(defn first-day-of-the-month
  ([^long year ^long month]
     (first-day-of-the-month- (date-time year month)))
  ([dt]
     (first-day-of-the-month- dt)))

(defn last-day-of-the-month
  ([^long year ^long month]
     (last-day-of-the-month- (date-time year month)))
  ([dt]
     (last-day-of-the-month- dt)))

(defn number-of-days-in-the-month
  (^long [^ZonedDateTime dt]
         (day (last-day-of-the-month- dt)))
  (^long [^long year ^long month]
         (day (last-day-of-the-month- (date-time year month)))))

(defn nth-day-of-the-month
  "Returns the nth day of the month."
  ([^long year ^long month ^long n]
   (nth-day-of-the-month (date-time year month) n))
  ([^ZonedDateTime dt ^long n]
   (plus (first-day-of-the-month dt)
         (days (- n 1)))))

(defn ^ZonedDateTime today-at
  ([^long hours ^long minutes ^long seconds ^long nanos]
   (.. (now)
       (withHour   hours)
       (withMinute minutes)
       (withSecond seconds)
       (withNano   nanos)))
  ([^long hours ^long minutes ^long seconds]
   (today-at hours minutes seconds 0))
  ([^long hours ^long minutes]
   (today-at hours minutes 0)))

(defn do-at* [^ZonedDateTime base-date-time body-fn]
  (binding [*clock* (Clock/fixed (.toInstant base-date-time) utc)]
    (try
      (body-fn))))

(defmacro do-at
  "Like clojure.core/do except evalautes the expression at the given date-time"
  [^ZonedDateTime base-date-time & body]
  `(do-at* ~base-date-time
    (fn [] ~@body)))

(defn ^ZonedDateTime floor
  "Floors the given date-time dt to the given time unit dt-fn,
  e.g. (floor (now) hour) returns (now) for all units
  up to and including the hour"
  ([^ZonedDateTime dt dt-fn]
   (let [dt-fns [year month day hour minute second nano]]
    (apply date-time
      (map apply
        (concat (take-while (partial not= dt-fn) dt-fns) [dt-fn])
        (repeat [dt]))))))
