package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import java.time.ZonedDateTime

data class GetPopulationResponse (
    val id: Long?,
    val optimizationRunId: Long?,
    val generation: Int?,
    var populationMembers: List<ChromosomeResponse>?,
    val createdAt: ZonedDateTime
) {

    constructor(population: Population): this(
        id = population.id,
        optimizationRunId = population.optimizationRunId,
        generation = population.generation,
        populationMembers = population.populationMembers.map { ChromosomeResponse(it) },
        createdAt = population.createdAt,
    )

}
