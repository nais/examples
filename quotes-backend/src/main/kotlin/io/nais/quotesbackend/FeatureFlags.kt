package io.nais.quotesbackend

import io.getunleash.DefaultUnleash
import io.getunleash.Unleash
import io.getunleash.event.UnleashReady
import io.getunleash.event.UnleashSubscriber
import io.getunleash.util.UnleashConfig
import io.getunleash.variant.Variant
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
                .unleashAPI("$apiUrl/api/")
                .apiKey(apiToken)
                .environment(environment)
                .subscriber(object : UnleashSubscriber {
                    override fun onReady(ready: UnleashReady) {
                        log.info("Unleash client ready")
                    }
                    override fun onError(unleashException: io.getunleash.UnleashException) {
                        log.warn("Unleash error: {}", unleashException.message)
                    }
                })
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

    fun getVariant(flag: String): Variant {
        return unleash?.getVariant(flag) ?: Variant.DISABLED_VARIANT
    }

    fun allFlags(): Map<String, Any> {
        return mapOf(
            QUOTES_SUBMIT to flagDetail(QUOTES_SUBMIT),
            QUOTES_ERRORS to flagDetail(QUOTES_ERRORS, default = false),
        )
    }

    private fun flagDetail(flag: String, default: Boolean = true): Map<String, Any> {
        val enabled = isEnabled(flag, default)
        val variant = getVariant(flag)
        val detail = mutableMapOf<String, Any>("enabled" to enabled)
        if (variant.name != "disabled") {
            val variantMap = mutableMapOf<String, Any>(
                "name" to variant.name,
                "enabled" to variant.isEnabled
            )
            variant.payload.ifPresent { payload ->
                variantMap["payload"] = mapOf("type" to payload.type, "value" to payload.value)
            }
            detail["variant"] = variantMap
        }
        return detail
    }

    fun shutdown() {
        unleash?.shutdown()
    }
}
