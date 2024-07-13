package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import java.time.ZonedDateTime

data class ChromosomeResponse(
    val id: Long,
    val populationId: Long?,
    val objectiveFunctionId: Long?,
    val evaluationStatus: EvaluationStatus,
    val fitness: Double?,
    val size: Int,
    val elements: List<Double>?,
    val createdAt: ZonedDateTime
) {
    constructor(chromosomeData: ChromosomeData): this(
        chromosomeData.id!!,
        chromosomeData.populationId,
        chromosomeData.objectiveFunctionId,
        chromosomeData.evaluationStatus,
        chromosomeData.fitness,
        chromosomeData.size,
        chromosomeData.elements?.sortedBy { it.position }?.map { it.value },
        chromosomeData.createdAt
    ) {

    }
}
