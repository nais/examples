package no.nav.shopbackend.controller

import no.nav.shopbackend.model.Product
import no.nav.shopbackend.model.Rating
import no.nav.shopbackend.repo.ProductRepository
import no.nav.shopbackend.repo.RatingRepository
import no.nav.shopbackend.service.SentimentService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path = ["/api/products", "/api/products/"], produces = ["application/json"])
class ProductController {
  @Autowired lateinit var productRepository: ProductRepository
  @Autowired lateinit var ratingRepository: RatingRepository
  @Autowired lateinit var sentimentService: SentimentService

  @GetMapping
  fun findAll(
      @RequestParam(defaultValue = "0") page: Int,
      @RequestParam(defaultValue = "10") size: Int
  ): Iterable<Product> {
    val pageable = PageRequest.of(page, size)
    return productRepository.findAll(pageable)
  }

  @PostMapping("/")
  @ResponseStatus(HttpStatus.CREATED)
  fun save(@RequestBody product: Product): Product {
    return productRepository.save(product)
  }

  @GetMapping("/{id:\\d+}")
  fun findById(@PathVariable id: Long): ResponseEntity<Product> {
    val product = productRepository.findById(id)
    return product.map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())
  }

  @PutMapping("/{id:\\d+}")
  fun update(@PathVariable id: Long, @RequestBody product: Product): ResponseEntity<Product> {
    val existingProduct = productRepository.findById(id)
    return existingProduct
        .map {
          val updatedProduct =
              it.copy(
                  name = product.name,
                  description = product.description,
                  category = product.category
              )
          ResponseEntity.ok(productRepository.save(updatedProduct))
        }
        .orElse(ResponseEntity.notFound().build())
  }

  @DeleteMapping("/{id:\\d+}")
  fun deleteById(@PathVariable id: Long): ResponseEntity<Void> {
    val product = productRepository.findById(id)
    return product
        .map {
          productRepository.deleteById(id)
          ResponseEntity<Void>(HttpStatus.NO_CONTENT)
        }
        .orElse(ResponseEntity.notFound().build())
  }

  @GetMapping("/{id:\\d+}/ratings")
  fun findRatingsByProductId(@PathVariable id: Long): ResponseEntity<List<Rating>> {
    val ratings = ratingRepository.findByProductId(id)
    return if (ratings.isNotEmpty()) {
      ResponseEntity.ok(ratings)
    } else {
      ResponseEntity.notFound().build()
    }
  }

  @PostMapping("/{id:\\d+}/ratings")
  @ResponseStatus(HttpStatus.CREATED)
  fun saveRating(@PathVariable id: Long, @RequestBody rating: Rating): ResponseEntity<Rating> {
    val product = productRepository.findById(id)
    return product
        .map {
          if (rating.comment != null) {
            var sentiment = "unknown"

            try {
              sentiment = sentimentService.getSentiment(rating.comment).sentiment
            } catch (e: Exception) {
              println("Failed to get sentiment for comment: ${rating.comment} with error: $e")
            }

            ResponseEntity.ok(
                ratingRepository.save(rating.copy(product = it, sentiment = sentiment))
            )
          } else {
            ResponseEntity.ok(ratingRepository.save(rating.copy(product = it)))
          }
        }
        .orElse(ResponseEntity.notFound().build())
  }

  @RequestMapping("/generate")
  fun save(): String {

    val products =
        arrayOf(
            mapOf(
                "name" to "A T-Shirt",
                "description" to "A test product",
                "category" to "TEE_SHIRT",
            ),
            mapOf(
                "name" to "A Hoodie",
                "description" to "A test product",
                "category" to "HOODIE",
            ),
            mapOf(
                "name" to "A Cap",
                "description" to "A test product",
                "category" to "CAP",
            ),
            mapOf(
                "name" to "Light saber",
                "description" to "Jedi weapon of choice",
                "category" to "OTHER",
            ),
        )

    for (it in products) {
      println(it)
      val randomPrice = (0..100).random()
      val product =
          Product(
              it["name"] as String,
              it["description"] as String,
              Product.Category.valueOf(it["category"] as String),
              randomPrice.toDouble(),
              emptyList(),
              mutableListOf<Rating>(),
          )

      productRepository.save(product)
      ratingRepository.save(Rating((3..5).random(), "Great product", "positive", product))
      ratingRepository.save(Rating((1..3).random(), "Bad product", "negative", product))
      ratingRepository.save(Rating((1..5).random(), "Average product", "neutral", product))
      ratingRepository.save(Rating((3..5).random(), "Good product", "neutral", product))
      ratingRepository.save(Rating((1..3).random(), "Bad product", "negative", product))
    }

    return "Done"
  }
}
