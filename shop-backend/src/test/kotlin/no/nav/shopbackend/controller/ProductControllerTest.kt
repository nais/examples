package no.nav.shopbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.Optional
import no.nav.shopbackend.model.Product
import no.nav.shopbackend.model.Rating
import no.nav.shopbackend.repo.ProductRepository
import no.nav.shopbackend.repo.RatingRepository
import no.nav.shopbackend.service.SentimentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(ProductController::class)
class ProductControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @MockBean private lateinit var productRepo: ProductRepository
  @MockBean private lateinit var ratingRepo: RatingRepository
  @MockBean private lateinit var sentimentService: SentimentService

  @Test
  fun testGetProductById() {
    val product =
        Product(
            "Test Product",
            "A test product",
            Product.Category.TEE_SHIRT,
            13.37,
            emptyList(),
            mutableListOf<Rating>(),
            1L
        )

    product.ratings.add(Rating(5, "Great product", "positive", product))
    product.ratings.add(Rating(1, "Bad product", "negative", product))
    product.ratings.add(Rating(3, "Average product", "neutral", product))
    product.ratings.add(Rating(4, "Good product", "neutral", product))

    `when`(productRepo.findById(anyLong())).thenReturn(Optional.ofNullable(product))

    val result =
        mockMvc
            .perform(get("/api/products/1").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()

    val expected =
        """{"name":"Test Product","description":"A test product","category":"TEE_SHIRT","price":13.37,"images":[],"id":1,"averageRating":3.25}"""
    assertEquals(expected, result.response.contentAsString)
  }

  @Test
  fun testPostReview() {
    val product =
        Product(
            "Test Product",
            "A test product",
            Product.Category.TEE_SHIRT,
            13.37,
            emptyList(),
            mutableListOf<Rating>(),
            1L
        )

    `when`(productRepo.findById(anyLong())).thenReturn(Optional.ofNullable(product))
    `when`(sentimentService.getSentiment("Good product"))
        .thenReturn(SentimentService.SentimentResponse("neutral", 0.0f, 0.0f))

    val rating = Rating(4, "Good product", null, product)
    val json = ObjectMapper().writeValueAsString(rating)

    mockMvc
        .perform(
            post("/api/products/1/ratings").contentType(MediaType.APPLICATION_JSON).content(json)
        )
        .andExpect(status().isOk)

    verify(ratingRepo, times(1)).save(Rating(4, "Good product", "neutral", product))
  }
}
