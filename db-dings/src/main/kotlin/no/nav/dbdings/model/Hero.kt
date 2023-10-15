package no.nav.dbdings.model

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.jetbrains.annotations.NotNull

@Entity
data class Hero(
    @field:NotNull val firstName: String,
    val lastName: String?,
    @field:NotNull val species: Species,
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long = -1
) {
  enum class Species {
    HUMAN,
    MANDOLORIAN,
    MON_CALAMARI,
    OTHER,
    RODIAN,
    TOGRUTA,
    TRANDOSHAN,
    TWI_LEK,
    WOOKIEE,
    YODA_SPECIES,
    ZABRAK,
  }
  private constructor() : this("", "", Species.OTHER)
}
