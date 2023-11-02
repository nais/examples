package no.nav.shopbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.shopbackend.model.Product
import no.nav.shopbackend.model.Rating
import no.nav.shopbackend.repo.ProductRepository
import no.nav.shopbackend.repo.RatingRepository
import no.nav.shopbackend.service.SentimentService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
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

  @MockBean private lateinit var sentimentService: SentimentService

  @MockBean private lateinit var productRepository: ProductRepository

  @MockBean private lateinit var ratingRepository: RatingRepository

  private lateinit var product: Product

  @BeforeEach
  fun setUp() {
    product =
        Product(
            "Test Product",
            "A test product",
            Product.Category.TEE_SHIRT,
            13.37,
            emptyList(),
            mutableListOf<Rating>(),
            1L
        )

    val ratings =
        mutableListOf<Rating>(
            Rating(5, "Great product", "positive", product, 1L),
            Rating(1, "Bad product", "negative", product, 2L),
            Rating(3, "Average product", "neutral", product, 3L),
            Rating(4, "Good product", "neutral", product, 4L)
        )

    for (rating in ratings) {
      product.ratings.add(rating)
    }

    `when`(productRepository.findAll(PageRequest.of(0, 10))).thenReturn(PageImpl(listOf(product)))
    `when`(productRepository.findById(1L)).thenReturn(java.util.Optional.of(product))
    `when`(ratingRepository.findByProductId(1L)).thenReturn(ratings)
  }

  @Test
  fun testGetProducts() {
    val result =
        mockMvc
            .perform(get("/api/products").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()

    val expected =
        """{"content":[{"name":"Test Product","description":"A test product","category":"TEE_SHIRT","price":13.37,"images":[],"id":1,"averageRating":3.25}],"pageable":"INSTANCE","totalPages":1,"totalElements":1,"last":true,"numberOfElements":1,"first":true,"size":1,"number":0,"sort":{"sorted":false,"unsorted":true,"empty":true},"empty":false}"""
    assertEquals(expected, result.response.contentAsString)
  }

  @Test
  fun testGetProductById() {
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
  fun testPostReviewForProduct() {
    `when`(sentimentService.getSentiment("Super awesome product"))
        .thenReturn(SentimentService.SentimentResponse("positive", 4.0f, 0.8f))

    val rating = Rating(5, "Super awesome product", null, product)
    val json = ObjectMapper().writeValueAsString(rating)

    val result =
        mockMvc
            .perform(
                post("/api/products/1/ratings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
            )
            .andExpect(status().isOk)
            .andReturn()

    // inspect what ratingRepository.save was called with
    // Capture the argument passed to ratingRepository.save
    val captor = ArgumentCaptor.forClass(Rating::class.java)
    verify(ratingRepository).save(captor.capture())

    // Assert that the captured argument has the expected values
    val savedRating = captor.value
    assertEquals(5, savedRating.stars)
    assertEquals("Super awesome product", savedRating.comment)
    assertEquals("positive", savedRating.sentiment)
  }

  @Test
  fun testGetReviewsForProduct() {
    val result =
        mockMvc
            .perform(get("/api/products/1/ratings").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()

    val expected =
        """[{"stars":5,"comment":"Great product","sentiment":"positive","id":1},{"stars":1,"comment":"Bad product","sentiment":"negative","id":2},{"stars":3,"comment":"Average product","sentiment":"neutral","id":3},{"stars":4,"comment":"Good product","sentiment":"neutral","id":4}]"""
    assertEquals(expected, result.response.contentAsString)
  }
}
