package no.nav.security.examples

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import java.net.ProxySelector
import java.net.URL
import java.util.concurrent.TimeUnit
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

internal val httpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    engine {
        customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
    }
}

private val logger = LoggerFactory.getLogger("Main")

fun main() {
    val config = Configuration()

    val jwkProvider = JwkProviderBuilder(URL(config.azureAd.openIdConfiguration.jwksUri))
        .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
        .rateLimited(10, 1, TimeUnit.MINUTES) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
        .build()

    embeddedServer(Netty, port = config.application.port) {
        install(ContentNegotiation) {
            jackson {
                enable(SerializationFeature.INDENT_OUTPUT)
            }
        }

        install(Authentication) {
            jwt {
                verifier(jwkProvider, config.azureAd.openIdConfiguration.issuer)
                realm = config.application.name
                validate { credentials ->
                    try {
                        requireNotNull(credentials.payload.audience) {
                            "Auth: Missing audience in token"
                        }
                        require(credentials.payload.audience.contains(config.azureAd.clientId)) {
                            "Auth: Valid audience not found in claims"
                        }
                        JWTPrincipal(credentials.payload)
                    } catch (e: Throwable) {
                        null
                    }
                }
            }
        }
        install(CallLogging) {
            level = Level.INFO
        }
        routing {
            authenticate {
                route("api") {
                    get {
                        call.respond(Ping("You've reached a protected endpoint at ${config.application.name}"))
                    }

                    get("tokeninfo") {
                        when (val tokenInfo = call.getTokenInfo()) {
                            null -> call.respond(HttpStatusCode.Unauthorized, "Could not find a valid principal")
                            else -> call.respond(tokenInfo)
                        }
                    }
                }
            }
        }
    }.start(wait = true)
}

private fun ApplicationCall.getTokenInfo(): Map<String, JsonNode>? = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        logger.debug("found principal $principal")
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    }

data class Ping(val ping: String)
