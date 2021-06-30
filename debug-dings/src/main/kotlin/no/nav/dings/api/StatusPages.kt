package no.nav.dings.api

import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import no.nav.dings.HttpException
import javax.security.sasl.AuthenticationException

fun StatusPages.Configuration.exceptionHandler() {
    exception<HttpException> { cause ->
        call.respond(cause.httpStatusCode, "message: ${cause.message}")
    }
    exception<InternalError> { cause ->
        val status = HttpStatusCode.InternalServerError
        call.respond(
            status,
            HttpErrorResponse(
                code = status,
                url = "",
                cause = cause.toString(),
                message = "The request was either invalid or lacked required parameters"
            )
        )
    }
    exception<AuthenticationException> { cause ->
        val status = HttpStatusCode.Unauthorized
        call.respond(
            status,
            HttpErrorResponse(
                code = status,
                url = "",
                cause = cause.toString(),
                message = "The request was either invalid or lacked required parameters"
            )
        )
    }
}

data class HttpErrorResponse(
    val url: String,
    val message: String? = null,
    val cause: String? = null,
    val code: HttpStatusCode = HttpStatusCode.InternalServerError
)
