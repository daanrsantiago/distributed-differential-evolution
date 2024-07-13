package br.com.daniel.optimization.distributed.diferentialEvolution.model

data class ChromosomeElementDetails(
    val name: String,
    val position: Int,
    val lowerBoundary: Double,
    val upperBoundary: Double,
    val description: String? = null
) {

}
