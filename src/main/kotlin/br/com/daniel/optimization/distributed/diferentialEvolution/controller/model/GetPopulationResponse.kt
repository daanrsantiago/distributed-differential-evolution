package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import java.time.ZonedDateTime

data class GetPopulationResponse (
    val id: Long?,
    val optimizationRunId: Long?,
    val generation: Int?,
    val size: Int?,
    var populationMembers: MutableList<ChromosomeResponse>?,
    val createdAt: ZonedDateTime
) {

    constructor(populationData: PopulationData): this(
        id = populationData.id,
        optimizationRunId = populationData.optimizationRunId,
        generation = populationData.generation,
        size = populationData.size,
        populationMembers = populationData.populationMembers?.map { ChromosomeResponse(it) }?.toMutableList(),
        createdAt = populationData.createdAt,
    )

}
