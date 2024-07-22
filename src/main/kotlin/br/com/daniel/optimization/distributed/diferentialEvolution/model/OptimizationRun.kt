package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy.DE_RAND_1_BIN
import java.time.ZoneId
import java.time.ZonedDateTime

class OptimizationRun(
    val id: Long? = null,
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val strategy: OptimizationStrategy = DE_RAND_1_BIN,
    val crossOverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    var currentGeneration: Int,
    var bestSoFarChromosome: Chromosome? = null,
    var status: OptimizationStatus,
    val objectiveFunctionEvaluationTimeoutSeconds: Long? = null,
    val chromosomeElementsDetails: MutableList<ChromosomeElementDetails>,
    var timeToFinishInSeconds: Long? = null,
    var finishedAt: ZonedDateTime? = null,
    val createdAt: ZonedDateTime? = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
    ) {
    init {
        chromosomeElementsDetails.sortBy { it.position }
    }

    constructor(optimizationRunData: OptimizationRunData): this(
        id = optimizationRunData.id!!,
        objectiveFunctionId = optimizationRunData.objectiveFunctionId!!,
        populationSize = optimizationRunData.populationSize!!,
        strategy = optimizationRunData.strategy,
        crossOverProbability = optimizationRunData.crossOverProbability!!,
        perturbationFactor = optimizationRunData.perturbationFactor!!,
        valueToReach = optimizationRunData.valueToReach,
        maxGenerations = optimizationRunData.maxGenerations!!,
        currentGeneration = optimizationRunData.currentGeneration,
        bestSoFarChromosome = optimizationRunData.bestSoFarChromosome?.let { Chromosome(it) },
        status = optimizationRunData.status,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        chromosomeElementsDetails = optimizationRunData.chromosomeElementsDetails!!
            .map { ChromosomeElementDetails(it) }
            .toMutableList(),
        timeToFinishInSeconds = optimizationRunData.timeToFinishInSeconds,
        finishedAt = optimizationRunData.finishedAt,
        createdAt = optimizationRunData.createdAt
    )

    fun toOptimizationRunData(): OptimizationRunData {
        return OptimizationRunData(
            id = id,
            objectiveFunctionId= objectiveFunctionId,
            populationSize = populationSize,
            strategy = strategy,
            crossOverProbability = crossOverProbability,
            perturbationFactor = perturbationFactor,
            valueToReach = valueToReach,
            maxGenerations = maxGenerations,
            currentGeneration = currentGeneration,
            bestSoFarChromosome = bestSoFarChromosome?.toChromosomeData(),
            status = status,
            objectiveFunctionEvaluationTimeoutSeconds = objectiveFunctionEvaluationTimeoutSeconds,
            chromosomeElementsDetails = chromosomeElementsDetails.map { it.toChromosomeElementDetailData() }.toMutableList(),
            timeToFinishInSeconds = timeToFinishInSeconds,
            finishedAt = finishedAt,
            createdAt = createdAt
        )
    }

}