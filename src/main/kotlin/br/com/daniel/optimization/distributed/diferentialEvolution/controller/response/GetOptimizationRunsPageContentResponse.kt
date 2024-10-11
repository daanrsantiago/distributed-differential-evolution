package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.GetOptimizationRunResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy
import java.time.ZonedDateTime

data class GetOptimizationRunsPageContentResponse(
    val id: Long,
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val strategy: OptimizationStrategy,
    val crossoverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double?,
    val maxGenerations: Int,
    val currentGeneration: Int,
    val objectiveFunctionEvaluationTimeoutSeconds: Long,
    val maxObjectiveFunctionReEvaluations: Int,
    var status: OptimizationStatus,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetails>,
    val timeToFinishInSeconds: Long?,
    val finishedAt: ZonedDateTime?,
    val createdAt: ZonedDateTime?
) {
    constructor(optimizationRun: OptimizationRun): this(
        id = optimizationRun.id!!,
        objectiveFunctionId = optimizationRun.objectiveFunctionId,
        populationSize = optimizationRun.populationSize,
        strategy = optimizationRun.strategy,
        crossoverProbability = optimizationRun.crossoverProbability,
        perturbationFactor = optimizationRun.perturbationFactor,
        valueToReach = optimizationRun.valueToReach,
        maxGenerations = optimizationRun.maxGenerations,
        currentGeneration = optimizationRun.currentGeneration,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRun.objectiveFunctionEvaluationTimeoutSeconds!!,
        maxObjectiveFunctionReEvaluations = optimizationRun.maxObjectiveFunctionReEvaluations,
        status = optimizationRun.status,
        chromosomeElementDetails = optimizationRun.chromosomeElementsDetails,
        timeToFinishInSeconds = optimizationRun.timeToFinishInSeconds,
        finishedAt = optimizationRun.finishedAt,
        createdAt = optimizationRun.createdAt,
    )
}
