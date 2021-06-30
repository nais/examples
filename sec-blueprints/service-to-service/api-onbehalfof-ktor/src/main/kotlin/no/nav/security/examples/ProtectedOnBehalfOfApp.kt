package no.nav.security.examples

import com.auth0.jwk.JwkProviderBuilder
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.github.michaelbull.result.mapBoth
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.jackson.jackson
import io.ktor.request.uri
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import java.net.ProxySelector
import java.net.URL
import java.util.concurrent.TimeUnit

internal val defaultHttpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    engine {
        customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
    }
}

private val logger = LoggerFactory.getLogger("Main")

private val config = Configuration()
private val azureAdClient = AzureAdClient(config.azureAd)
private val downstreamResourceClient = DownstreamResourceClient(azureAdClient)

fun main() {

    embeddedServer(Netty, port = config.application.port) {
        val jwkProvider = JwkProviderBuilder(URL(config.azureAd.openIdConfiguration.jwksUri))
            .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
            .rateLimited(10, 1, TimeUnit.MINUTES) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
            .build()
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
                        call.respondSuccess(
                            config = config,
                            response = "You've reached a protected endpoint in a middletier application"
                        )
                    }

                    get("tokeninfo") {
                        when (val tokenInfo = call.getTokenInfo()) {
                            null -> call.respond(HttpStatusCode.Unauthorized, "Could not find a valid principal")
                            else -> call.respond(tokenInfo)
                        }
                    }

                    get("me") {
                        val accessToken: String = checkNotNull(call.getAccessToken())
                        azureAdClient.getUserInfoFromGraph(accessToken)
                            .mapBoth(
                                success = { json -> call.respondSuccess(config, json) },
                                failure = { throwableErrorMessage -> call.respondFailure(throwableErrorMessage) }
                            )
                    }

                    get("downstream/api/tokeninfo") {
                        logger.info("call downstream api for tokeninfo")
                        call.respondWithDownstreamResource("${config.downstream.resourceUrl}/tokeninfo")
                    }

                    get("downstream/api") {
                        logger.info("ping downstream api")
                        call.respondWithDownstreamResource(config.downstream.resourceUrl)
                    }
                }
            }
        }
    }.start(wait = true)
}

private suspend fun ApplicationCall.respondWithDownstreamResource(url: String) {
    val accessToken: String = checkNotNull(getAccessToken())
    downstreamResourceClient
        .get(Resource(config.downstream.clientId, url), accessToken)
        .mapBoth(
            success = { downstreamResource ->
                respondSuccess(config, downstreamResource)
            },
            failure = { throwableErrorMessage ->
                respondFailure(throwableErrorMessage)
            }
        )
}

private suspend fun ApplicationCall.respondSuccess(config: Configuration, response: Any) =
    respond(Resource(config.azureAd.clientId, request.uri, response))

private suspend fun ApplicationCall.respondFailure(errorMessage: ThrowableErrorMessage) =
    respond(HttpStatusCode.InternalServerError, errorMessage.toErrorResponse())

private fun HttpAuthHeader.getBlob(): String? = when {
    this is HttpAuthHeader.Single && authScheme.toLowerCase() in listOf("bearer") -> blob
    else -> null
}

private fun ApplicationCall.getAccessToken(): String? = request.parseAuthorizationHeader()?.getBlob()

private fun ApplicationCall.getTokenInfo(): Map<String, JsonNode>? = authentication
    .principal<JWTPrincipal>()
    ?.let { principal ->
        logger.debug("found principal $principal")
        principal.payload.claims.entries
            .associate { claim -> claim.key to claim.value.`as`(JsonNode::class.java) }
    }
