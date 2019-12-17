package me.viartemev.ktor.me.viartemev.ktor.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.sun.xml.internal.ws.encoding.soap.DeserializationException
import me.viartemev.ktor.domain.Event
import org.apache.kafka.common.serialization.Deserializer

class JacksonDeserializer(
    private val objectMapper: ObjectMapper = ObjectMapper()
        .registerModule(JavaTimeModule())
        .registerModule(KotlinModule())
) :
    Deserializer<Event?> {

    override fun deserialize(topic: String?, data: ByteArray?): Event? {
        try {
            if (data == null) {
                return null
            }
            return objectMapper.readValue(data, Event::class.java)
        } catch (e: Exception) {
            throw DeserializationException("Error deserializing JSON message", e)
        }
    }
}