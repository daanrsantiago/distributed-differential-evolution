package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import java.time.ZonedDateTime

class Chromosome(
    var id: Long? = null,
    var populationId: Long? = null,
    val fitness: Double? = null,
    val size: Int,
    var type: ChromosomeType,
    val targetChromosomeId: Long? = null,
    val targetPopulationId: Long? = null,
    var evaluationStatus: EvaluationStatus = EvaluationStatus.NOT_EVALUATED,
    var evaluationId: String? = null,
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
    val elements: MutableList<Double>,
) {

    constructor(chromosomeData: ChromosomeData): this(
        id = chromosomeData.id,
        populationId = chromosomeData.populationId,
        fitness = chromosomeData.fitness,
        size = chromosomeData.size,
        type = chromosomeData.type,
        targetChromosomeId = chromosomeData.targetChromosomeId,
        targetPopulationId = chromosomeData.targetPopulationId,
        evaluationStatus = chromosomeData.evaluationStatus,
        evaluationBeginAt = chromosomeData.evaluationBeginAt,
        evaluatedAt = chromosomeData.evaluatedAt,
        elements = chromosomeData.elements!!.sortedBy { it.position }.map { it.value }.toMutableList()
    )

    fun toChromosomeData(optimizationRunId: Long, objectiveFunctionId: Long): ChromosomeData {
        return ChromosomeData(
            id = id,
            populationId = populationId,
            fitness = fitness,
            optimizationRunId = optimizationRunId,
            objectiveFunctionId = objectiveFunctionId,
            size = size,
            type = type,
            targetChromosomeId = targetChromosomeId,
            targetPopulationId = targetPopulationId,
            evaluationStatus = evaluationStatus,
            evaluationId = evaluationId,
            evaluatedAt = evaluatedAt,
            evaluationBeginAt = evaluationBeginAt,
            elements = elements.mapIndexed { position, value ->
                ChromosomeElementData(
                    position = position,
                    value = value
                )
            }.toMutableList()
        )
    }

}
