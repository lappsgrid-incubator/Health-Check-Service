package org.lappsgrid.health.json

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 *
 */
class ZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {
    static final ISO = DateTimeFormatter.ISO_ZONED_DATE_TIME

    public ZonedDateTimeSerializer() {
        super(ZonedDateTime)
    }

    @Override
    void serialize(ZonedDateTime zonedDateTime, JsonGenerator json, SerializerProvider serializerProvider) throws IOException {
        json.writeString(zonedDateTime.format(ISO))
    }
}
