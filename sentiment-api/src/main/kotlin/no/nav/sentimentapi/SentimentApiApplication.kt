package no.nav.sentimentapi

import com.google.cloud.language.v1.LanguageServiceClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
  @Bean
  fun languageServiceClient(): LanguageServiceClient {
    return LanguageServiceClient.create()
  }
}

@SpringBootApplication class SentimentApiApplication

fun main(args: Array<String>) {
  runApplication<SentimentApiApplication>(*args)
}
