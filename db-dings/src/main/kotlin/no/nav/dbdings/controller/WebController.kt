package no.nav.dbdings.controller

import no.nav.dbdings.model.Hero
import no.nav.dbdings.repo.HeroRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/heroes")
class WebController {
  @Autowired lateinit var repository: HeroRepository

  @RequestMapping("/") fun findAll() = repository.findAll()

  @PostMapping("/")
  @ResponseStatus(HttpStatus.CREATED)
  fun save(@RequestBody hero: Hero): Hero {
    return repository.save(hero)
  }
  // curl -X POST -H "Content-Type: application/json" -d '{"firstName": "Darth", "lastName": "Maul",
  // "species": "Zabrak"}' http://localhost:8080/api/heroes/

  @RequestMapping("/{id:\\d+}")
  fun findById(@PathVariable id: Long): ResponseEntity<Hero> {
    val hero = repository.findById(id)
    return hero.map { ResponseEntity.ok(it) }.orElse(ResponseEntity.notFound().build())
  }

  @RequestMapping("/generate")
  fun save(): String {
    repository.save(Hero("Luke", "Skywalker", Hero.Species.HUMAN))
    repository.save(Hero("Leia", "Organa", Hero.Species.HUMAN))
    repository.save(Hero("Han", "Solo", Hero.Species.HUMAN))
    repository.save(Hero("Chewbacca", "", Hero.Species.WOOKIEE))
    repository.save(Hero("Lando", "Calrissian", Hero.Species.HUMAN))
    repository.save(Hero("Yoda", "", Hero.Species.YODA_SPECIES))
    repository.save(Hero("Obi-Wan", "Kenobi", Hero.Species.HUMAN))
    repository.save(Hero("Anakin", "Skywalker", Hero.Species.HUMAN))
    repository.save(Hero("Darth", "Vader", Hero.Species.HUMAN))
    repository.save(Hero("Palpatine", "", Hero.Species.HUMAN))

    return "Done"
  }
}
