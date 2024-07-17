package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import java.time.ZonedDateTime

class OptimizationRun(
    val id: Long,
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val crossOverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    val currentGeneration: Int,
    var status: OptimizationStatus,
    val objectiveFunctionEvaluationTimeoutSeconds: Long? = null,
    val chromosomeElementsDetails: MutableList<ChromosomeElementDetails>,
    val timeToFinishInSeconds: Long? = null,
    val finishedAt: ZonedDateTime? = null,
    val createdAt: ZonedDateTime
    ) {
    init {
        chromosomeElementsDetails.sortBy { it.position }
    }

    constructor(optimizationRunData: OptimizationRunData): this(
        id = optimizationRunData.id!!,
        objectiveFunctionId = optimizationRunData.objectiveFunctionId!!,
        populationSize = optimizationRunData.populationSize!!,
        crossOverProbability = optimizationRunData.crossOverProbability!!,
        perturbationFactor = optimizationRunData.perturbationFactor!!,
        valueToReach = optimizationRunData.valueToReach,
        maxGenerations = optimizationRunData.maxGenerations!!,
        currentGeneration = optimizationRunData.currentGeneration,
        status = optimizationRunData.status,
        objectiveFunctionEvaluationTimeoutSeconds = optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        chromosomeElementsDetails = optimizationRunData.chromosomeElementsDetails!!
            .map { ChromosomeElementDetails(it) }
            .toMutableList(),
        timeToFinishInSeconds = optimizationRunData.timeToFinishInSeconds,
        finishedAt = optimizationRunData.finishedAt,
        createdAt = optimizationRunData.createdAt
    )

    fun createInitialPopulation(): Population {
        val populationMembers = mutableListOf<Chromosome>()
        for (iChromosome in 1..populationSize) {
            val chromosomeElements = mutableListOf<Double>()
            for(chromosomeElementDetails in chromosomeElementsDetails) {
                val chromosomeElementValue = chromosomeElementDetails.lowerBoundary +
                        Math.random() * (chromosomeElementDetails.upperBoundary - chromosomeElementDetails.lowerBoundary)
                chromosomeElements.add(chromosomeElementValue)
            }
            val chromosome = Chromosome(
                type = TARGET,
                elements = chromosomeElements
            )
            populationMembers.add(chromosome)
        }

        return Population(
            generation = 1,
            populationMembers = populationMembers
        )
    }

}