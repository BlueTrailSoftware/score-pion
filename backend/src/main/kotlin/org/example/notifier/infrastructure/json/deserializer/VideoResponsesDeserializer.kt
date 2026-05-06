package org.example.notifier.infrastructure.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

class VideoResponsesDeserializer : StdDeserializer<Map<String, Any>>(Map::class.java) {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Map<String, Any> {
        val node = p.codec.readTree<JsonNode>(p)

        return when {
            node.isObject -> {
                val result = mutableMapOf<String, Any>()
                node.fields().forEachRemaining { (key, value) ->
                    result[key] = value.asText()
                }
                result
            }
            node.isArray && node.size() == 0 -> {
                emptyMap()
            }
            else -> throw JsonMappingException(p, "Unexpected type for video_responses")
        }
    }
}
