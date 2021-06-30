package no.nav.dings.config

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.ktor.auth.OAuthServerSettings
import io.ktor.http.HttpMethod
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import no.nav.dings.token.OauthServerConfigurationMetadata
import no.nav.dings.token.utils.defaultHttpClient
import no.nav.dings.token.utils.getOAuthServerConfigurationMetadata
import java.net.URL
import java.util.concurrent.TimeUnit

private val config: Configuration =
    systemProperties() overriding
        EnvironmentVariables()

@KtorExperimentalAPI
data class Environment(
    val application: Application = Application(),
    val login: Login = Login(),
    val idporten: Idporten = Idporten(),
    val tokenX: TokenX = TokenX(),
    val downstreamApi: DownstreamApi = DownstreamApi()
) {

    data class Application(
        val profile: String = config.getOrElse(Key("application.profile", stringType), "TEST"),
        val port: Int = config.getOrElse(Key("application.port", intType), 8080),
        val redirectUrl: String = config.getOrElse(Key("application.redirect.url", stringType), "http://localhost:$port/oauth")
    )

    data class Login(
        val idTokenCookie: String = "id_token",
        val redirectCookie: String = "redirect_uri",
        val afterLoginUri: String = "/debugger"
    )

    data class Idporten(
        val wellKnownUrl: String = config.getOrElse(
            Key("idporten.well.known.url", stringType),
            "https://oidc-ver2.difi.no/idporten-oidc-provider/.well-known/openid-configuration"
        ),
        val scope: String = config.getOrElse(Key("idporten.scope", stringType), "openid"),
        val clientId: String = config.getOrElse(Key("idporten.client.id", stringType), "client_id"),
        val clientSecret: String = config.getOrElse(Key("idporten.client.secret", stringType), "client_secret")
    ) {

        val metadata: OauthServerConfigurationMetadata =
            runBlocking {
                defaultHttpClient.getOAuthServerConfigurationMetadata(wellKnownUrl)
            }
        val jwkProvider: JwkProvider = JwkProviderBuilder(URL(metadata.jwksUri))
            .cached(10, 24, TimeUnit.HOURS)
            .rateLimited(10, 1, TimeUnit.MINUTES)
            .build()

        val oauth2ServerSettings = OAuthServerSettings.OAuth2ServerSettings(
            name = "IdPorten",
            authorizeUrl = metadata.authorizationEndpoint,
            accessTokenUrl = metadata.tokenEndpoint,
            clientId = clientId,
            clientSecret = clientSecret,
            accessTokenRequiresBasicAuth = false,
            requestMethod = HttpMethod.Post, // must POST to token endpoint
            defaultScopes = listOf(scope),
            // customise the authorization request with extra parameters
            authorizeUrlInterceptor = { this.parameters.append("response_mode", "query") }
        )
    }

    data class TokenX(
        val wellKnownUrl: String = config[Key("token.x.well.known.url", stringType)],
        val clientId: String = config[Key("token.x.client.id", stringType)],
        val privateJwk: String = config[Key("token.x.private.jwk", stringType)],
        val gcpAudience: String = config.getOrElse(Key("client.gcp.audience", stringType), "dev-gcp:plattformsikkerhet:api-dings"),
        val onpremAudience: String = config.getOrElse(Key("client.onprem.audience", stringType), "dev-fss:plattformsikkerhet:api-dings")
    ) {
        val metadata: OauthServerConfigurationMetadata =
            runBlocking {
                defaultHttpClient.getOAuthServerConfigurationMetadata(wellKnownUrl)
            }
    }

    data class DownstreamApi(
        val gcpApiUrl: String = config.getOrElse(Key("downstream.gcp.api.url", stringType), "https://api-dings.dev-gcp.nais.io/hello"),
        val onpremApiUrl: String = config.getOrElse(Key("downstream.onprem.api.url", stringType), "https://api-dings.dev-fss-pub.nais.io/hello")
    )
}
