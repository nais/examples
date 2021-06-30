package no.nav.dings.config

import io.ktor.http.ContentType
import java.util.Base64

const val PROMETHEUS = "/prometheus"
const val IS_ALIVE = "/isAlive"
const val IS_READY = "/isReady"

val APPLICATION_JSON = ContentType.Application.Json

internal fun encodeBase64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
