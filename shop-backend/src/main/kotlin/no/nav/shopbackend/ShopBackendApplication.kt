package no.nav.shopbackend

import okhttp3.OkHttpClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
  @Bean
  fun okHttpClient(): OkHttpClient {
    return OkHttpClient()
  }
}

@SpringBootApplication class ShopBackendApplication

fun main(args: Array<String>) {
  runApplication<ShopBackendApplication>(*args)
}
