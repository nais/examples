package no.nav.shopbackend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class ShopBackendApplication

fun main(args: Array<String>) {
  runApplication<ShopBackendApplication>(*args)
}
