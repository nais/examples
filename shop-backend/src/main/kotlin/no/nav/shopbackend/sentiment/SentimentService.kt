package no.nav.shopbackend.service

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class SentimentService(
    private val httpClient: OkHttpClient,
    private val objectMapper: ObjectMapper,
    @Value("\${sentiment-api.host:http://localhost:8081}") private val sentimentApiHost: String
) {
  private val sentimentApiUrl = "$sentimentApiHost/api/sentiment"

  fun getSentiment(text: String): SentimentResponse {
    val requestBody =
        objectMapper.writeValueAsString(SentimentRequest(text)).toRequestBody(JSON_MEDIA_TYPE)
    val request = Request.Builder().url(sentimentApiUrl).post(requestBody).build()

    val response = httpClient.newCall(request).execute()
    if (!response.isSuccessful) {
      throw RuntimeException("Failed to get sentiment for text: $text")
    }

    return objectMapper.readValue(response.body!!.string(), SentimentResponse::class.java)
  }

  data class SentimentRequest(@JsonProperty("text") val text: String)
  data class SentimentResponse(
      @JsonProperty("sentiment") val sentiment: String,
      @JsonProperty("score") val score: Float,
      @JsonProperty("magnitude") val magnitude: Float
  )

  companion object {
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
  }
}
