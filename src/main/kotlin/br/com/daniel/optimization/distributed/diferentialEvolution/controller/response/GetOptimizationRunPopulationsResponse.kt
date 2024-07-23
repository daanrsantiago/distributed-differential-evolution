package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import java.time.ZonedDateTime

data class GetOptimizationRunPopulationsResponse(
    val id: Long?,
    val optimizationRunId: Long?,
    val generation: Int?,
    val size: Int?,
    val createdAt: ZonedDateTime
) {

    constructor(populationData: PopulationData): this(
        id = populationData.id,
        optimizationRunId = populationData.optimizationRunId,
        generation = populationData.generation,
        size = populationData.size,
        createdAt = populationData.createdAt,
    )

}
