package no.nav.dings

import com.auth0.jwt.JWT
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.freemarker.FreeMarker
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.request.path
import io.ktor.routing.Routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import io.ktor.util.KtorExperimentalAPI
import mu.KotlinLogging
import no.nav.dings.api.debuggerApi
import no.nav.dings.api.exceptionHandler
import no.nav.dings.api.idTokenVerifier
import no.nav.dings.api.login
import no.nav.dings.api.selfTest
import no.nav.dings.authentication.idToken
import no.nav.dings.config.Environment
import org.slf4j.event.Level

private val log = KotlinLogging.logger { }

@KtorExperimentalAPI
fun createHttpServer(environment: Environment, applicationStatus: ApplicationStatus): NettyApplicationEngine {
    return embeddedServer(
        Netty,
        port = environment.application.port,
        module = {
            setupHttpServer(
                environment = environment,
                applicationStatus = applicationStatus
            )
        }
    )
}

@KtorExperimentalAPI
fun Application.setupHttpServer(environment: Environment, applicationStatus: ApplicationStatus) {

    log.info { "Application Profile running: ${environment.application.profile}" }
    val idporten = environment.idporten
    install(Authentication) {

        oauth(idporten.oauth2ServerSettings.name) {
            client = HttpClient(CIO)
            providerLookup = { idporten.oauth2ServerSettings }
            urlProvider = { environment.application.redirectUrl }
        }

        idToken("cookie") {
            val jwkProvider = environment.idporten.jwkProvider
            cookieName = environment.login.idTokenCookie
            redirectUriCookieName = environment.login.redirectCookie
            verifier = {
                jwkProvider.get(JWT.decode(it).keyId).idTokenVerifier(
                    environment.idporten.clientId,
                    environment.idporten.metadata.issuer
                )
            }
        }
    }

    val logLevel = Level.INFO
    log.info { "Installing log level: $logLevel" }
    install(CallLogging) {
        level = logLevel
        filter { call -> call.request.path().startsWith("/debugger") }
    }
    log.info { "Installing Api-Exception handler" }
    install(StatusPages) {
        exceptionHandler()
    }
    log.info { "Installing ObjectMapper" }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(Jackson.defaultMapper))
    }
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    log.info { "Installing routes" }
    install(Routing) {
        selfTest(readySelfTestCheck = { applicationStatus.initialized }, aLiveSelfTestCheck = { applicationStatus.running })
        login(environment)
        debuggerApi(environment)
    }
    applicationStatus.initialized = true
    log.info { "Application is up and running" }
}

object Jackson {
    val defaultMapper: ObjectMapper = jacksonObjectMapper()

    init {
        defaultMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    }
}
