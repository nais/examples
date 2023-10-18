package no.nav.dbdings.repo

import no.nav.dbdings.model.Rating
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : CrudRepository<Rating, Long> {
  fun findAll(pageable: Pageable): Iterable<Rating>
}
