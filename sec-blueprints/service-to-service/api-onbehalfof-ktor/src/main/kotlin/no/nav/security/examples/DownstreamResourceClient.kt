package no.nav.security.examples

import com.fasterxml.jackson.databind.JsonNode
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(DownstreamResourceClient::class.java)

class DownstreamResourceClient(
    private val azureAdClient: AzureAdClient,
    private val httpClient: HttpClient = defaultHttpClient
) {
    suspend fun get(
        resource: Resource,
        accessToken: String
    ): Result<Resource, ThrowableErrorMessage> {
        val scopes = listOf("api://${resource.clientId}/.default")
        return azureAdClient
            .getOnBehalfOfAccessTokenForResource(scopes, accessToken)
            .andThen { oboAccessToken -> fetchFromDownstreamApi(resource, oboAccessToken) }
            .andThen { response -> Ok(resource.addResponse(response)) }
    }

    private suspend fun fetchFromDownstreamApi(
        resource: Resource,
        oboAccessToken: AccessToken
    ): Result<JsonNode, ThrowableErrorMessage> =
        runCatching {
            httpClient.get<JsonNode>(resource.url) {
                header(HttpHeaders.Authorization, "Bearer ${oboAccessToken.accessToken}")
            }
        }.fold(
            onSuccess = { result -> Ok(result) },
            onFailure = { error ->
                logger.error("received error from downstream api", error)
                Err(ThrowableErrorMessage(message = "Error response from '${resource.url}'", throwable = error))
            }
        )
}
