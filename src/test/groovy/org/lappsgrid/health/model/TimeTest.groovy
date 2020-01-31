package org.lappsgrid.health.model

import org.junit.Ignore
import org.junit.Test
import org.lappsgrid.health.util.Settings
import org.lappsgrid.health.util.Time
import redis.clients.jedis.Jedis

import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
@Ignore
class TimeTest {

    @Test
    void test() {
        println Time.epoch()
    }

    @Test
    void set() {
        Jedis redis = new Jedis(Settings.REDIS_HOST)
        String s = Time.now().toString()
        println s
        redis.set("health:lastHealthCheck", s)
    }

    @Test
    void get() {
        Jedis redis = new Jedis(Settings.REDIS_HOST)
        String s = redis.get("health:lastHealthCheck")
        println s
        ZonedDateTime t = ZonedDateTime.parse(s)
        ZonedDateTime now = Time.now()
        long msec = Time.between(t, now)
        println now.toString()
        LocalDateTime time = LocalDateTime.ofInstant(new Date(msec).toInstant(), ZoneId.of("UTC"))

        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss.S")
        println f.format(time)
    }

    @Test
    void missing() {
        Jedis redis = new Jedis(Settings.REDIS_HOST)
        String value = redis.get("foo.bar")
        println value
    }

    @Test
    void oneHour() {
        ZonedDateTime t1 = ZonedDateTime.parse("2019-12-25T20:44:25.292Z")
        ZonedDateTime t2 = ZonedDateTime.parse("2019-12-25T21:44:25.292Z")
        println Time.between(t1, t2)

    }
//    @Test
//    void putAnInt() {
//        Jedis redis = new Jedis("localhost")
//        redis.put("int.value", 5)
//    }
//
//    @Test
//    void getAnInt() {
//        Jedis redis = new Jedis("localhost")
//        println redis.get("int.value")
//    }
}
