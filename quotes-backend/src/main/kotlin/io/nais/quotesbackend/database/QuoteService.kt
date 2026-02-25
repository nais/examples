package io.nais.quotesbackend.database

import io.nais.quotesbackend.Quote
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
class QuoteService(private val database: Database) {
  private suspend fun <T> dbQuery(block: suspend () -> T): T =
          newSuspendedTransaction(Dispatchers.IO, database) { block() }

  suspend fun getAllQuotes(): List<Quote> = dbQuery {
    QuotesTable.selectAll().map {
      Quote(
              id = it[QuotesTable.id].toString(),
              text = it[QuotesTable.text],
              author = it[QuotesTable.author]
      )
    }
  }

  suspend fun getQuoteById(id: Int): Quote? = dbQuery {
    QuotesTable.selectAll()
            .where { QuotesTable.id eq id }
            .map {
              Quote(
                      id = it[QuotesTable.id].toString(),
                      text = it[QuotesTable.text],
                      author = it[QuotesTable.author]
              )
            }
            .singleOrNull()
  }

  suspend fun createQuote(text: String, author: String): Quote = dbQuery {
    val insertStatement =
            QuotesTable.insert {
              it[QuotesTable.text] = text
              it[QuotesTable.author] = author
            }
    val id = insertStatement[QuotesTable.id]
    Quote(id = id.toString(), text = text, author = author)
  }

  suspend fun updateQuote(id: Int, text: String, author: String): Quote? = dbQuery {
    val updated =
            QuotesTable.update({ QuotesTable.id eq id }) {
              it[QuotesTable.text] = text
              it[QuotesTable.author] = author
            }
    if (updated > 0) {
      QuotesTable.selectAll()
              .where { QuotesTable.id eq id }
              .map {
                Quote(
                        id = it[QuotesTable.id].toString(),
                        text = it[QuotesTable.text],
                        author = it[QuotesTable.author]
                )
              }
              .singleOrNull()
    } else {
      null
    }
  }

  suspend fun deleteQuote(id: Int): Boolean = dbQuery {
    val deleted = QuotesTable.deleteWhere { QuotesTable.id eq id }
    deleted > 0
  }

  suspend fun searchQuotes(query: String): List<Quote> = dbQuery {
    val escaped = query.replace("%", "\\%").replace("_", "\\_")
    QuotesTable.selectAll()
            .where { (QuotesTable.text like "%$escaped%") or (QuotesTable.author like "%$escaped%") }
            .map {
              Quote(
                      id = it[QuotesTable.id].toString(),
                      text = it[QuotesTable.text],
                      author = it[QuotesTable.author]
              )
            }
  }

  suspend fun getQuoteCount(): Long = dbQuery { QuotesTable.selectAll().count() }

  suspend fun initializeDefaultQuotes() = dbQuery {
    if (QuotesTable.selectAll().count() == 0L) {
      val defaultQuotes =
              listOf(
                      "Deploy with confidence—let Nais handle the platform, you focus on the code." to
                              "Nais Team",
                      "Kubernetes is complex, but Nais makes it simple for developers." to
                              "Platform Engineer",
                      "With Nais, continuous delivery is not just a dream—it's the default." to
                              "DevOps Enthusiast",
                      "Secure by default—Nais integrates best practices into every deployment." to
                              "Security Advocate",
                      "From Helm charts to GitHub Actions, Nais brings it all together." to
                              "Cloud Native Developer"
              )

      defaultQuotes.forEach { (text, author) ->
        QuotesTable.insert {
          it[QuotesTable.text] = text
          it[QuotesTable.author] = author
        }
      }
    }
  }
}
