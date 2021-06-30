package no.nav.security.examples

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.expect
import com.github.michaelbull.result.expectError
import io.ktor.client.features.ClientRequestException
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val validAccessToken = AccessToken(
    accessToken = "validAccessToken",
    expiresIn = 3600,
    tokenType = "bearer"
)
private const val downstreamClientId: String = "clientId"

class DownstreamResourceClientTest {

    private val downstreamMockServer: MockWebServer = MockWebServer()
    private val azureAdClientMock: AzureAdClient = mockk()
    private val downstreamResourceClient: DownstreamResourceClient =
        DownstreamResourceClient(azureAdClient = azureAdClientMock)
    private lateinit var downstreamUrl: String

    @BeforeEach
    fun setup() {
        downstreamMockServer.start()
        downstreamUrl = "http://localhost:${downstreamMockServer.port}/ping"
    }

    @AfterEach
    fun teardown() {
        downstreamMockServer.shutdown()
    }

    @Test
    fun `given a valid access token, fetching resource from downstream resource should succeed`() {
        downstreamMockServer.enqueue(
            MockResponse().setBody(
                """
                    {
                        "ping" : "You've reached a protected endpoint at downstream API"
                    }
                """.trimIndent()
            ).setHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        )
        coEvery { azureAdClientMock.getOnBehalfOfAccessTokenForResource(any(), any()) } returns Ok(
            validAccessToken
        )
        runBlocking {
            downstreamResourceClient.get(
                Resource(downstreamClientId, downstreamUrl),
                accessToken = validAccessToken.accessToken
            )
        }.expect { "valid response from downstream server" }
            .let { resource: Resource ->
                assertTrue(resource.response.toString().isNotEmpty())
                assertTrue(downstreamMockServer.takeRequest().getHeader("Authorization")!!.contains(validAccessToken.accessToken))
            }
    }

    @Test
    fun `given an invalid access token, fetching resource from downstream resource should fail`() {
        downstreamMockServer.enqueue(MockResponse().setResponseCode(401).setBody("invalid access token"))
        val invalidOboAccessToken = "invalidOboAccessToken"
        val invalidAccessToken = "invalidAccessToken"
        coEvery { azureAdClientMock.getOnBehalfOfAccessTokenForResource(any(), any()) } returns Ok(
            validAccessToken.copy(accessToken = invalidOboAccessToken)
        )
        runBlocking {
            downstreamResourceClient.get(
                Resource(downstreamClientId, downstreamUrl),
                accessToken = invalidAccessToken
            )
        }.expectError { "unauthorized response from downstream server" }
            .let { error ->
                val exception = error.throwable as ClientRequestException
                assertEquals(exception.response?.status, HttpStatusCode.Unauthorized)
                assertTrue(downstreamMockServer.takeRequest().getHeader("Authorization")!!.contains(invalidOboAccessToken))
            }
    }
}
