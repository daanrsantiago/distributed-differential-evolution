package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementDetailsData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus

data class CreateOptimizationRunResponse(
    val id: Long? = null,
    val objectiveFunctionId: Long? = null,
    val populationSize: Int? = null,
    val crossOverProbability: Double? = null,
    val perturbationFactor: Double? = null,
    val valueToReach: Double? = null,
    val maxGenerations: Int? = null,
    val objectiveFunctionEvaluationTimeoutSeconds: Long?,
    val chromosomeElementDetails: List<ChromosomeElementDetailsData>? = null,
    val status: OptimizationStatus? = null,
) {

    constructor(optimizationRunData: OptimizationRunData): this(
        id = optimizationRunData.id,
        objectiveFunctionId = optimizationRunData.objectiveFunctionId,
        populationSize = optimizationRunData.populationSize,
        crossOverProbability = optimizationRunData.crossOverProbability,
        perturbationFactor = optimizationRunData.perturbationFactor,
        valueToReach = optimizationRunData.valueToReach,
        maxGenerations = optimizationRunData.maxGenerations,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        chromosomeElementDetails = optimizationRunData.chromosomeElementsDetails,
        status = optimizationRunData.status
    )

}
