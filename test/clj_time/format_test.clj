(ns clj-time.format-test
  (:refer-clojure :exclude [extend second])
  (:require [clojure.test :refer :all]
            [clj-time [core :refer :all] [format :refer :all]])
  (:import [java.time ZoneOffset]
           java.util.Locale))

(deftest test-to-zoned-date-time
  (is (= (date-time 2010 3 11 1 2)
         (to-zoned-date-time (date-time 2010 3 11 1 2))))
  (is (= (date-time 2010 3 11 1 2)
         (to-zoned-date-time (local-date-time 2010 3 11 1 2))))
  (is (= (date-time 2010 3 11)
         (to-zoned-date-time (local-date 2010 3 11))))
  (is (= (date-time 2010 3 11 1 2)
         (do-at (date-time 2010 3 11)
                (to-zoned-date-time (local-time 1 2))))))

(deftest test-parse-best
  (let [fmt (formatter-local "yyyy-MM-dd[[ ]['T']HH:mm[:ss][X][XXX]]")]
    (is (= (local-date 2016 7 7)
           (parse-best fmt "2016-07-07")))
    (is (= (local-date-time 2016 7 7 13 14 15)
           (parse-best fmt "2016-07-07 13:14:15")))
    (is (= (zoned-date-time 2016 7 7 13 14)
           (parse-best fmt "2016-07-07 13:14Z")))))

(deftest test-formatter
  (testing "with string pattern"
    (let [fmt (formatter "yyyyMMdd")]
      (is (= (date-time 2010 3 11)
             (parse fmt "20100311")))))
  (testing "with time zone"
    (let [dtz (time-zone-for-id "America/Detroit")
          fmt (formatter "yyyyMMdd" dtz)]
      (is (= (from-time-zone (date-time 2010 3 11) dtz)
             (parse fmt "20100311")))))
  (testing "with keyword"
    (let [dtz (time-zone-for-id "America/Detroit")
          fmt (formatter dtz :year-month-day :basic-date "yy/dd/MM")]
      (is (= (from-time-zone (date-time 2097 06 17) dtz)
             (parse fmt "97/17/06")))))
  (testing "with formatter"
    (let [dtz (time-zone-for-id "America/Detroit")
          fmt (formatter dtz (formatters :year-month-day) (formatters :basic-date) "yy/dd/MM")]
      (is (= (from-time-zone (date-time 2097 06 17) dtz)
             (parse fmt "97/17/06"))))))

(deftest test-formatter-zoned
  (let [fmt (formatter "yyyy-MM-dd HH:mm:ss")]
    (is (= (date-time 2010 3 11 13 14 15)
           (parse-zoned fmt "2010-03-11 13:14:15")))))

(deftest test-formatter-local
  (let [fmt (formatter-local "yyyy-MM-dd HH:mm:ss")]
    (is (= (local-date-time 2010 3 11 13 14 15)
           (parse-local fmt "2010-03-11 13:14:15"))))
  (let [fmt (formatter-local "yyyyMMdd")]
    (is (= (local-date-time 2010 3 11)
           (parse-local fmt "20100311")))))

(deftest test-parse
  (is (= (date-time 2010 10 11)
         (parse "2010-10-11T00:00:00")))
  (let [fmt (formatters :date)]
    (is (= (date-time 2010 3 11)
           (parse fmt "2010-03-11"))))
  (let [fmt (formatters :basic-date-time)]
    (is (= (date-time 2010 3 11 17 49 20 881e6)
           (parse fmt "20100311T174920.881Z")))))

(deftest test-unparse
  (let [fmt (formatters :date)]
    (is (= "2010-03-11"
           (unparse fmt (date-time 2010 3 11)))))
  (let [fmt (formatters :basic-date-time)]
    (is (= "20100311T174920.881Z"
           (unparse fmt (date-time 2010 3 11 17 49 20 881e6))))
    (is (= "20100311T124920.881-0500"
           (unparse (formatter "yyyyMMdd'T'HHmmss.SSSZ"
                               (ZoneOffset/ofHours -5))
                    (date-time 2010 3 11 17 49 20 881e6))))))

(deftest test-local-parse
  (is (= (local-date-time 2010 10 11)
         (parse-local "2010-10-11T00:00:00")))
  (let [fmt (formatters :date)]
    (is (= (local-date-time 2010 3 11)
           (parse-local fmt "2010-03-11"))))
  (let [fmt (formatters :basic-date-time)]
    (is (= (local-date-time 2010 3 11 17 49 20 881e6)
           (parse-local fmt "20100311T174920.881Z")))))

(deftest test-local-unparse
  (let [fmt (formatters :date)]
    (is (= "2010-03-11"
           (unparse-local fmt (local-date-time 2010 3 11)))))
  (let [fmt (formatters :basic-date-time)]
    (is (= "20100311T174920.881"
           (unparse-local fmt (local-date-time 2010 3 11 17 49 20 881e6))))
    (is (= "20100311T174920.881"
           (unparse-local (formatter-local "yyyyMMdd'T'HHmmss.SSS")
                    (local-date-time 2010 3 11 17 49 20 881e6))))))

(deftest test-local-date-parse
  (is (= (local-date 2010 10 11)
         (parse-local-date "2010-10-11T00:00:00")))
  (let [fmt (formatters :date)]
    (is (= (local-date 2010 3 11)
           (parse-local-date fmt "2010-03-11"))))
  (let [fmt (formatters :basic-date-time)]
    (is (= (local-date 2010 3 11)
           (parse-local-date fmt "20100311T000000.000Z")))))

(deftest test-local-date-unparse
  (let [fmt (formatters :date)]
    (is (= "2010-03-11"
           (unparse-local-date fmt (local-date 2010 3 11)))))
  #_(let [fmt (formatters :basic-date-time)]
    (is (= "20100311T000000.000"
           (unparse-local-date fmt (local-date-time 2010 3 11 00 00 00 000))))))

(deftest test-local-time-parse
  (is (= (local-time 12)
         (parse-local-time "12:00:00")))
  (is (= (local-time 12 13 14 15e6)
         (parse-local-time "12:13:14.015"))))

(deftest test-local-time-unparse
  (let [fmt (formatter "HH:mm:ss.SSS")] ; Cannot use (formatters :local-time) here as it does not support printing
    (is (= "13:14:15.167"
        (unparse-local-time fmt (local-time 13 14 15 167e6))))))

(deftest test-formatter-modifiers
  (let [fmt (formatter "YYYY-MM-dd hh:mm z" (time-zone-for-id "America/Chicago"))]
    (is (= "2010-03-11 11:49 CST"
           (unparse fmt (date-time 2010 3 11 17 49 20 881)))))
  #_(let [fmt (with-zone (formatters :basic-date-time) (time-zone-for-id "America/Chicago"))]
    (is (= "20100311T114920.881-0600"
           (unparse fmt (date-time 2010 3 11 17 49 20 881e6)))))
  (let [fmt (with-locale (formatters :rfc822) Locale/ITALY)]
    (is (= "gio, 11 mar 2010 17:49:20 +0000"
           (unparse fmt (date-time 2010 3 11 17 49 20 881e6)))))
  (let [current-locale (Locale/getDefault)]
    (Locale/setDefault Locale/ITALY)
    (try
      (is (= "Thu, 11 Mar 2010 17:49:20 +0000"
             (unparse (formatters :rfc822) (date-time 2010 3 11 17 49 20 881e6))))
      (finally
        (Locale/setDefault current-locale))))
  #_(let [fmt (with-pivot-year (formatter "YY") 2050)]
    (is (= (date-time 2075 1 1)
           (parse fmt "75"))))
  #_(let [fmt (with-default-year (formatter "MM dd") 2010)]
    (is (= (date-time 2010 3 11)
           (parse fmt "03 11")))))

#_(deftest test-multi-parser
  (let [fmt (formatter utc "yyyy-MM-dd HH:mm" "yyyy/MM/dd@HH:mm" "yyyyMMddHHmm")]
    (is (= "2012-02-01 22:15"
           (unparse fmt (parse fmt "2012-02-01 22:15"))))
    (is (= "2012-02-01 22:15"
           (unparse fmt (parse fmt "2012/02/01@22:15"))))
    (is (= "2012-02-01 22:15"
           (unparse fmt (parse fmt "201202012215"))))
    (is (= (date-time 2012 2 1 22 15)
           (parse fmt "201202012215")))
    (is (= "2012-02-01 22:15"
           (unparse fmt (date-time 2012 2 1 22 15))))))

(deftest test-mysql-format
  (are [expectation mysql] (= (parse mysql) expectation)
       (date-time 2013 1 1 0 0 0) "2013-01-01 00:00:00"
       (date-time 1991 1 13 11 30 45) "1991-1-13 11:30:45"
       (date-time 2013 8 3 12 11 13) "2013-08-03 12:11:13"))

#_(deftest test-instant->map-from-interval
  (let [it (interval (date-time 1986 9 2 0 0 2)  (date-time 1986 11 30 2 5 12))]
    (is (= (instant->map it)
      {:years 0
       :months 2
       :days 28
       :hours 2
       :minutes 5
       :seconds 10}))))

(deftest test-instant->map-from-date-time
  (let [dt (date-time 1986 9 2 0 0 2)]
    (is (= (instant->map dt)
      {:years 1986
       :months 9
       :days 2
       :hours 0
       :minutes 0
       :seconds 2}))))
