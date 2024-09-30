package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy.DE_RAND_1_BIN
import java.time.ZoneId
import java.time.ZonedDateTime

data class OptimizationRun(
    val id: Long? = null,
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val strategy: OptimizationStrategy = DE_RAND_1_BIN,
    val crossoverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    var currentGeneration: Int,
    var bestSoFarChromosome: Chromosome? = null,
    var status: OptimizationStatus,
    val objectiveFunctionEvaluationTimeoutSeconds: Long? = null,
    val maxObjectiveFunctionReEvaluations: Int = 3,
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
        crossoverProbability = optimizationRunData.crossoverProbability!!,
        perturbationFactor = optimizationRunData.perturbationFactor!!,
        valueToReach = optimizationRunData.valueToReach,
        maxGenerations = optimizationRunData.maxGenerations!!,
        currentGeneration = optimizationRunData.currentGeneration,
        bestSoFarChromosome = optimizationRunData.bestSoFarChromosome?.let { Chromosome(it) },
        status = optimizationRunData.status,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        maxObjectiveFunctionReEvaluations = optimizationRunData.maxObjectiveFunctionReEvaluations,
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
            crossoverProbability = crossoverProbability,
            perturbationFactor = perturbationFactor,
            valueToReach = valueToReach,
            maxGenerations = maxGenerations,
            currentGeneration = currentGeneration,
            bestSoFarChromosome = bestSoFarChromosome?.toChromosomeData(),
            status = status,
            objectiveFunctionEvaluationTimeoutSeconds = objectiveFunctionEvaluationTimeoutSeconds,
            maxObjectiveFunctionReEvaluations = maxObjectiveFunctionReEvaluations,
            chromosomeElementsDetails = chromosomeElementsDetails.map { it.toChromosomeElementDetailData() }.toMutableList(),
            timeToFinishInSeconds = timeToFinishInSeconds,
            finishedAt = finishedAt,
            createdAt = createdAt
        )
    }

}