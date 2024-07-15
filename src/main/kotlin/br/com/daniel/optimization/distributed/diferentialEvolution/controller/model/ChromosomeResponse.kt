package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import java.time.ZonedDateTime

data class ChromosomeResponse(
    val id: Long,
    val populationId: Long?,
    val objectiveFunctionId: Long?,
    val type: ChromosomeType,
    val targetChromosomeId: Long?,
    val targetPopulationId: Long?,
    val evaluationStatus: EvaluationStatus,
    val evaluationId: String?,
    val evaluationBeginAt: ZonedDateTime?,
    val evaluatedAt: ZonedDateTime?,
    val fitness: Double?,
    val size: Int,
    val elements: List<Double>?,
    val createdAt: ZonedDateTime
) {
    constructor(chromosomeData: ChromosomeData): this(
        chromosomeData.id!!,
        chromosomeData.populationId,
        chromosomeData.objectiveFunctionId,
        chromosomeData.type,
        chromosomeData.targetChromosomeId,
        chromosomeData.targetPopulationId,
        chromosomeData.evaluationStatus,
        chromosomeData.evaluationId,
        chromosomeData.evaluationBeginAt,
        chromosomeData.evaluatedAt,
        chromosomeData.fitness,
        chromosomeData.size,
        chromosomeData.elements?.sortedBy { it.position }?.map { it.value },
        chromosomeData.createdAt
    ) {

    }
}
