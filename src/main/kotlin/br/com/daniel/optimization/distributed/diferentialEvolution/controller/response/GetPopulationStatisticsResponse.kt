package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import br.com.daniel.optimization.distributed.diferentialEvolution.util.bestChromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.util.meanFitness
import br.com.daniel.optimization.distributed.diferentialEvolution.util.worstChromosome

data class GetPopulationStatisticsResponse(
    val populationId: Long?,
    val optimizationRunId: Long?,
    val generation: Int,
    val bestChromosome: ChromosomeResponse? = null,
    val bestFitness: Double?,
    val meanFitness: Double,
    val worstFitness: Double?
) {
    constructor(population: Population): this(
        populationId = population.id,
        optimizationRunId = population.optimizationRunId,
        generation = population.generation,
        bestChromosome = ChromosomeResponse(population.members.bestChromosome()),
        bestFitness = population.members.bestChromosome().fitness,
        meanFitness = population.members.meanFitness(),
        worstFitness = population.members.worstChromosome().fitness
    )
}
