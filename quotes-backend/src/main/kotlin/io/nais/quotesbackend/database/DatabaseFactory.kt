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
              // NAIS provides DB_JDBC_URL which is ready for JDBC (jdbc:postgresql://...)
              // For local dev, fall back to DB_URL or default to H2
              val jdbcUrl =
                      System.getProperty("DB_JDBC_URL")
                              ?: System.getenv("DB_JDBC_URL") ?: System.getProperty("DB_URL")
                                      ?: System.getenv("DB_URL")
                                      ?: "jdbc:h2:mem:quotes;DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
              val dbUser = System.getProperty("DB_USERNAME") ?: System.getenv("DB_USERNAME") ?: "sa"
              val dbPassword =
                      System.getProperty("DB_PASSWORD") ?: System.getenv("DB_PASSWORD") ?: ""

              // Auto-detect driver based on URL
              driverClassName =
                      when {
                        jdbcUrl.contains("h2") -> "org.h2.Driver"
                        else -> "org.postgresql.Driver"
                      }

              this.jdbcUrl = jdbcUrl
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
