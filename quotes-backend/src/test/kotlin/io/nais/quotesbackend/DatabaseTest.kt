package io.nais.quotesbackend

import io.nais.quotesbackend.database.QuoteService
import io.nais.quotesbackend.database.QuotesTable
import kotlin.test.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseTest {

  private lateinit var database: Database
  private lateinit var quoteService: QuoteService

  @BeforeTest
  fun setup() {
    // Use in-memory H2 database for testing
    database =
            Database.connect(
                    "jdbc:h2:mem:test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;",
                    driver = "org.h2.Driver"
            )

    transaction(database) { SchemaUtils.create(QuotesTable) }

    quoteService = QuoteService()
  }

  @AfterTest
  fun tearDown() {
    transaction(database) {
      QuotesTable.deleteAll()
      SchemaUtils.drop(QuotesTable)
    }
  }

  @Test
  fun testCreateQuote() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val quote = quoteService.createQuote("Test quote", "Test author")

    assertNotNull(quote.id)
    assertEquals("Test quote", quote.text)
    assertEquals("Test author", quote.author)
  }

  @Test
  fun testGetAllQuotes() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    // Create test quotes
    quoteService.createQuote("Quote 1", "Author 1")
    quoteService.createQuote("Quote 2", "Author 2")
    quoteService.createQuote("Quote 3", "Author 3")

    val quotes = quoteService.getAllQuotes()

    assertEquals(3, quotes.size)
  }

  @Test
  fun testGetQuoteById() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val createdQuote = quoteService.createQuote("Specific quote", "Specific author")
    val quoteId = createdQuote.id?.toInt() ?: fail("Quote ID should not be null")

    val retrievedQuote = quoteService.getQuoteById(quoteId)

    assertNotNull(retrievedQuote)
    assertEquals(createdQuote.id, retrievedQuote.id)
    assertEquals("Specific quote", retrievedQuote.text)
    assertEquals("Specific author", retrievedQuote.author)
  }

  @Test
  fun testGetQuoteByIdNotFound() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val retrievedQuote = quoteService.getQuoteById(99999)

    assertNull(retrievedQuote)
  }

  @Test
  fun testInitializeDefaultQuotes() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    // Initialize default quotes
    quoteService.initializeDefaultQuotes()

    val quotes = quoteService.getAllQuotes()
    assertEquals(5, quotes.size, "Should have 5 default quotes")

    // Verify one of the default quotes
    val firstQuote = quotes.first()
    assertTrue(firstQuote.text.isNotEmpty())
    assertTrue(firstQuote.author.isNotEmpty())
  }

  @Test
  fun testInitializeDefaultQuotesIdempotent() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    // Initialize default quotes twice
    quoteService.initializeDefaultQuotes()
    quoteService.initializeDefaultQuotes()

    val quotes = quoteService.getAllQuotes()
    assertEquals(5, quotes.size, "Should still have only 5 quotes after second initialization")
  }

  @Test
  fun testCreateMultipleQuotes() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val quote1 = quoteService.createQuote("First quote", "Author 1")
    val quote2 = quoteService.createQuote("Second quote", "Author 2")
    val quote3 = quoteService.createQuote("Third quote", "Author 3")

    assertNotNull(quote1.id)
    assertNotNull(quote2.id)
    assertNotNull(quote3.id)

    // IDs should be different
    assertNotEquals(quote1.id, quote2.id)
    assertNotEquals(quote2.id, quote3.id)
    assertNotEquals(quote1.id, quote3.id)

    val allQuotes = quoteService.getAllQuotes()
    assertEquals(3, allQuotes.size)
  }

  @Test
  fun testQuoteTextAndAuthorPreservation() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val longText = "A".repeat(500)
    val longAuthor = "B".repeat(200)

    val quote = quoteService.createQuote(longText, longAuthor)
    val quoteId = quote.id?.toInt() ?: fail("Quote ID should not be null")

    val retrievedQuote = quoteService.getQuoteById(quoteId)

    assertNotNull(retrievedQuote)
    assertEquals(longText, retrievedQuote.text)
    assertEquals(longAuthor, retrievedQuote.author)
  }

  @Test
  fun testEmptyDatabase() = runBlocking {
    // Clear any existing data
    transaction(database) { QuotesTable.deleteAll() }

    val quotes = quoteService.getAllQuotes()

    assertTrue(quotes.isEmpty())
  }

  @Test
  fun testUpdateQuote() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    val createdQuote = quoteService.createQuote("Original text", "Original author")
    val quoteId = createdQuote.id?.toInt() ?: fail("Quote ID should not be null")

    val updatedQuote = quoteService.updateQuote(quoteId, "Updated text", "Updated author")

    assertNotNull(updatedQuote)
    assertEquals(createdQuote.id, updatedQuote?.id)
    assertEquals("Updated text", updatedQuote?.text)
    assertEquals("Updated author", updatedQuote?.author)
  }

  @Test
  fun testUpdateNonExistentQuote() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    val result = quoteService.updateQuote(99999, "Text", "Author")
    assertNull(result)
  }

  @Test
  fun testDeleteQuote() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    val createdQuote = quoteService.createQuote("To be deleted", "Author")
    val quoteId = createdQuote.id?.toInt() ?: fail("Quote ID should not be null")

    val deleted = quoteService.deleteQuote(quoteId)
    assertTrue(deleted)

    val retrievedQuote = quoteService.getQuoteById(quoteId)
    assertNull(retrievedQuote)
  }

  @Test
  fun testDeleteNonExistentQuote() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    val deleted = quoteService.deleteQuote(99999)
    assertFalse(deleted)
  }

  @Test
  fun testSearchQuotes() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    quoteService.createQuote("Kotlin is awesome", "Developer")
    quoteService.createQuote("Java is great", "Engineer")
    quoteService.createQuote("Go is fast", "Gopher")

    val kotlinResults = quoteService.searchQuotes("Kotlin")
    assertEquals(1, kotlinResults.size)
    assertEquals("Kotlin is awesome", kotlinResults.first().text)

    val developerResults = quoteService.searchQuotes("Developer")
    assertEquals(1, developerResults.size)

    val noResults = quoteService.searchQuotes("Python")
    assertTrue(noResults.isEmpty())
  }

  @Test
  fun testGetQuoteCount() = runBlocking {
    transaction(database) { QuotesTable.deleteAll() }

    var count = quoteService.getQuoteCount()
    assertEquals(0L, count)

    quoteService.createQuote("Quote 1", "Author 1")
    quoteService.createQuote("Quote 2", "Author 2")
    quoteService.createQuote("Quote 3", "Author 3")

    count = quoteService.getQuoteCount()
    assertEquals(3L, count)
  }
}
