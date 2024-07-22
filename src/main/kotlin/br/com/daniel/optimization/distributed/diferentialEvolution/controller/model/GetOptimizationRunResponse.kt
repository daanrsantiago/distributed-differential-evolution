package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy
import java.time.ZonedDateTime

data class GetOptimizationRunResponse (
    val id: Long?,
    val objectiveFunctionId: Long?,
    val populationSize: Int?,
    val strategy: OptimizationStrategy?,
    val crossOverProbability: Double?,
    val perturbationFactor: Double?,
    val valueToReach: Double?,
    val maxGenerations: Int?,
    val currentGeneration: Int?,
    var bestSoFarChromosome: ChromosomeResponse?,
    val objectiveFunctionEvaluationTimeoutSeconds: Long?,
    var status: OptimizationStatus,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetails>?,
    val timeToFinishInSeconds: Long?,
    val finishedAt: ZonedDateTime?,
    val createdAt: ZonedDateTime?
) {

    constructor(optimizationRun: OptimizationRun): this(
        id = optimizationRun.id!!,
        objectiveFunctionId = optimizationRun.objectiveFunctionId,
        populationSize = optimizationRun.populationSize,
        strategy = optimizationRun.strategy,
        crossOverProbability = optimizationRun.crossOverProbability,
        perturbationFactor = optimizationRun.perturbationFactor,
        valueToReach = optimizationRun.valueToReach,
        maxGenerations = optimizationRun.maxGenerations,
        currentGeneration = optimizationRun.currentGeneration,
        bestSoFarChromosome = optimizationRun.bestSoFarChromosome?.let { ChromosomeResponse(it) },
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRun.objectiveFunctionEvaluationTimeoutSeconds,
        status = optimizationRun.status,
        chromosomeElementDetails = optimizationRun.chromosomeElementsDetails,
        timeToFinishInSeconds = optimizationRun.timeToFinishInSeconds,
        finishedAt = optimizationRun.finishedAt,
        createdAt = optimizationRun.createdAt,
    )

}
