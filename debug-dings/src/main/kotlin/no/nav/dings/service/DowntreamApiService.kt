package no.nav.dings.service

import no.nav.dings.config.Environment

class DowntreamApiService(
    private val config: Environment.TokenX
) {

    var isOnPrem: Boolean = false

    fun audience() =
        when {
            isOnPrem -> {
                config.onpremAudience
            }
            else -> {
                config.gcpAudience
            }
        }
}
