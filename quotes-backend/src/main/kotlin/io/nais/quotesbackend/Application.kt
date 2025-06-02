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
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

var globalErrorRate: Double = 0.1

@Serializable data class Quote(val id: String? = null, val text: String, val author: String)

fun main() {
  embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
  log.info("Using global error rate: $globalErrorRate")

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
                      "message" to (cause.message ?: "Invalid or missing request body.")
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
                      "message" to (cause.message ?: "Invalid request format or missing fields.")
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

  val quotes =
          ConcurrentHashMap<String, Quote>().apply {
            put(
                    "1",
                    Quote(
                            "1",
                            "Deploy with confidence—let Nais handle the platform, you focus on the code.",
                            "Nais Team"
                    )
            )
            put(
                    "2",
                    Quote(
                            "2",
                            "Kubernetes is complex, but Nais makes it simple for developers.",
                            "Platform Engineer"
                    )
            )
            put(
                    "3",
                    Quote(
                            "3",
                            "With Nais, continuous delivery is not just a dream—it's the default.",
                            "DevOps Enthusiast"
                    )
            )
            put(
                    "4",
                    Quote(
                            "4",
                            "Secure by default—Nais integrates best practices into every deployment.",
                            "Security Advocate"
                    )
            )
            put(
                    "5",
                    Quote(
                            "5",
                            "From Helm charts to GitHub Actions, Nais brings it all together.",
                            "Cloud Native Developer"
                    )
            )
          }

  routing {
    route("/api/quotes") {
      get {
        try {
          if (Random.nextDouble() < globalErrorRate) {
            throw IllegalStateException("Database connection failed")
          }
          call.respond(quotes.values)
        } catch (e: IllegalStateException) {
          call.application.environment.log.error("Database error: ${e.message}")
          call.respond(
                  HttpStatusCode.InternalServerError,
                  mapOf("error" to "DATABASE_ERROR", "message" to e.message)
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
        val quoteRequest = call.receive<Quote>()
        call.application.environment.log.debug("Received quote request: $quoteRequest")

        val maxExistingId = quotes.keys.mapNotNull { it.toIntOrNull() }.maxOrNull() ?: 0
        val newId = (maxExistingId + 1).toString()
        val newQuote = quoteRequest.copy(id = newId)

        quotes[newId] = newQuote

        call.application.environment.log.info("New quote created with ID: $newId")
        call.respond(HttpStatusCode.Created, newQuote)
      }
    }

    get("/api/quotes/{id}") {
      try {
        if (Random.nextDouble() < globalErrorRate) {
          throw IllegalStateException("Failed to retrieve quote due to database error")
        }
      } catch (e: IllegalStateException) {
        call.application.environment.log.error("Error occurred while fetching quote: ${e.message}")
        call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "DATABASE_ERROR", "message" to e.message)
        )
        return@get
      }
      val id =
              call.parameters["id"]
                      ?: return@get call.respond(
                              HttpStatusCode.BadRequest,
                              mapOf("error" to "MISSING_ID", "message" to "Missing ID")
                      )
      val quote =
              quotes[id]
                      ?: return@get call.respond(
                              HttpStatusCode.NotFound,
                              mapOf("error" to "NOT_FOUND", "message" to "Quote not found")
                      )
      call.respond(quote)
    }

    openAPI(path = "openapi")
    swaggerUI(path = "swagger")

    route("/internal") {
      get("/health") { call.respond(HttpStatusCode.OK, "Application is healthy") }
    }
  }
}
