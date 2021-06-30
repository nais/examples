package no.nav.security.examples

import com.fasterxml.jackson.annotation.JsonProperty
import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EmptyConfiguration
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking

private val config = systemProperties() overriding
    EnvironmentVariables() overriding
    ConfigurationProperties.fromOptionalResource("application-local.secrets.properties") overriding
    ConfigurationProperties.fromResource("application.properties")

data class Configuration(
    val application: Application = Application(),
    val azureAd: AzureAd = AzureAd()
) {
    data class Application(
        val port: Int = config[Key("application.port", intType)],
        val name: String = config[Key("application.name", stringType)]
    )

    data class AzureAd(
        val clientId: String = config[Key("azure.app.client.id", stringType)],
        val wellKnownConfigurationUrl: String = config[Key("azure.app.well.known.url", stringType)],
        val openIdConfiguration: AzureAdOpenIdConfiguration = runBlocking {
            httpClient.get(wellKnownConfigurationUrl)
        }
    )
}

data class AzureAdOpenIdConfiguration(
    @JsonProperty("jwks_uri")
    val jwksUri: String,
    @JsonProperty("issuer")
    val issuer: String,
    @JsonProperty("token_endpoint")
    val tokenEndpoint: String,
    @JsonProperty("authorization_endpoint")
    val authorizationEndpoint: String
)

private fun ConfigurationProperties.Companion.fromOptionalResource(resourceName: String): Configuration =
    ClassLoader.getSystemClassLoader().getResource(resourceName)?.let {
        fromResource(resourceName)
    } ?: EmptyConfiguration
