package me.viartemev.ktor.me.viartemev.ktor.service

import me.viartemev.ktor.domain.Event
import me.viartemev.ktor.me.viartemev.ktor.kafka.dispatch
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord

const val EVENTS_TOPIC = "events"

class EventService(private val kafkaProducer: KafkaProducer<Long, Event>) {
    suspend fun sendEvent(event: Event) =
        kafkaProducer.dispatch(ProducerRecord<Long, Event>(EVENTS_TOPIC, 0, event))
}
