package me.viartemev.ktor.me.viartemev.ktor.kafka

import io.ktor.application.ApplicationEnvironment
import io.ktor.server.engine.ApplicationEngineEnvironment
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@KtorExperimentalAPI
fun <K, V> buildProducer(environment: ApplicationEnvironment): KafkaProducer<K, V> {
    val producerConfig = environment.config.config("ktor.kafka.producer")
    val producerProps = Properties().apply {
        this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = producerConfig.property("bootstrap.servers").getList()
        this[ProducerConfig.CLIENT_ID_CONFIG] = producerConfig.property("client.id").getString()
        this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = producerConfig.property("key.serializer").getString()
        this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = producerConfig.property("value.serializer").getString()
    }
    return KafkaProducer(producerProps)
}

suspend inline fun <reified K : Any, reified V : Any> KafkaProducer<K, V>.dispatch(record: ProducerRecord<K, V>) =
    suspendCancellableCoroutine<RecordMetadata> { continuation ->
        this.send(record) { metadata, exception ->
            if (metadata == null) continuation.resumeWithException(exception!!) else continuation.resume(metadata)
        }
    }