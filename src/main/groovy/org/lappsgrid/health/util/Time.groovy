package org.lappsgrid.health.util

import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

/**
 * Helper functions to ensure we create and measure ZonedDateTime objects consistently.
 */
class Time {

    // Do not allow instances.
    private Time() { }

    static ZonedDateTime now() {
        // Of all the ways to set a ZonedDateTime to UTC this one results in
        // the default formatting we want (a simple Z at the end).
        return ZonedDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC))
    }

    static ZonedDateTime epoch() {
        return ZonedDateTime.ofInstant(Instant.EPOCH, ZoneId.ofOffset("", ZoneOffset.UTC))
    }

    static long between(ZonedDateTime t1, ZonedDateTime t2) {
        between(t1, t2, ChronoUnit.MILLIS)
    }

    static long between(ZonedDateTime t1, ZonedDateTime t2, ChronoUnit unit) {
        return unit.between(t1, t2)
    }

    static long since(ZonedDateTime t) {
        return between(now(), t, ChronoUnit.MILLIS)
    }
    static long since(ZonedDateTime t, ChronoUnit unit) {
        between(now(), t, unit)
    }
}
