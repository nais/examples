package no.nav.shopbackend.service

import com.fasterxml.jackson.databind.ObjectMapper
import java.net.HttpURLConnection
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SentimentServiceTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var sentimentService: SentimentService

  @BeforeEach
  fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    sentimentService =
        SentimentService(
            httpClient = OkHttpClient(),
            objectMapper = ObjectMapper(),
            sentimentApiHost = mockWebServer.url("/api/sentiment").toString()
        )
  }

  @AfterEach
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `test getSentiment with positive text`() {
    val text = "I love this product!"
    val expectedSentiment = "positive"

    mockWebServer.enqueue(
        MockResponse().setBody("""{"score":0.9,"magnitude":0.9,"sentiment":"positive"}""")
    )

    val actualSentiment = sentimentService.getSentiment(text)

    assertEquals(expectedSentiment, actualSentiment.sentiment)
  }

  @Test
  fun `test getSentiment with negative text`() {
    val text = "I hate this product!"
    val expectedSentiment = "negative"

    mockWebServer.enqueue(
        MockResponse().setBody("""{"score":-0.9,"magnitude":0.9,"sentiment":"negative"}""")
    )

    val actualSentiment = sentimentService.getSentiment(text)

    assertEquals(expectedSentiment, actualSentiment.sentiment)
  }

  @Test
  fun `test getSentiment with 500 error from sentiment API`() {
    val text = "I hate this product!"
    mockWebServer.enqueue(MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR))

    assertThrows(RuntimeException::class.java) { sentimentService.getSentiment(text) }
  }
}
