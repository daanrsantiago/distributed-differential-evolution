package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import jakarta.persistence.Column
import jakarta.persistence.Embeddable


@Embeddable
data class ChromosomeElementData(
    val position: Int,
    @Column(name = "chromosomeValue")
    val value: Double
)