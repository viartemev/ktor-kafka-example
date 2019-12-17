package me.viartemev.ktor

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.Routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.util.KtorExperimentalAPI
import me.viartemev.ktor.domain.Event
import me.viartemev.ktor.me.viartemev.ktor.feature.BackgroundJob
import me.viartemev.ktor.me.viartemev.ktor.kafka.buildConsumer
import me.viartemev.ktor.me.viartemev.ktor.kafka.buildProducer
import me.viartemev.ktor.me.viartemev.ktor.service.EventService
import me.viartemev.ktor.me.viartemev.ktor.web.events

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted (you can also use the application.conf's ktor.deployment.shutdown.url key)
        shutDownUrl = "/ktor/application/shutdown"
        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    val eventProducer = buildProducer<Long, Event>(environment)
    val eventService = EventService(eventProducer)
    install(Routing) {
        events(eventService)
    }
    install(BackgroundJob.BackgroundJobFeature) {
        name = "Kafka-Producer-Job"
        job = buildConsumer<Long, String>(environment)
    }
}