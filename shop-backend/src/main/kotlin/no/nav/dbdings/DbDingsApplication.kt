package no.nav.dbdings

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication class DbDingsApplication

fun main(args: Array<String>) {
  runApplication<DbDingsApplication>(*args)
}
