package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import br.com.daniel.optimization.distributed.diferentialEvolution.util.bestChromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.util.meanFitness
import br.com.daniel.optimization.distributed.diferentialEvolution.util.worstChromosome

data class GetPopulationStatisticsResponse(
    val populationId: Long?,
    val optimizationRunId: Long?,
    val generation: Int,
    val bestFitness: Double?,
    val meanFitness: Double,
    val worstFitness: Double?
) {
    constructor(population: Population): this(
        populationId = population.id,
        optimizationRunId = population.optimizationRunId,
        generation = population.generation,
        bestFitness = population.populationMembers.bestChromosome().fitness,
        meanFitness = population.populationMembers.meanFitness(),
        worstFitness = population.populationMembers.worstChromosome().fitness
    )
}
