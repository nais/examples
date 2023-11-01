package no.nav.sentimentapi.controller

import com.google.cloud.language.v1.Document
import com.google.cloud.language.v1.Document.Type.PLAIN_TEXT
import com.google.cloud.language.v1.LanguageServiceClient
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SentimentApiController(@Autowired val languageServiceClient: LanguageServiceClient) {

  @PostMapping("/api/sentiment")
  fun analyzeSentiment(@RequestBody text: String): Map<String, Any> {
    val cleanText = Jsoup.clean(text, Safelist.none())
    val doc = Document.newBuilder().setContent(cleanText).setType(PLAIN_TEXT).build()
    val sentiment = languageServiceClient.analyzeSentiment(doc).documentSentiment

    val humanReadableSentiment =
        when {
          sentiment.score > 0.25 && sentiment.magnitude >= 2.0 -> "very positive"
          sentiment.score > 0.25 -> "positive"
          sentiment.score < -0.25 && sentiment.magnitude >= 2.0 -> "very negative"
          sentiment.score < -0.25 -> "negative"
          else -> "neutral"
        }

    return mapOf(
        "score" to sentiment.score,
        "magnitude" to sentiment.magnitude,
        "sentiment" to humanReadableSentiment
    )
  }
  /*
  curl -X POST \
    http://localhost:8080/api/sentiment \
    -H 'Content-Type: application/json' \
    -d '{"text": "I love this product!"}'
  */
}
