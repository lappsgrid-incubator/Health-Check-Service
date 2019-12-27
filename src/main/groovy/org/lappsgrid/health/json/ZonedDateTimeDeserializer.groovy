package org.lappsgrid.health.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

import java.time.ZonedDateTime

/**
 *
 */
class ZonedDateTimeDeserializer extends StdDeserializer<ZonedDateTime> {

    ZonedDateTimeDeserializer() {
        super(ZonedDateTime)
    }

    @Override
    ZonedDateTime deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = parser.getCodec().readTree(parser)
        return ZonedDateTime.parse(node.textValue())
    }
}
