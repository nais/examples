package no.nav.dbdings.repo

import no.nav.dbdings.model.Hero
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface HeroRepository : CrudRepository<Hero, Long> {
  fun findByLastName(lastName: String): Iterable<Hero>
}
