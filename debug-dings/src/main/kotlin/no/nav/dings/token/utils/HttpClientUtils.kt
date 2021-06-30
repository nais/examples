package no.nav.dings.token.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.url
import io.ktor.client.statement.readText
import io.ktor.http.ContentType
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import no.nav.dings.token.OauthServerConfigurationMetadata

private val log = KotlinLogging.logger { }

@KtorExperimentalAPI
internal val defaultHttpClient = HttpClient(CIO) {
    install(JsonFeature) {
        serializer = JacksonSerializer { objectMapper }
    }
}

internal val objectMapper: ObjectMapper = ObjectMapper()
    .registerKotlinModule()
    .configure(SerializationFeature.INDENT_OUTPUT, true)

internal suspend fun HttpClient.getOAuthServerConfigurationMetadata(url: String): OauthServerConfigurationMetadata =
    withLog("getting oauth server configuration metadata", url) {
        this.get<OauthServerConfigurationMetadata> {
            url(url)
            accept(ContentType.Application.Json)
        }.also { log.info { "Got WellKnown config from: $it" } }
    }

internal suspend fun <T> withLog(callName: String, url: String, block: suspend () -> T): T {
    return try {
        log.info { "$callName: $url" }
        block()
    } catch (e: Exception) {
        log.warn { "Something went wrong with request to url: $url " }
        if (e is ClientRequestException) {
            val responseMessage = e.response?.readText()
            log.error { "Error Response: $responseMessage" }
        }
        throw e
    }
}
