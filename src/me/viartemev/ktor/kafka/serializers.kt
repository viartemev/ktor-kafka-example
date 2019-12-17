package me.viartemev.ktor.me.viartemev.ktor.kafka

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer


class JacksonSerializer<T>(private val objectMapper: ObjectMapper = ObjectMapper().registerModule(JavaTimeModule())) :
    Serializer<T> {
    override fun serialize(topic: String?, data: T?): ByteArray? {
        return if (data == null) {
            null
        } else try {
            objectMapper.writeValueAsBytes(data)
        } catch (e: JsonProcessingException) {
            throw SerializationException("Error serializing JSON message", e)
        }
    }
}
