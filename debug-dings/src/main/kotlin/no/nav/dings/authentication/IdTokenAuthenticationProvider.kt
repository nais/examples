package no.nav.dings.authentication

import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.JWTVerifier
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.Authentication
import io.ktor.auth.AuthenticationFailedCause
import io.ktor.auth.AuthenticationPipeline
import io.ktor.auth.AuthenticationProvider
import io.ktor.auth.Principal
import io.ktor.auth.principal
import io.ktor.request.path
import io.ktor.response.respondRedirect
import mu.KotlinLogging

private val log = KotlinLogging.logger { }

class IdTokenAuthenticationProvider internal constructor(config: Configuration) : AuthenticationProvider(config) {
    internal val cookieName = config.cookieName
    internal val redirectUriCookieName = config.redirectUriCookieName
    internal val verifier: ((String) -> JWTVerifier?) = config.verifier
    internal val loginUrl: String = config.loginUrl

    class Configuration internal constructor(name: String?) : AuthenticationProvider.Configuration(name) {
        internal var cookieName: String = "id_token"
        internal var redirectUriCookieName: String = "redirect_uri"
        internal var loginUrl: String = "/oauth"
        internal var verifier: ((String) -> JWTVerifier?) = { null }
        internal fun build() = IdTokenAuthenticationProvider(this)
    }
}

fun Authentication.Configuration.idToken(
    name: String? = null,
    configure: IdTokenAuthenticationProvider.Configuration.() -> Unit
) {
    val provider = IdTokenAuthenticationProvider.Configuration(name).apply(configure).build()
    val verifier = provider.verifier
    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val idToken = call.request.cookies[provider.cookieName]
        try {
            if (idToken != null) {
                val decodedJWT = verifier(idToken)?.verify(idToken)
                if (decodedJWT != null) {
                    context.principal(IdTokenPrincipal(decodedJWT))
                    return@intercept
                }
            } else {
                log.debug("no idtoken cookie found. ")
                call.response.cookies.append(provider.redirectUriCookieName, call.request.path())
            }
        } catch (e: Throwable) {
            val message = e.message ?: e.javaClass.simpleName
            log.error("Token verification failed: {}", message)
        }
        context.challenge("JWTAuthKey", AuthenticationFailedCause.InvalidCredentials) {
            call.respondRedirect(provider.loginUrl)
            it.complete()
        }
    }
    register(provider)
}

data class IdTokenPrincipal(val decodedJWT: DecodedJWT) : Principal

fun ApplicationCall.idTokenPrincipal(): IdTokenPrincipal? = this.principal()
