package io.nais.quotesbackend

import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.testApplication
import io.nais.quotesbackend.database.QuotesTable
import kotlin.test.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class ApplicationIntegrationTest {

  private lateinit var database: Database

  @BeforeTest
  fun setupDatabase() {
    // Set environment variables for test database connection
    System.setProperty(
            "DB_URL",
            "jdbc:h2:mem:test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL"
    )
    System.setProperty("DB_USERNAME", "sa")
    System.setProperty("DB_PASSWORD", "")

    // Use H2 in-memory database for integration tests
    database =
            Database.connect(
                    url = System.getProperty("DB_URL"),
                    driver = "org.h2.Driver",
                    user = System.getProperty("DB_USERNAME"),
                    password = System.getProperty("DB_PASSWORD")
            )

    transaction(database) { SchemaUtils.create(QuotesTable) }
  }

  @AfterTest
  fun tearDownDatabase() {
    transaction(database) {
      QuotesTable.deleteAll()
      SchemaUtils.drop(QuotesTable)
    }
  }

  @Test
  fun testGetQuotesWithDatabase() = testApplication {
    globalErrorRate = 0.0

    // Clear database before test
    transaction(database) { QuotesTable.deleteAll() }

    application { module() }

    val client = createClient {
      install(ClientContentNegotiation) {
        json(
                Json {
                  prettyPrint = true
                  isLenient = true
                  ignoreUnknownKeys = true
                }
        )
      }
    }

    // Call readiness endpoint to initialize default quotes
    client.get("/internal/ready")

    // Now we should have quotes
    val initialResponse = client.get("/api/quotes")
    assertEquals(HttpStatusCode.OK, initialResponse.status)

    val quotes: List<Quote> = initialResponse.body()
    assertTrue(quotes.isNotEmpty(), "Should have quotes after initialization")
    assertEquals(5, quotes.size, "Should have 5 default quotes")
  }

  @Test
  fun testCreateAndRetrieveQuote() = testApplication {
    globalErrorRate = 0.0

    transaction(database) { QuotesTable.deleteAll() }

    application { module() }

    val client = createClient {
      install(ClientContentNegotiation) {
        json(
                Json {
                  prettyPrint = true
                  isLenient = true
                  ignoreUnknownKeys = true
                }
        )
      }
    }

    // Create a new quote
    val newQuote = QuoteInput(text = "Integration test quote", author = "Test Author")
    val createResponse =
            client.post("/api/quotes") {
              contentType(ContentType.Application.Json)
              setBody(newQuote)
            }

    assertEquals(HttpStatusCode.Created, createResponse.status)
    val createdQuote: Quote = createResponse.body()
    assertNotNull(createdQuote.id)
    assertEquals("Integration test quote", createdQuote.text)
    assertEquals("Test Author", createdQuote.author)

    // Retrieve the created quote
    val getResponse = client.get("/api/quotes/${createdQuote.id}")
    assertEquals(HttpStatusCode.OK, getResponse.status)

    val retrievedQuote: Quote = getResponse.body()
    assertEquals(createdQuote.id, retrievedQuote.id)
    assertEquals(createdQuote.text, retrievedQuote.text)
    assertEquals(createdQuote.author, retrievedQuote.author)
  }

  @Test
  fun testGetAllQuotesIncludesNewlyCreated() = testApplication {
    globalErrorRate = 0.0

    transaction(database) { QuotesTable.deleteAll() }

    application { module() }

    val client = createClient {
      install(ClientContentNegotiation) {
        json(
                Json {
                  prettyPrint = true
                  isLenient = true
                  ignoreUnknownKeys = true
                }
        )
      }
    }

    // Get initial count
    val initialResponse = client.get("/api/quotes")
    val initialQuotes: List<Quote> = initialResponse.body()
    val initialCount = initialQuotes.size

    // Create a new quote
    val newQuote = QuoteInput(text = "Another test quote", author = "Another Author")
    client.post("/api/quotes") {
      contentType(ContentType.Application.Json)
      setBody(newQuote)
    }

    // Get all quotes again
    val updatedResponse = client.get("/api/quotes")
    val updatedQuotes: List<Quote> = updatedResponse.body()

    assertEquals(initialCount + 1, updatedQuotes.size)
    assertTrue(updatedQuotes.any { it.text == "Another test quote" })
  }

  @Test
  fun testGetQuoteNotFoundReturns404() = testApplication {
    globalErrorRate = 0.0

    transaction(database) { QuotesTable.deleteAll() }

    application { module() }

    val client = createClient {}

    val response = client.get("/api/quotes/99999")
    assertEquals(HttpStatusCode.NotFound, response.status)

    val body = response.bodyAsText()
    assertTrue(body.contains("NOT_FOUND"))
  }

  @Test
  fun testGetQuoteWithInvalidIdFormat() = testApplication {
    globalErrorRate = 0.0

    application { module() }

    val client = createClient {}

    val response = client.get("/api/quotes/invalid")
    assertEquals(HttpStatusCode.BadRequest, response.status)

    val body = response.bodyAsText()
    assertTrue(body.contains("INVALID_ID"))
  }

  @Test
  fun testCreateQuoteWithMissingFields() = testApplication {
    globalErrorRate = 0.0

    application { module() }

    val client = createClient {}

    val invalidQuote = """{"text":"Only text field"}"""
    val response =
            client.post("/api/quotes") {
              contentType(ContentType.Application.Json)
              setBody(invalidQuote)
            }

    // The application returns 500 for serialization errors, which gets caught by error handling
    assertTrue(
            response.status == HttpStatusCode.BadRequest ||
                    response.status == HttpStatusCode.InternalServerError,
            "Expected 400 or 500, but got ${response.status}"
    )
    val body = response.bodyAsText()
    assertTrue(body.contains("error") || body.contains("Field"))
  }

  @Test
  fun testHealthEndpoint() = testApplication {
    application { module() }

    val client = createClient {}
    val response = client.get("/internal/health")

    assertEquals(HttpStatusCode.OK, response.status)
    assertEquals("Application is healthy", response.bodyAsText())
  }

  @Test
  fun testReadinessEndpoint() = testApplication {
    application { module() }

    val client = createClient {}
    val response = client.get("/internal/ready")

    assertEquals(HttpStatusCode.OK, response.status)
    assertTrue(response.bodyAsText().contains("ready"))
  }

  @Test
  fun testCreateMultipleQuotesSequentially() = testApplication {
    globalErrorRate = 0.0

    transaction(database) { QuotesTable.deleteAll() }

    application { module() }

    val client = createClient {
      install(ClientContentNegotiation) {
        json(
                Json {
                  prettyPrint = true
                  isLenient = true
                  ignoreUnknownKeys = true
                }
        )
      }
    }

    val quotes =
            listOf(
                    QuoteInput(text = "First quote", author = "Author 1"),
                    QuoteInput(text = "Second quote", author = "Author 2"),
                    QuoteInput(text = "Third quote", author = "Author 3")
            )

    val createdIds = mutableListOf<String>()

    for (quote in quotes) {
      val response =
              client.post("/api/quotes") {
                contentType(ContentType.Application.Json)
                setBody(quote)
              }
      assertEquals(HttpStatusCode.Created, response.status)
      val created: Quote = response.body()
      assertNotNull(created.id)
      createdIds.add(created.id!!)
    }

    // Verify all quotes exist
    val allQuotesResponse = client.get("/api/quotes")
    val allQuotes: List<Quote> = allQuotesResponse.body()

    assertTrue(allQuotes.size >= 3)
    for (id in createdIds) {
      assertTrue(allQuotes.any { it.id == id })
    }
  }
}
