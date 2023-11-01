package no.nav.sentimentapi.controller

import com.google.api.gax.core.NoCredentialsProvider
import com.google.cloud.language.v1.AnalyzeSentimentResponse
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.language.v1.Sentiment
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SentimentApiController::class)
class SentimentApiControllerTest(@Autowired private val mockMvc: MockMvc) {

  @MockBean private lateinit var languageServiceClient: LanguageServiceClient
  @MockBean private lateinit var credentialsProvider: NoCredentialsProvider

  @Test
  fun `analyzeSentiment should return sentiment score and magnitude`() {
    val text = "I love this product!"
    val sentiment = Sentiment.newBuilder().setScore(0.8f).setMagnitude(0.8f).build()
    val response = AnalyzeSentimentResponse.newBuilder().setDocumentSentiment(sentiment).build()

    `when`(languageServiceClient.analyzeSentiment(any<Document>(Document::class.java)))
        .thenReturn(response)

    mockMvc
        .perform(
            post("/api/sentiment").contentType("application/json").content("{\"text\": \"$text\"}")
        )
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.score").value(sentiment.score))
        .andExpect(jsonPath("$.magnitude").value(sentiment.magnitude))
        .andExpect(jsonPath("$.sentiment").value("positive"))
  }
}
