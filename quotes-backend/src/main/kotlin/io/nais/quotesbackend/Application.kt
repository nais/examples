package io.nais.quotesbackend

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.nais.quotesbackend.database.DatabaseFactory
import io.nais.quotesbackend.database.QuoteService
import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

var globalErrorRate: Double = 0.1

@Serializable data class Quote(val id: String, val text: String, val author: String)

@Serializable data class QuoteInput(val text: String, val author: String)

fun main() {
        embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
        log.info("Using global error rate: $globalErrorRate")

        DatabaseFactory.init()
        val quoteService = QuoteService()

        install(CallLogging) {
                level = Level.INFO
                logger = this@module.log
                format { call ->
                        val method = call.request.httpMethod.value
                        val path = call.request.uri
                        val status = call.response.status()?.value
                        "$method $path -> $status"
                }
        }

        log.info("Using global error rate: $globalErrorRate")

        install(StatusPages) {
                exception<BadRequestException> { call, cause ->
                        call.application.environment.log.warn(
                                "Bad request for ${call.request.path()}: ${cause.message}"
                        )
                        call.respond(
                                HttpStatusCode.BadRequest,
                                mapOf(
                                        "error" to "BAD_REQUEST",
                                        "message" to
                                                (cause.message
                                                        ?: "Invalid or missing request body.")
                                )
                        )
                }
                exception<SerializationException> { call, cause ->
                        call.application.environment.log.warn(
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

        install(ContentNegotiation) {
                json(
                        Json {
                                prettyPrint = true
                                isLenient = true
                        }
                )
        }

        routing {
                route("/api/quotes") {
                        get {
                                try {
                                        if (Random.nextDouble() < globalErrorRate) {
                                                throw IllegalStateException(
                                                        "Database connection failed"
                                                )
                                        }
                                        val quotes = quoteService.getAllQuotes()
                                        call.respond(quotes)
                                } catch (e: IllegalStateException) {
                                        call.application.environment.log.error(
                                                "Database error: ${e.message}"
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Unexpected error: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                }
                        }

                        post {
                                if (Random.nextDouble() < globalErrorRate) {
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                "Simulated error for observability testing"
                                        )
                                        return@post
                                }
                                try {
                                        val quoteRequest = call.receive<QuoteInput>()
                                        call.application.environment.log.debug(
                                                "Received quote request: $quoteRequest"
                                        )

                                        val newQuote =
                                                quoteService.createQuote(
                                                        quoteRequest.text,
                                                        quoteRequest.author
                                                )

                                        call.application.environment.log.info(
                                                "New quote created with ID: ${newQuote.id}"
                                        )
                                        call.respond(HttpStatusCode.Created, newQuote)
                                } catch (e: BadRequestException) {
                                        // Let StatusPages handle BadRequestException
                                        throw e
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Error creating quote: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                }
                        }

                        put("/{id}") {
                                try {
                                        val idParam =
                                                call.parameters["id"]
                                                        ?: return@put call.respond(
                                                                HttpStatusCode.BadRequest,
                                                                mapOf(
                                                                        "error" to "MISSING_ID",
                                                                        "message" to "Missing ID"
                                                                )
                                                        )
                                        val id =
                                                idParam.toIntOrNull()
                                                        ?: return@put call.respond(
                                                                HttpStatusCode.BadRequest,
                                                                mapOf(
                                                                        "error" to "INVALID_ID",
                                                                        "message" to
                                                                                "ID must be a number"
                                                                )
                                                        )

                                        val quoteRequest = call.receive<QuoteInput>()
                                        val updatedQuote =
                                                quoteService.updateQuote(
                                                        id,
                                                        quoteRequest.text,
                                                        quoteRequest.author
                                                )
                                                        ?: return@put call.respond(
                                                                HttpStatusCode.NotFound,
                                                                mapOf(
                                                                        "error" to "NOT_FOUND",
                                                                        "message" to
                                                                                "Quote not found"
                                                                )
                                                        )

                                        call.application.environment.log.info(
                                                "Quote updated with ID: ${updatedQuote.id}"
                                        )
                                        call.respond(updatedQuote)
                                } catch (e: BadRequestException) {
                                        // Let StatusPages handle BadRequestException
                                        throw e
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Error updating quote: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                }
                        }

                        delete("/{id}") {
                                try {
                                        val idParam =
                                                call.parameters["id"]
                                                        ?: return@delete call.respond(
                                                                HttpStatusCode.BadRequest,
                                                                mapOf(
                                                                        "error" to "MISSING_ID",
                                                                        "message" to "Missing ID"
                                                                )
                                                        )
                                        val id =
                                                idParam.toIntOrNull()
                                                        ?: return@delete call.respond(
                                                                HttpStatusCode.BadRequest,
                                                                mapOf(
                                                                        "error" to "INVALID_ID",
                                                                        "message" to
                                                                                "ID must be a number"
                                                                )
                                                        )

                                        val deleted = quoteService.deleteQuote(id)
                                        if (!deleted) {
                                                return@delete call.respond(
                                                        HttpStatusCode.NotFound,
                                                        mapOf(
                                                                "error" to "NOT_FOUND",
                                                                "message" to "Quote not found"
                                                        )
                                                )
                                        }

                                        call.application.environment.log.info(
                                                "Quote deleted with ID: $id"
                                        )
                                        call.respond(HttpStatusCode.NoContent)
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Error deleting quote: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                }
                        }
                }

                get("/api/quotes/{id}") {
                        try {
                                if (Random.nextDouble() < globalErrorRate) {
                                        throw IllegalStateException(
                                                "Failed to retrieve quote due to database error"
                                        )
                                }
                                val idParam =
                                        call.parameters["id"]
                                                ?: return@get call.respond(
                                                        HttpStatusCode.BadRequest,
                                                        mapOf(
                                                                "error" to "MISSING_ID",
                                                                "message" to "Missing ID"
                                                        )
                                                )
                                val id =
                                        idParam.toIntOrNull()
                                                ?: return@get call.respond(
                                                        HttpStatusCode.BadRequest,
                                                        mapOf(
                                                                "error" to "INVALID_ID",
                                                                "message" to "ID must be a number"
                                                        )
                                                )

                                val quote =
                                        quoteService.getQuoteById(id)
                                                ?: return@get call.respond(
                                                        HttpStatusCode.NotFound,
                                                        mapOf(
                                                                "error" to "NOT_FOUND",
                                                                "message" to "Quote not found"
                                                        )
                                                )
                                call.respond(quote)
                        } catch (e: IllegalStateException) {
                                call.application.environment.log.error(
                                        "Error occurred while fetching quote: ${e.message}"
                                )
                                call.respond(
                                        HttpStatusCode.InternalServerError,
                                        mapOf("error" to "DATABASE_ERROR", "message" to e.message)
                                )
                        } catch (e: Exception) {
                                call.application.environment.log.error(
                                        "Unexpected error: ${e.message}",
                                        e
                                )
                                call.respond(
                                        HttpStatusCode.InternalServerError,
                                        mapOf("error" to "DATABASE_ERROR", "message" to e.message)
                                )
                        }
                }

                get("/api/quotes/search") {
                        try {
                                val query =
                                        call.request.queryParameters["q"]
                                                ?: return@get call.respond(
                                                        HttpStatusCode.BadRequest,
                                                        mapOf(
                                                                "error" to "MISSING_QUERY",
                                                                "message" to
                                                                        "Missing search query parameter 'q'"
                                                        )
                                                )

                                val quotes = quoteService.searchQuotes(query)
                                call.respond(quotes)
                        } catch (e: Exception) {
                                call.application.environment.log.error(
                                        "Error searching quotes: ${e.message}",
                                        e
                                )
                                call.respond(
                                        HttpStatusCode.InternalServerError,
                                        mapOf("error" to "DATABASE_ERROR", "message" to e.message)
                                )
                        }
                }

                openAPI(path = "openapi")
                swaggerUI(path = "swagger")

                route("/internal") {
                        get("/stats") {
                                try {
                                        val count = quoteService.getQuoteCount()
                                        call.respond(mapOf("total_quotes" to count))
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Error getting stats: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.InternalServerError,
                                                mapOf(
                                                        "error" to "DATABASE_ERROR",
                                                        "message" to e.message
                                                )
                                        )
                                }
                        }

                        get("/health") { call.respond(HttpStatusCode.OK, "Application is healthy") }
                        get("/ready") {
                                try {
                                        quoteService.initializeDefaultQuotes()
                                        call.respond(HttpStatusCode.OK, "Application is ready")
                                } catch (e: Exception) {
                                        call.application.environment.log.error(
                                                "Database initialization failed: ${e.message}",
                                                e
                                        )
                                        call.respond(
                                                HttpStatusCode.ServiceUnavailable,
                                                "Database not ready"
                                        )
                                }
                        }
                }
        }
}
