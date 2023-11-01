package no.nav.shopbackend.model

import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.jetbrains.annotations.NotNull

@Entity
data class Rating(
    @field:NotNull val stars: Int,
    val comment: String?,
    val sentiment: String?,
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "product_id") val product: Product?,
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long = -1
) {
  private constructor() : this(-1, "", "", null, -1L)
}
