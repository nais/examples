package no.nav.dings.token.tokendings

import com.nimbusds.jose.JOSEObjectType
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parametersOf
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import no.nav.dings.config.Environment
import no.nav.dings.token.AccessTokenResponse
import java.time.Instant
import java.util.UUID
import java.util.Date

private val log = KotlinLogging.logger { }

internal const val PARAMS_GRANT_TYPE = "grant_type"
internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:token-exchange"
internal const val PARAMS_SUBJECT_TOKEN_TYPE = "subject_token_type"
internal const val SUBJECT_TOKEN_TYPE = "urn:ietf:params:oauth:token-type:jwt"
internal const val PARAMS_SUBJECT_TOKEN = "subject_token"
internal const val PARAMS_AUDIENCE = "audience"
internal const val PARAMS_CLIENT_ASSERTION = "client_assertion"
internal const val PARAMS_CLIENT_ASSERTION_TYPE = "client_assertion_type"
internal const val CLIENT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer"
internal const val BEARER = "Bearer"

class TokenDingsService(
    private val tokenXConfig: Environment.TokenX
) {
    private val rsaKey = RSAKey.parse(tokenXConfig.privateJwk)

    @KtorExperimentalAPI
    fun clientAssertion(): String {
        log.info { "Getting Keys with keyID: ${rsaKey.keyID}" }
        log.info { "Getting Apps own private key and generating JWT token for integration with TokenDings" }
        return clientAssertion(tokenXConfig.clientId, tokenXConfig.metadata.tokenEndpoint, rsaKey)
    }
}

suspend fun HttpClient.tokenExchange(url: String, request: OAuth2TokenExchangeRequest) =
    this.submitForm<AccessTokenResponse>(
        url = url,
        formParameters = parametersOf(
            PARAMS_CLIENT_ASSERTION to listOf(request.clientAssertion),
            PARAMS_CLIENT_ASSERTION_TYPE to listOf(request.clientAssertionType),
            PARAMS_GRANT_TYPE to listOf(request.grantType),
            PARAMS_SUBJECT_TOKEN to listOf(request.subjectToken),
            PARAMS_SUBJECT_TOKEN_TYPE to listOf(request.subjectTokenType),
            PARAMS_AUDIENCE to listOf(request.audience)
        )
    )

fun clientAssertion(clientId: String, audience: String, rsaKey: RSAKey): String {
    val now = Date.from(Instant.now())
    return JWTClaimsSet.Builder()
        .issuer(clientId)
        .subject(clientId)
        .audience(audience)
        .issueTime(now)
        .expirationTime(Date.from(Instant.now().plusSeconds(60)))
        .jwtID(UUID.randomUUID().toString())
        .notBeforeTime(now)
        .build()
        .sign(rsaKey)
        .serialize()
}

internal fun JWTClaimsSet.sign(rsaKey: RSAKey): SignedJWT =
    SignedJWT(
        JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.keyID)
            .type(JOSEObjectType.JWT).build(),
        this
    ).apply {
        sign(RSASSASigner(rsaKey.toPrivateKey()))
    }

data class OAuth2TokenExchangeRequest(
    val clientAssertion: String,
    val subjectToken: String,
    val audience: String,
    val subjectTokenType: String = SUBJECT_TOKEN_TYPE,
    val clientAssertionType: String = CLIENT_ASSERTION_TYPE,
    val grantType: String = GRANT_TYPE
)
