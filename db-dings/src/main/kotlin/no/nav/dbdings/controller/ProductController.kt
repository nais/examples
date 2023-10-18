package no.nav.dbdings.controller

import no.nav.dbdings.model.Product
import no.nav.dbdings.model.Rating
import no.nav.dbdings.repo.ProductRepository
import no.nav.dbdings.repo.RatingRepository
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
  fun findRatingsByProductId(@PathVariable id: Long): ResponseEntity<MutableList<Rating>> {
    val product = productRepository.findById(id)
    return product.map { ResponseEntity.ok(it.ratings) }.orElse(ResponseEntity.notFound().build())
  }

  @PostMapping("/{id:\\d+}/ratings")
  @ResponseStatus(HttpStatus.CREATED)
  fun saveRating(@PathVariable id: Long, @RequestBody rating: Rating): ResponseEntity<Rating> {
    val product = productRepository.findById(id)
    return product
        .map {
          val rating = rating.copy(product = it)
          ResponseEntity.ok(ratingRepository.save(rating))
        }
        .orElse(ResponseEntity.notFound().build())
  }

  @RequestMapping("/generate")
  fun save(): String {

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

    productRepository.save(product)
    ratingRepository.save(Rating(5, "Great product", product))
    ratingRepository.save(Rating(1, "Bad product", product))
    ratingRepository.save(Rating(3, "Average product", product))
    ratingRepository.save(Rating(4, "Good product", product))

    return "Done"
  }
}
