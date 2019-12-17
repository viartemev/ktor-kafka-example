package me.viartemev.ktor.me.viartemev.ktor.kafka

import io.ktor.application.ApplicationEnvironment
import io.ktor.util.KtorExperimentalAPI
import me.viartemev.ktor.me.viartemev.ktor.feature.ClosableJob
import mu.KotlinLogging
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.errors.WakeupException
import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


private val logger = KotlinLogging.logger {}

class Consumer<K, V>(private val consumer: KafkaConsumer<K, V>, topic: String) : ClosableJob {
    private val closed: AtomicBoolean = AtomicBoolean(false)
    private var finished = CountDownLatch(1)

    init {
        consumer.subscribe(listOf(topic))
    }

    override fun run() {
        try {
            while (!closed.get()) {
                val records = consumer.poll(Duration.of(1000, ChronoUnit.MILLIS))
                for (record in records) {
                    logger.info { "topic = ${record.topic()}, partition = ${record.partition()}, offset = ${record.offset()}, key = ${record.key()}, value = ${record.value()}" }
                }
                if (!records.isEmpty) {
                    consumer.commitAsync { offsets, exception ->
                        if (exception != null) {
                            logger.error(exception) { "Commit failed for offsets $offsets" }
                        } else {
                            logger.info { "Offset committed  $offsets" }
                        }
                    }
                }
            }
            logger.info { "Finish consuming" }
        } catch (e: Throwable) {
            when (e) {
                is WakeupException -> logger.info { "Consumer waked up" }
                else -> logger.error(e) { "Polling failed" }
            }
        } finally {
            logger.info { "Commit offset synchronously" }
            consumer.commitSync()
            consumer.close()
            finished.countDown()
            logger.info { "Consumer successfully closed" }
        }
    }

    override fun close() {
        logger.info { "Close job..." }
        closed.set(true)
        consumer.wakeup()
        finished.await(3000, TimeUnit.MILLISECONDS)
        logger.info { "Job is successfully closed" }
    }
}

@KtorExperimentalAPI
fun <K, V> buildConsumer(environment: ApplicationEnvironment): Consumer<K, V> {
    val consumerConfig = environment.config.config("ktor.kafka.consumer")
    val consumerProps = Properties().apply {
        this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = consumerConfig.property("bootstrap.servers").getList()
        this[ConsumerConfig.CLIENT_ID_CONFIG] = consumerConfig.property("client.id").getString()
        this[ConsumerConfig.GROUP_ID_CONFIG] = consumerConfig.property("group.id").getString()
        this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = consumerConfig.property("key.deserializer").getString()
        this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = consumerConfig.property("value.deserializer").getString()
    }
    return Consumer(KafkaConsumer(consumerProps), consumerConfig.property("topic").getString())
}