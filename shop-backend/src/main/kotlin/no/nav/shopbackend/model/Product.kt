package no.nav.shopbackend.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import org.jetbrains.annotations.NotNull

@Entity
@JsonIgnoreProperties("ratings")
data class Product(
    @field:NotNull val name: String,
    @field:NotNull val description: String,
    @field:NotNull val category: Category,
    @field:NotNull val price: Double,
    val images: List<String> = emptyList(),
    @OneToMany(mappedBy = "product", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ratings: MutableList<Rating> = mutableListOf(),
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long = -1
) {
  enum class Category {
    TEE_SHIRT,
    HOODIE,
    SCARF,
    CAP,
    OTHER,
  }

  fun getAverageRating(): Double {
    if (ratings.isEmpty()) {
      return 0.0
    }
    val sum = ratings.sumOf { it.stars }
    return sum.toDouble() / ratings.size
  }

  private constructor() :
      this("", "", Category.OTHER, 0.0, emptyList(), mutableListOf<Rating>(), -1)
}
