package no.nav.dings

import io.ktor.util.KtorExperimentalAPI
import no.nav.dings.config.Environment

@KtorExperimentalAPI
fun main() {
    withMockOAuth2Server {
        createHttpServer(
            Environment(
                Environment.Application(
                    port = 8282
                ),
                Environment.Login(),
                Environment.Idporten(
                    wellKnownUrl = this.wellKnownUrl("idporten").toString(),
                    clientSecret = "secret",
                    clientId = "101010"
                ),
                Environment.TokenX(
                    wellKnownUrl = this.wellKnownUrl("tokenx").toString(),
                    privateJwk = generateRsaKey().first.toJSONObject().toJSONString(),
                    clientId = "909090"
                )
            ),
            ApplicationStatus()
        ).start(wait = true)
    }
}
