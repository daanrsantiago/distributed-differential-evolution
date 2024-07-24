package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import java.time.ZonedDateTime

data class GetOptimizationRunPopulationsResponse(
    val id: Long?,
    val optimizationRunId: Long?,
    val generation: Int?,
    val statistics: GetPopulationStatisticsResponse,
    val createdAt: ZonedDateTime
) {

    constructor(population: Population): this(
        id = population.id,
        optimizationRunId = population.optimizationRunId,
        generation = population.generation,
        statistics = GetPopulationStatisticsResponse(population),
        createdAt = population.createdAt,
    )

}
