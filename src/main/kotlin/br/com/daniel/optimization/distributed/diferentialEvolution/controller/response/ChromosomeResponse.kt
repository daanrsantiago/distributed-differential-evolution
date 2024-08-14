package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import java.time.ZonedDateTime

data class ChromosomeResponse(
    val id: Long,
    val populationId: Long?,
    val optimizationRunId: Long?,
    val objectiveFunctionId: Long?,
    val type: ChromosomeType,
    val targetChromosomeId: Long?,
    val targetPopulationId: Long?,
    val evaluationStatus: EvaluationStatus,
    val evaluationErrorReason: String?,
    val evaluationRetries: Int,
    val evaluationId: String?,
    val evaluationBeginAt: ZonedDateTime?,
    val evaluatedAt: ZonedDateTime?,
    val fitness: Double?,
    val elements: List<Double>?,
    val createdAt: ZonedDateTime
) {
    constructor(chromosome: Chromosome): this(
        chromosome.id!!,
        chromosome.populationId,
        chromosome.optimizationRunId,
        chromosome.objectiveFunctionId,
        chromosome.type,
        chromosome.targetChromosomeId,
        chromosome.targetPopulationId,
        chromosome.evaluationStatus,
        chromosome.evaluationErrorReason,
        chromosome.evaluationRetries,
        chromosome.evaluationId,
        chromosome.evaluationBeginAt,
        chromosome.evaluatedAt,
        chromosome.fitness,
        chromosome.elements,
        chromosome.createdAt
    ) {

    }
}
