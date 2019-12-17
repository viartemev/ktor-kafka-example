package me.viartemev.ktor.me.viartemev.ktor.web

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import me.viartemev.ktor.me.viartemev.ktor.service.EventService
import me.viartemev.ktor.domain.Event

fun Route.events(eventService: EventService) {

    route("/events") {
        post {
            val event = call.receive<Event>()
            eventService.sendEvent(event)
            call.respond(HttpStatusCode.Accepted)
        }
    }

}