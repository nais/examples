package io.nais.quotesbackend.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
  fun init() {
    val database = Database.connect(hikari())
    transaction(database) { SchemaUtils.create(QuotesTable) }
  }

  private fun hikari(): HikariDataSource {
    val config =
            HikariConfig().apply {
              // Support both environment variables and system properties (for testing)
              val dbUrl = System.getProperty("DB_URL") ?: System.getenv("DB_URL")
              val dbUser = System.getProperty("DB_USERNAME") ?: System.getenv("DB_USERNAME")
              val dbPassword =
                      System.getProperty("DB_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: ""

              // Auto-detect driver based on URL
              driverClassName =
                      when {
                        dbUrl.contains("h2") -> "org.h2.Driver"
                        else -> "org.postgresql.Driver"
                      }

              jdbcUrl = dbUrl
              username = dbUser
              password = dbPassword
              maximumPoolSize = 3
              isAutoCommit = false
              transactionIsolation = "TRANSACTION_REPEATABLE_READ"
              validate()
            }
    return HikariDataSource(config)
  }
}
