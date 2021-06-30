package no.nav.dings.api

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readText
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import mu.KotlinLogging
import no.nav.dings.HttpException
import no.nav.dings.Jackson.defaultMapper
import no.nav.dings.authentication.idTokenPrincipal
import no.nav.dings.config.Environment
import no.nav.dings.service.DowntreamApiService
import no.nav.dings.token.tokendings.OAuth2TokenExchangeRequest
import no.nav.dings.token.tokendings.TokenDingsService
import no.nav.dings.token.tokendings.tokenExchange
import no.nav.dings.token.utils.defaultHttpClient
import java.net.URI

private val log = KotlinLogging.logger { }

internal fun Routing.debuggerApi(environment: Environment) {
    val config = environment.tokenX
    val tokenDingsService = TokenDingsService(config)
    val apiService = DowntreamApiService(config)
    authenticate("cookie") {
        get("/") {
            call.respondRedirect("/debugger", permanent = true)
        }
        route("/debugger") {
            get {
                val principal = checkNotNull(call.idTokenPrincipal())
                apiService.isOnPrem = call.request.headers.contains("onprem")
                val subjectToken: String = authCache.getIfPresent(principal.decodedJWT.subject)?.idToken() ?: "error: could not get id_token from cache"
                call.respond(
                    FreeMarkerContent(
                        "debugger.ftl",
                        mapOf(
                            "tokendings_url" to config.metadata.tokenEndpoint,
                            "client_assertion_type" to "urn:ietf:params:oauth:client-assertion-type:jwt-bearer",
                            "client_assertion" to tokenDingsService.clientAssertion(),
                            "grant_type" to "urn:ietf:params:oauth:grant-type:token-exchange",
                            "audience" to apiService.audience(),
                            "subject_token_type" to "urn:ietf:params:oauth:token-type:jwt",
                            "subject_token" to subjectToken
                        )
                    )
                )
            }

            post {
                val parameters = call.receiveParameters()
                val apiConfig = environment.downstreamApi
                val url = parameters.require("tokendings_url")
                val tokenRequest = parameters.toTokenExchangeRequest()
                try {
                    val tokenResponse = defaultHttpClient.tokenExchange(
                        url,
                        tokenRequest
                    )
                    call.respond(
                        FreeMarkerContent(
                            "tokenresponse.ftl",
                            mapOf(
                                "token_request" to tokenRequest.toFormattedTokenRequest(URI(url)),
                                "token_response" to defaultMapper.writeValueAsString(tokenResponse),
                                "api_url" to if (apiService.isOnPrem) apiConfig.onpremApiUrl else apiConfig.gcpApiUrl,
                                "bearer_token" to tokenResponse.accessToken
                            )
                        )
                    )
                } catch (e: Exception) {
                    val error = e.formatException()
                    call.respond(
                        FreeMarkerContent(
                            "tokenresponse.ftl",
                            mapOf(
                                "token_request" to tokenRequest.toFormattedTokenRequest(URI(url)),
                                "token_response" to error,
                                "api_url" to "N/A",
                                "bearer_token" to "N/A"
                            )
                        )
                    )
                }
            }

            post("/call") {
                val parameters = call.receiveParameters()
                val url = URI(parameters.require("api_url"))
                val bearerToken = parameters.require("bearer_token")
                val formattedRequest: String =
                    """
                    GET ${url.path} HTTP/1.1
                    Host: ${url.host}
                    Authorization: Bearer $bearerToken
                    """.trimIndent()
                try {
                    val response: String = defaultHttpClient.get(url.toString()) {
                        header("Authorization", "Bearer $bearerToken")
                    }
                    call.respond(
                        FreeMarkerContent(
                            "apiresponse.ftl",
                            mapOf(
                                "api_request" to formattedRequest,
                                "api_response" to response
                            )
                        )
                    )
                } catch (e: Exception) {
                    val error = e.formatException()
                    call.respond(
                        FreeMarkerContent(
                            "apiresponse.ftl",
                            mapOf(
                                "api_request" to formattedRequest,
                                "api_response" to error
                            )
                        )
                    )
                }
            }
        }
    }
}

suspend fun Exception.formatException(): String {
    log.error("caught exception: $message", this)
    return if (this is ClientRequestException) {
        "${response?.status}\n\n" +
            "${response?.readText()}"
    } else {
        "$message"
    }
}

fun Parameters.require(name: String): String =
    this[name] ?: throw HttpException(HttpStatusCode.BadRequest, "missing required param $name")

fun OAuth2TokenExchangeRequest.toFormattedTokenRequest(uri: URI): String =
    """
    POST ${uri.path} HTTP/1.1
    Host: ${uri.host}
    Content-Type: application/x-www-form-urlencoded
            
    client_assertion_type=$clientAssertionType&
    client_assertion=$clientAssertion&
    grant_type=$grantType&
    audience=$audience&
    subject_token_type=$subjectTokenType&
    subject_token=$subjectToken&
    """.trimIndent()

fun Parameters.toTokenExchangeRequest(): OAuth2TokenExchangeRequest =
    OAuth2TokenExchangeRequest(
        this["client_assertion"]!!,
        this["subject_token"]!!,
        this["audience"]!!
    )
