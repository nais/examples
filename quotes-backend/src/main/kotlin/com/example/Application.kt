package com.example

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.* // Added import for StatusPages
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException // Added import for SerializationException
import kotlinx.serialization.json.Json

// Define globalErrorRate at the top level
var globalErrorRate: Double = 0.1 // Default value, can be overridden in tests

@Serializable data class Quote(val id: String? = null, val text: String, val author: String)

fun main() {
        // You might want to set globalErrorRate from command-line args here if needed for
        // standalone runs
        // For example, by parsing args or environment variables.
        // This example focuses on test override, so we leave it as is for main.
        embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
        log.info("Using global error rate: $globalErrorRate")

        // Conditionally install StatusPages to prevent DuplicatePluginException
        if (pluginOrNull(StatusPages) == null) {
                install(StatusPages) {
                        exception<SerializationException> { call, cause ->
                                call.application.log.warn(
                                        "Request deserialization failed for ${call.request.path()}: ${cause.message}"
                                )
                                call.respond(
                                        HttpStatusCode.BadRequest,
                                        mapOf(
                                                "error" to "DESERIALIZATION_ERROR",
                                                "message" to
                                                        (cause.message
                                                                ?: "Invalid request format or missing fields.")
                                        )
                                )
                        }
                }
        } else {
                // Optional: Log or handle the case where StatusPages is already installed,
                // potentially by the test harness. We might need to ensure our specific
                // handlers are added/merged if the test harness installs a default one.
                log.debug(
                        "StatusPages plugin already installed. Skipping re-installation by module."
                )
        }

        // Conditionally install ContentNegotiation to prevent DuplicatePluginException
        if (pluginOrNull(ContentNegotiation) == null) {
                install(ContentNegotiation) {
                        json(
                                Json {
                                        prettyPrint = true
                                        isLenient = true
                                }
                        )
                }
        }

        val quotes =
                ConcurrentHashMap<String, Quote>().apply {
                        put(
                                "1",
                                Quote(
                                        "1",
                                        "Deploy with confidence—let NAIS handle the platform, you focus on the code.",
                                        "NAIS Team"
                                )
                        )
                        put(
                                "2",
                                Quote(
                                        "2",
                                        "Kubernetes is complex, but NAIS makes it simple for developers.",
                                        "Platform Engineer"
                                )
                        )
                        put(
                                "3",
                                Quote(
                                        "3",
                                        "With NAIS, continuous delivery is not just a dream—it's the default.",
                                        "DevOps Enthusiast"
                                )
                        )
                        put(
                                "4",
                                Quote(
                                        "4",
                                        "Secure by default—NAIS integrates best practices into every deployment.",
                                        "Security Advocate"
                                )
                        )
                        put(
                                "5",
                                Quote(
                                        "5",
                                        "From Helm charts to GitHub Actions, NAIS brings it all together.",
                                        "Cloud Native Developer"
                                )
                        )
                }

        routing {
                route("/api/quotes") {
                        get {
                                if (Random.nextDouble() < globalErrorRate) { // Use globalErrorRate
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                "Simulated error for observability testing"
                                        )
                                        return@get
                                }
                                call.respond(quotes.values)
                        }

                        post {
                                if (Random.nextDouble() < globalErrorRate) { // Use globalErrorRate
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                "Simulated error for observability testing"
                                        )
                                        return@post
                                }
                                val quoteRequest = call.receive<Quote>()
                                application.log.info(
                                        "Received quote request (id should be null): $quoteRequest"
                                )

                                val maxExistingId =
                                        quotes.keys
                                                .mapNotNull { key ->
                                                        try {
                                                                key.toInt()
                                                        } catch (e: NumberFormatException) {
                                                                null
                                                        }
                                                }
                                                .maxOrNull()
                                                ?: 0
                                application.log.info("Max existing ID determined: $maxExistingId")

                                val newId = (maxExistingId + 1).toString()
                                application.log.info("Generated new ID: $newId")

                                val newQuote = quoteRequest.copy(id = newId)
                                application.log.info(
                                        "Created newQuote object to be stored and responded: $newQuote"
                                )

                                if (quotes.containsKey(newId)) {
                                        application.log.warn(
                                                "Warning: Overwriting existing quote with ID $newId"
                                        )
                                }
                                quotes[newId] = newQuote

                                // Log the quote that will actually be sent in the response
                                application.log.info("Responding with: $newQuote")
                                call.respond(HttpStatusCode.Created, newQuote)
                        }
                }

                get("/api/quotes/{id}") {
                        if (Random.nextDouble() < globalErrorRate) { // Use globalErrorRate
                                call.respond(
                                        HttpStatusCode.InternalServerError,
                                        "Simulated error for observability testing"
                                )
                                return@get
                        }
                        val id =
                                call.parameters["id"]
                                        ?: return@get call.respond( // Changed to respond with JSON
                                                HttpStatusCode.BadRequest,
                                                mapOf(
                                                        "error" to "MISSING_ID",
                                                        "message" to "Missing ID"
                                                )
                                        )
                        val quote =
                                quotes[id]
                                        ?: return@get call.respond( // Changed to respond with JSON
                                                HttpStatusCode.NotFound,
                                                mapOf(
                                                        "error" to "NOT_FOUND",
                                                        "message" to "Quote not found"
                                                )
                                        )
                        call.respond(quote)
                }

                // Add OpenAPI and Swagger support
                openAPI(path = "openapi")
                swaggerUI(path = "swagger")

                // Liveness and Readiness endpoint
                route("/internal") {
                        get("/health") { call.respond(HttpStatusCode.OK, "Application is healthy") }
                }
        }
}
