package no.nav.dbdings.controller

import java.util.Optional
import no.nav.dbdings.model.Product
import no.nav.dbdings.model.Rating
import no.nav.dbdings.repo.ProductRepository
import no.nav.dbdings.repo.RatingRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ExtendWith(SpringExtension::class)
@WebMvcTest(ProductController::class)
class ProductControllerTest {
  @Autowired private lateinit var mockMvc: MockMvc

  @MockBean private lateinit var productRepo: ProductRepository
  @MockBean private lateinit var ratingRepo: RatingRepository

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

    product.ratings.add(Rating(5, "Great product", product))
    product.ratings.add(Rating(1, "Bad product", product))
    product.ratings.add(Rating(3, "Average product", product))
    product.ratings.add(Rating(4, "Good product", product))

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
}
