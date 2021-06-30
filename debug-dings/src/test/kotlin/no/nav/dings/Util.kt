package no.nav.dings

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.KeyUse
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import io.ktor.util.KtorExperimentalAPI
import no.nav.dings.config.Environment
import no.nav.security.mock.oauth2.MockOAuth2Server
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.text.ParseException
import java.util.UUID
import kotlin.test.fail

internal fun <R> withMockOAuth2Server(
    test: MockOAuth2Server.() -> R
): R {
    val server = MockOAuth2Server()
    server.start()
    try {
        return server.test()
    } finally {
        server.shutdown()
    }
}

const val APP_PORT = 8888

val rsaKey = generateRsaKey()

@KtorExperimentalAPI
fun setupTestEnvironment(mockOAuth2Server: MockOAuth2Server): Environment {
    return Environment(
        application = Environment.Application(
            profile = "TEST",
            port = APP_PORT
        ),
        idporten = Environment.Idporten(
            mockOAuth2Server.wellKnownUrl("idporten").toString(),
            clientSecret = "secret",
            clientId = "101010"
        ),
        tokenX = Environment.TokenX(
            clientId = "cluster:namespace:app1",
            wellKnownUrl = mockOAuth2Server.wellKnownUrl("tokenx").toString(),
            privateJwk = rsaKey.first.toJSONObject().toJSONString(),

        )
    )
}

internal fun parseAndValidateSignatur(rsaKey: RSAKey, token: String) {
    val signedJwt = try {
        SignedJWT.parse(token)
    } catch (e: ParseException) {
        fail("Could not parse token: ${e.message}")
    }
    try {
        signedJwt.verify(RSASSAVerifier(rsaKey))
    } catch (e: Exception) {
        fail("Could not validate signature: ${e.message}")
    }
}

internal fun generateRsaKey(keyId: String = UUID.randomUUID().toString(), keySize: Int = 2048): Pair<RSAKey, PrivateKey?> {
    var privateKey: PrivateKey?
    return Pair(
        KeyPairGenerator.getInstance("RSA").apply { initialize(keySize) }.generateKeyPair()
            .let {
                privateKey = it.private
                RSAKey.Builder(it.public as RSAPublicKey)
                    .privateKey(it.private as RSAPrivateKey)
                    .keyID(keyId)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyUse(KeyUse.SIGNATURE)
                    .build()
            },
        privateKey
    )
}
