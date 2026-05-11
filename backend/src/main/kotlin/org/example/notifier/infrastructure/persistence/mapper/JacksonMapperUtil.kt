package org.example.notifier.infrastructure.persistence.mapper

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

/**
 * Utility for dynamic object ↔ Map conversion using Jackson.
 * Reduces mapper boilerplate by handling type conversions automatically.
 */
object JacksonMapperUtil {

    internal val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

        // Ignore computed properties (methods) when serializing
        setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
    }

    /**
     * Converts any domain object to Map<String, Any?>
     */
    fun toMap(obj: Any): MutableMap<String, Any?> {
        @Suppress("UNCHECKED_CAST")
        return objectMapper.convertValue(obj, Map::class.java) as MutableMap<String, Any?>
    }

    /**
     * Converts Map<String, Any?> to domain object of type T
     */
    fun <T> fromMap(map: Map<String, Any?>, clazz: Class<T>): T {
        return objectMapper.convertValue(map, clazz)
    }
}
