package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import java.time.ZonedDateTime

data class GetPopulationResponse (
    val id: Long?,
    val optimizationRunId: Long?,
    val generation: Int?,
    var members: List<ChromosomeResponse>?,
    val chromosomesRemainingToBeEvaluated: Int,
    val createdAt: ZonedDateTime
) {

    constructor(population: Population): this(
        id = population.id,
        optimizationRunId = population.optimizationRunId,
        generation = population.generation,
        members = population.members.map { ChromosomeResponse(it) },
        chromosomesRemainingToBeEvaluated = population.chromosomesRemainingToBeEvaluated,
        createdAt = population.createdAt,
    )

}
