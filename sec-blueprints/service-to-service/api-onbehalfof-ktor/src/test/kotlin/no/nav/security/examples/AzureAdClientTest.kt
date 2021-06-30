package no.nav.security.examples

import com.github.michaelbull.result.expect
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.TokenRequest
import io.ktor.client.request.get
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AzureAdTokenCallback(
    private val audience: List<String>,
    private val claims: Map<String, String> = emptyMap(),
    issuerId: String,
) : DefaultOAuth2TokenCallback(issuerId = issuerId, audience = audience) {
    override fun audience(tokenRequest: TokenRequest): List<String> = audience
    override fun addClaims(tokenRequest: TokenRequest): Map<String, Any> = claims
}

@KtorExperimentalAPI
object AzureAdClientTest {
    private val oauth2Server = MockOAuth2Server()
    private const val issuer = "some-issuer"
    private const val clientId = "some-client-id"

    @BeforeAll
    @JvmStatic
    fun start() {
        oauth2Server.start()
    }

    @AfterAll
    @JvmStatic
    fun teardown() {
        oauth2Server.shutdown()
    }

    @Test
    fun `given valid credentials, getting access token for resource should succeed`() {
        val config = getAzureConfig()
        val azureAdClient = AzureAdClient(config)
        val resource = "some-resource"
        val scopes = listOf("api://$resource/.default")

        oauth2Server.enqueueCallback(
            AzureAdTokenCallback(
                audience = listOf(resource),
                issuerId = issuer,
                claims = mapOf("azp" to clientId)
            )
        )

        runBlocking { azureAdClient.getAccessTokenForResource(scopes) }
            .expect { "should return a valid access token" }
            .let { accessTokenResponse ->
                with(SignedJWT.parse(accessTokenResponse.accessToken)) {
                    assertEquals(listOf(resource), jwtClaimsSet.audience, "audience should only contain some-resource")
                    assertEquals(config.openIdConfiguration.issuer, jwtClaimsSet.issuer, "issuer should be some-issuer")
                    assertEquals(clientId, jwtClaimsSet.getStringClaim("azp"), "azp should be some-client-id")
                }
            }
    }

    @Test
    fun `given valid access token, exchanging access token for resource on behalf of caller should succeed`() {
        val config = getAzureConfig()
        val azureAdClient = AzureAdClient(config)
        val originalAccessToken = oauth2Server.issueToken(
            issuerId = issuer,
            clientId = clientId,
            tokenCallback = AzureAdTokenCallback(
                audience = listOf(clientId),
                claims = mapOf("azp" to clientId),
                issuerId = issuer
            )
        )
        val resource = "some-resource"
        val scopes = listOf("api://$resource/.default")

        oauth2Server.enqueueCallback(
            AzureAdTokenCallback(
                audience = listOf(resource),
                issuerId = issuer
            )
        )

        runBlocking {
            azureAdClient.getOnBehalfOfAccessTokenForResource(scopes, originalAccessToken.serialize())
        }.expect { "should return a valid access token" }
            .let { accessTokenResponse ->
                with(SignedJWT.parse(accessTokenResponse.accessToken)) {
                    assertEquals(listOf(resource), jwtClaimsSet.audience, "audience should only contain some-resource")
                    assertEquals(config.openIdConfiguration.issuer, jwtClaimsSet.issuer, "issuer should be some-issuer")
                    assertEquals(clientId, jwtClaimsSet.getStringClaim("azp"), "azp should be some-client-id")
                }
            }
    }

    private fun getAzureConfig(): Configuration.AzureAd {
        val wellKnownUrl = oauth2Server.wellKnownUrl(issuer).toString()
        return Configuration.AzureAd(
            clientId = clientId,
            clientSecret = "some-client-secret",
            wellKnownConfigurationUrl = wellKnownUrl,
            openIdConfiguration = runBlocking { defaultHttpClient.get(wellKnownUrl) }
        )
    }
}
