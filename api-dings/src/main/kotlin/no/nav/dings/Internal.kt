package no.nav.dings

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

const val IS_ALIVE = "/isAlive"
const val IS_READY = "/isReady"

inline fun Routing.selfTest(
    crossinline readySelfTestCheck: () -> Boolean,
    crossinline aLiveSelfTestCheck: () -> Boolean = { true }
) {
    get(IS_ALIVE) {
        if (aLiveSelfTestCheck()) {
            call.respondText("I'm alive")
        } else {
            call.respondText("I'm dead!", status = HttpStatusCode.InternalServerError)
        }
    }
    get(IS_READY) {
        if (readySelfTestCheck()) {
            call.respondText("I'm ready")
        } else {
            call.respondText("I'm dead!", status = HttpStatusCode.InternalServerError)
        }
    }
}
