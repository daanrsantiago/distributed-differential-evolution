package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementDetailsData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import java.time.ZonedDateTime

data class GetOptimizationRunResponse (
    val id: Long?,
    val objectiveFunctionId: Long?,
    val populationSize: Int?,
    val crossOverProbability: Double?,
    val perturbationFactor: Double?,
    val valueToReach: Double?,
    val maxGenerations: Int?,
    val currentGeneration: Int?,
    var bestSoFarChromosome: ChromosomeResponse?,
    val objectiveFunctionEvaluationTimeoutSeconds: Long?,
    var status: OptimizationStatus,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetailsData>?,
    val createdAt: ZonedDateTime
) {

    constructor(optimizationRunData: OptimizationRunData): this(
        id = optimizationRunData.id!!,
        objectiveFunctionId = optimizationRunData.objectiveFunctionId,
        populationSize = optimizationRunData.populationSize,
        crossOverProbability = optimizationRunData.crossOverProbability,
        perturbationFactor = optimizationRunData.perturbationFactor,
        valueToReach = optimizationRunData.valueToReach,
        maxGenerations = optimizationRunData.maxGenerations!!,
        currentGeneration = optimizationRunData.currentGeneration,
        bestSoFarChromosome = optimizationRunData.bestSoFarChromosome?.let { ChromosomeResponse(it) },
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        status = optimizationRunData.status,
        chromosomeElementDetails = optimizationRunData.chromosomeElementDetails,
        createdAt = optimizationRunData.createdAt,
    )

}
