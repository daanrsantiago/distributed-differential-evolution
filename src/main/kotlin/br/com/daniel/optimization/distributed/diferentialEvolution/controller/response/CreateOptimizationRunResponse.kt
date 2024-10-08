package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy

data class CreateOptimizationRunResponse(
    val id: Long? = null,
    val objectiveFunctionId: Long? = null,
    val populationSize: Int? = null,
    val strategy: OptimizationStrategy? = null,
    val crossoverProbability: Double? = null,
    val perturbationFactor: Double? = null,
    val valueToReach: Double? = null,
    val maxGenerations: Int? = null,
    val objectiveFunctionEvaluationTimeoutSeconds: Long?,
    val maxObjectiveFunctionReEvaluations: Int?,
    val chromosomeElementDetails: List<ChromosomeElementDetails>? = null,
    val status: OptimizationStatus? = null,
) {

    constructor(optimizationRun: OptimizationRun): this(
        id = optimizationRun.id,
        objectiveFunctionId = optimizationRun.objectiveFunctionId,
        populationSize = optimizationRun.populationSize,
        strategy = optimizationRun.strategy,
        crossoverProbability = optimizationRun.crossoverProbability,
        perturbationFactor = optimizationRun.perturbationFactor,
        valueToReach = optimizationRun.valueToReach,
        maxGenerations = optimizationRun.maxGenerations,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRun.objectiveFunctionEvaluationTimeoutSeconds,
        maxObjectiveFunctionReEvaluations = optimizationRun.maxObjectiveFunctionReEvaluations,
        chromosomeElementDetails = optimizationRun.chromosomeElementsDetails,
        status = optimizationRun.status
    )

}
