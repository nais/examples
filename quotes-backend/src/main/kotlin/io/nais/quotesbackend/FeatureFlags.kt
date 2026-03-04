package io.nais.quotesbackend

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.util.UnleashConfig
import org.slf4j.LoggerFactory

object FeatureFlags {
    const val QUOTES_SUBMIT = "quotes.submit"
    const val QUOTES_ERRORS = "quotes.errors"

    private val log = LoggerFactory.getLogger(FeatureFlags::class.java)
    private var unleash: Unleash? = null

    fun init(appName: String = "quotes-backend") {
        if (unleash != null) return
        val apiUrl = System.getenv("UNLEASH_SERVER_API_URL")?.takeIf { it.isNotBlank() } ?: return
        val apiToken = System.getenv("UNLEASH_SERVER_API_TOKEN")?.takeIf { it.isNotBlank() } ?: return
        val environment = System.getenv("UNLEASH_SERVER_API_ENVIRONMENT") ?: "development"

        try {
            val config = UnleashConfig.builder()
                .appName(appName)
                .unleashAPI("$apiUrl/")
                .apiKey(apiToken)
                .environment(environment)
                .build()

            unleash = DefaultUnleash(config)
            log.info("Unleash initialized for environment: $environment")
        } catch (e: Exception) {
            log.warn("Failed to initialize Unleash, feature flags will fall back to default values: ${e.message}")
        }
    }

    fun isEnabled(flag: String, default: Boolean = true): Boolean {
        return unleash?.isEnabled(flag, default) ?: default
    }

    fun allFlags(): Map<String, Boolean> {
        return mapOf(
            QUOTES_SUBMIT to isEnabled(QUOTES_SUBMIT),
            QUOTES_ERRORS to isEnabled(QUOTES_ERRORS, default = false),
        )
    }

    fun shutdown() {
        unleash?.shutdown()
    }
}
