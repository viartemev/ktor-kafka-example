package me.viartemev.ktor.domain

import java.time.Instant

data class Event(val id: Long, val timestamp: Instant, val message: String)
