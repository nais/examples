package no.nav.sentimentapi.controller

import com.google.api.gax.core.GoogleCredentialsProvider
import com.google.cloud.language.v1.AnalyzeSentimentResponse
import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.LanguageServiceClient
import com.google.cloud.language.v1.Sentiment
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

// @Bean
// @Primary
// fun languageServiceClient(): LanguageServiceClient {
//  val languageServiceSettings: LanguageServiceSettings =
//      LanguageServiceSettings.newBuilder().setCredentialsProvider(NoCredentialsProvider()).build()
//  return LanguageServiceClient.create(languageServiceSettings)
// }

@WebMvcTest(SentimentApiController::class)
@ExtendWith(SpringExtension::class)
class SentimentApiControllerTest(@Autowired private val mockMvc: MockMvc) {
  // @MockBean val languageServiceClient = mock(LanguageServiceClient::class.java)
  @MockBean lateinit var languageServiceClient: LanguageServiceClient
  @MockBean lateinit var googleCredentialsProvider: GoogleCredentialsProvider

  @Test
  fun `analyzeSentiment should return sentiment score and magnitude`() {
    val text = "I love this product!"
    val sentiment = Sentiment.newBuilder().setScore(0.8f).setMagnitude(0.8f).build()
    val response = AnalyzeSentimentResponse.newBuilder().setDocumentSentiment(sentiment).build()

    `when`(googleCredentialsProvider.getCredentials()).thenReturn(null)

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
