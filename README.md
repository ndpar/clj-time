# `clj-time` <a href="http://travis-ci.org/#!/seancorfield/clj-time/builds"><img src="https://secure.travis-ci.org/seancorfield/clj-time.png" /></a> [![Dependency Status](https://www.versioneye.com/clojure/clj-time:clj-time/0.11.0/badge.png)](https://www.versioneye.com/clojure/clj-time:clj-time/0.11.0) [![Join the chat at https://gitter.im/clj-time/clj-time](https://badges.gitter.im/clj-time/clj-time.svg)](https://gitter.im/clj-time/clj-time?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)


A date and time library for Clojure, wrapping the [Java Time](https://docs.oracle.com/javase/8/docs/api/java/time/package-summary.html) API.

## Artifacts

`clj-time` artifacts are [released to Clojars](https://clojars.org/clj-time/clj-time).

If you are using Maven, add the following repository definition to your `pom.xml`:

``` xml
<repository>
  <id>clojars.org</id>
  <url>http://clojars.org/repo</url>
</repository>
```

### The Most Recent Release

With Leiningen:

``` clj
[clj-time "0.12.0"]
```

With Maven:

``` xml
<dependency>
  <groupId>clj-time</groupId>
  <artifactId>clj-time</artifactId>
  <version>0.12.0</version>
</dependency>
```

## Bugs and Enhancements

Please open issues against the [official clj-time repo on Github](https://github.com/clj-time/clj-time/issues).

## Mailing List

Please ask questions on the [clj-time mailing list](http://groups.google.com/forum/#!forum/clj-time).


## Usage

### clj-time.core

The main namespace for date-time operations in the `clj-time` library is `clj-time.core`.

``` clj
(require '[clj-time.core :as t])
```

Create a ZonedDateTime instance with date-time, specifying the year, month,
day, hour, minute, second, and nanosecond:


``` clj
(t/date-time 1986 10 14 4 3 27 456)
=> #object[java.time.ZonedDateTime 0x641654fd "1986-10-14T04:03:27.000000456Z"]
```

Less-significant fields can be omitted:

``` clj
(t/date-time 1986 10 14)
=> #object[java.time.ZonedDateTime 0x1556a972 "1986-10-14T00:00Z"]
```

Get the current time with `now` and the start of the Unix epoch with
`epoch`.

Once you have a date-time, use accessors like `hour` and `second` to
access the corresponding fields:


``` clj
(t/hour (t/date-time 1986 10 14 22))
=> 22
```

The date-time constructor always returns times in the UTC time
zone. If you want a time with the specified fields in a different time
zone, use `from-time-zone`:


``` clj
(t/from-time-zone (t/date-time 1986 10 22) (t/time-zone-for-offset -2))
=> #object[java.time.ZonedDateTime 0x830ed83 "1986-10-22T00:00-02:00"]
```

If on the other hand you want a given absolute instant in time in a
different time zone, use `to-time-zone`:


``` clj
(t/to-time-zone (t/date-time 1986 10 22) (t/time-zone-for-offset -2))
=> #object[java.time.ZonedDateTime 0x542a49f6 "1986-10-21T22:00-02:00"]
```

In addition to `time-zone-for-offset`, you can use the
`time-zone-for-id` and `default-time-zone` functions and the `utc` Var
to construct or get `ZoneId` instances.


If you only want a date with no time component, consider using the
`local-date` and `today` functions. These return `LocalDate` instances
that do not have time components (and thus don't suffer from
timezone-related shifting).


``` clj
(t/local-date 2013 3 20)
=> #object[java.time.LocalDate 0xd6c1970 "2013-03-20"]
```

The functions `equal?`, `after?`, and `before?` determine the relative position
of two ZonedDateTime instances:

``` clj
(t/equal? (t/date-time 1986 10) (t/date-time 1986 10))
=> true
(t/after? (t/date-time 1986 10) (t/date-time 1986 9))
=> true
(t/before? (t/date-time 1986 9) (t/date-time 1986 10))
=> true
```

Often you will want to find a date some amount of time from a given
date. For example, to find the time 1 month and 3 weeks from a given
date-time:


``` clj
(t/plus (t/date-time 1986 10 14) (t/months 1) (t/weeks 3))
=> #object[java.time.ZonedDateTime 0x75bf3c15 "1986-12-05T00:00Z"]
```

An `Interval` is used to represent the span of time between two
`ZonedDateTime` instances. Construct one using `interval`, then query them
using `within?`, `overlaps?`, and `abuts?`

``` clj
(t/within? (t/interval (t/date-time 1986) (t/date-time 1990))
              (t/date-time 1987))
=> true
```

The `in-seconds` and `in-minutes` functions can be used to describe
intervals in the corresponding temporal units:

``` clj
(t/in-minutes (t/interval (t/date-time 1986 10 2) (t/date-time 1986 10 14)))
=> 17280
```

The `overlap` function can be used to get an `Interval` representing the
overlap between two intervals:

``` clj
(t/overlap (t/interval (t/date-time 1986) (t/date-time 1990))
         (t/interval (t/date-time 1987) (t/date-time 1991)))
=> #clj_time.core.Interval{:start #object[java.time.ZonedDateTime 0x11f8679c "1987-01-01T00:00Z"], :end #object[java.time.ZonedDateTime 0x646c34f2 "1990-01-01T00:00Z"]}
```

`today-at` returns a moment in time at the given hour,
minute and second on the current date:

``` clj
(t/today-at 12 00)
=> #object[java.time.ZonedDateTime 0x36902b4f "2016-08-04T12:00Z"]
(t/today-at 12 00 05)
=> #object[java.time.ZonedDateTime 0x743fb9e3 "2016-08-04T12:00:05Z"]
```

### clj-time.format

If you need to parse or print date-times, use `clj-time.format`:

``` clj
(require '[clj-time.format :as f])
```

Parsing and printing are controlled by formatters. You can either use
one of the built in ISO8601 formatters or define your own, e.g.:

``` clj
(def built-in-formatter (f/formatters :basic-date-time))
(def custom-formatter (f/formatter "yyyyMMdd"))
```

To see a list of available built-in formatters and an example of a
date-time printed in their format:


``` clj
(f/show-formatters)
```

Remember that `mm` is minutes, `MM` is months, `ss` is seconds and
`SS` is milliseconds. You can find a [complete list of patterns](https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html)
on the Java Time website.

Once you have a formatter, parsing and printing are straightforward:

``` clj
(f/parse custom-formatter "20100311")
=> #object[java.time.ZonedDateTime 0x57d4f1c4 "2010-03-11T00:00Z"]

(f/unparse custom-formatter (t/date-time 2010 10 3))
=> "20101003"
```

To parse dates in multiple formats and format dates in just one
format, you can do this:


``` clj
(def multi-parser (f/formatter (t/default-time-zone) "YYYY-MM-dd" "YYYY/MM/dd"))

(f/unparse multi-parser (f/parse multi-parser "2012-02-01"))
=> "2012-02-01"

(f/unparse multi-parser (f/parse multi-parser "2012/02/01"))
=> "2012-02-01"
```

### clj-time.coerce

The namespace `clj-time.coerce` contains utility functions for
coercing Java `ZonedDateTime` instances to and from various other types:


``` clj
(require '[clj-time.coerce :as c])
```

For example, to convert a `ZonedDateTime` to and from a `long`:

``` clj
(c/to-long (t/date-time 1998 4 25))
=> 893462400000

(c/from-long 893462400000)
=> #object[java.time.ZonedDateTime 0x7587925d "1998-04-25T00:00Z"]
```

And by the magic of protocols you can pass in an isoformat string and
get the unix epoch milliseconds:

``` clj
(c/to-long "2013-08-01")
=> 1375315200000
```

There are also conversions to and from `java.util.Date` (`to-date` and
`from-date`), `java.sql.Date` (`to-sql-date` and `from-sql-date`),
`java.sql.Timestamp` (`to-sql-time` and `from-sql-time`) and several
other types.

### clj-time.local

The namespace `clj-time.local` contains functions for working with
local time without having to shift to/from utc, the preferred time
zone of clj-time.core.

``` clj
(require '[clj-time.local :as l])
```

Get the current local time with

``` clj
(l/local-now)
```

Get a local date-time instance retaining the time fields with

``` clj
(l/to-local-date-time obj)
```

The following all return 1986-10-14 04:03:27.246 with the local time
zone.

``` clj
(l/to-local-date-time (clj-time.core/date-time 1986 10 14 4 3 27 246))
(l/to-local-date-time "1986-10-14T04:03:27.246")
(l/to-local-date-time "1986-10-14T04:03:27.246Z")
```

The dynamic var `*local-formatters*` contains a map of local
formatters for parsing and printing. It is initialized with all the
formatters in clj-time.format localized.

to-local-date-time for strings uses `*local-formatters*` to parse.

Format an obj using a formatter in `*local-formatters*` corresponding
to the format-key passed in with

``` clj
(l/format-local-time (l/local-now) :basic-date-time)
```


### clj-time.periodic

`clj-time.periodic/periodic-seq` returns an infinite sequence of instants
separated by a time period starting with the given point in time:

``` clj
(require '[clj-time.periodic :as p])
(require '[clj-time.core :as t])

;; returns 10 instants starting with current time separated
;; by 12 hours
(take 10 (p/periodic-seq (t/now) (t/hours 12)))
```

### clj-time.predicates

`clj-time.predicates` comes with a set of handy predicates to
check for common conditions. For instance:

``` clj
(require '[clj-time.core :as t])
(require '[clj-time.predicates :as pr])
```
``` clojure
(pr/monday? (t/date-time 1999 9 9))
=> false

(pr/january? (t/date-time 2011 1 1))
=> true

(pr/weekend? (t/date-time 2014 1 26))
=> true

(pr/weekday? (t/date-time 2014 1 26))
=> false

(pr/last-day-of-month? (t/date-time 2014 1 26))
=> false

(pr/first-day-of-month? (t/date-time 2014 1 26))
=> false
```

### clj-time.jdbc

`clj-time.jdbc` registers protocol extensions so you don’t have to use
`clj-time.coerce` yourself to coerce to and from SQL timestamps.

From the REPL:

``` clj
(require 'clj-time.jdbc)
```

In your project:

``` clj
(ns my.neat.project
  (:require [clj-time.jdbc]))

; They're registered and ready to use.
```

Now you can use `java.time.ZonedDateTime` objects when "writing" to the database
in place of `java.sql.Timestamp` objects, and expect `java.time.ZonedDateTime`
objects when "reading" where you would have previously expected
`java.sql.Timestamp` objects.

## Development

Running the tests:

    $ rm -f test/readme.clj && lein test-all && lein test-readme

(assumes Leiningen 2.x)

## Documentation

The complete [API documentation](http://clj-time.github.com/clj-time/doc/index.html) is also available (codox generated).

## License

Released under the MIT License: <https://github.com/clj-time/clj-time/blob/master/MIT-LICENSE.txt>

