package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import jakarta.persistence.Embeddable
import jakarta.persistence.Table

@Embeddable
@Table(name = "chromosomeElementDetails")
class ChromosomeElementDetailsData(
    val name: String? = null,
    val position: Int? = null,
    val lowerBoundary: Double? = null,
    val upperBoundary: Double? = null,
    val description: String? = null
) {
}