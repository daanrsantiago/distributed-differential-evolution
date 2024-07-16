package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementDetailsData

data class ChromosomeElementDetails(
    val name: String,
    val position: Int,
    val lowerBoundary: Double,
    val upperBoundary: Double,
    val description: String? = null
) {

    constructor(chromosomeElementDetailsData: ChromosomeElementDetailsData): this(
        name = chromosomeElementDetailsData.name!!,
        position = chromosomeElementDetailsData.position!!,
        lowerBoundary = chromosomeElementDetailsData.lowerBoundary!!,
        upperBoundary = chromosomeElementDetailsData.upperBoundary!!,
        description = chromosomeElementDetailsData.description
    )

}
