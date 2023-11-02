package no.nav.shopbackend.repo

import no.nav.shopbackend.model.Rating
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long> {
  fun findAll(pageable: Pageable): Iterable<Rating>

  @Query("SELECT r FROM Rating r WHERE r.product.id = :productId")
  fun findByProductId(@Param("productId") productId: Long): List<Rating>
}
