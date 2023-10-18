package no.nav.dbdings.repo

import no.nav.dbdings.model.Product
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : CrudRepository<Product, Long> {
  fun findAll(pageable: Pageable): Iterable<Product>
  // fun findByCategory(category: Product.Category, pageable: Pageable): Iterable<Product>
}
