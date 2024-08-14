package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import java.time.ZoneId
import java.time.ZonedDateTime

data class Chromosome(
    var id: Long? = null,
    var populationId: Long? = null,
    var optimizationRunId: Long? = null,
    var objectiveFunctionId: Long? = null,
    var fitness: Double? = null,
    var type: ChromosomeType,
    val targetChromosomeId: Long? = null,
    val targetPopulationId: Long? = null,
    var evaluationStatus: EvaluationStatus = EvaluationStatus.NOT_EVALUATED,
    var evaluationErrorReason: String? = null,
    var evaluationRetries: Int = 0,
    var evaluationId: String? = null,
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
    val elements: MutableList<Double>,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {

    constructor(chromosomeData: ChromosomeData): this(
        id = chromosomeData.id,
        populationId = chromosomeData.populationId,
        optimizationRunId = chromosomeData.optimizationRunId,
        objectiveFunctionId = chromosomeData.objectiveFunctionId,
        fitness = chromosomeData.fitness,
        type = chromosomeData.type,
        targetChromosomeId = chromosomeData.targetChromosomeId,
        targetPopulationId = chromosomeData.targetPopulationId,
        evaluationStatus = chromosomeData.evaluationStatus,
        evaluationErrorReason = chromosomeData.evaluationErrorReason,
        evaluationRetries = chromosomeData.evaluationRetries,
        evaluationId = chromosomeData.evaluationId,
        evaluationBeginAt = chromosomeData.evaluationBeginAt,
        evaluatedAt = chromosomeData.evaluatedAt,
        elements = chromosomeData.elements!!.sortedBy { it.position }.map { it.value }.toMutableList(),
        createdAt = chromosomeData.createdAt
    )

    fun toChromosomeData(): ChromosomeData {
        return ChromosomeData(
            id = id,
            populationId = populationId,
            optimizationRunId = optimizationRunId,
            objectiveFunctionId = objectiveFunctionId,
            fitness = fitness,
            type = type,
            targetChromosomeId = targetChromosomeId,
            targetPopulationId = targetPopulationId,
            evaluationStatus = evaluationStatus,
            evaluationErrorReason = evaluationErrorReason,
            evaluationId = evaluationId,
            evaluatedAt = evaluatedAt,
            evaluationBeginAt = evaluationBeginAt,
            elements = elements.mapIndexed { position, value ->
                ChromosomeElementData(
                    position = position,
                    value = value
                )
            }.toMutableList(),
            createdAt = createdAt
        )
    }

}
