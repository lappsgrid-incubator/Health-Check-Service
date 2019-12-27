package org.lappsgrid.health.json


import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import org.lappsgrid.serialization.Data

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Our own custom Jackson serializer until the one in org.lappsgrid.serialization
 * if fixed to handle ZonedDateTime objects correctly.
 */
class Serializer {
    static final private DateTimeFormatter ISO = DateTimeFormatter.ISO_ZONED_DATE_TIME

    static final ObjectMapper mapper
    static final ObjectMapper prettyPrinter

    private static ObjectMapper configure(ObjectMapper mapper) {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        SimpleModule mod = new SimpleModule()
        mod.addSerializer(ZonedDateTime, new ZonedDateTimeSerializer())
        mod.addDeserializer(ZonedDateTime, new ZonedDateTimeDeserializer())
        mapper.registerModule(mod)
        return mapper
    }

    static {
        mapper = configure(new ObjectMapper())
        prettyPrinter = configure(new ObjectMapper())
        prettyPrinter.enable(SerializationFeature.INDENT_OUTPUT)

    }

    // No instances allowed.
    private Serializer() {

    }

    static String toJson(Object object) {
        mapper.writeValueAsString(object)
    }

    static String toPrettyJson(Object object) {
        prettyPrinter.writeValueAsString(object)
    }

    static Data parse(String json) {
        parse(String, Data)
    }

    static <T> T parse(String json, Class<T> theClass) {
        return mapper.readValue(json, theClass)
    }
}
