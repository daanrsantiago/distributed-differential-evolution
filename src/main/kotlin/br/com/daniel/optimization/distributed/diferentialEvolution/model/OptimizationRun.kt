package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus

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
    ) {
    init {
        chromosomeElementsDetails.sortBy { it.position }
    }

    constructor(optimizationRunData: OptimizationRunData): this(
        optimizationRunData.id!!,
        optimizationRunData.objectiveFunctionId!!,
        optimizationRunData.populationSize!!,
        optimizationRunData.crossOverProbability!!,
        optimizationRunData.perturbationFactor!!,
        optimizationRunData.valueToReach,
        optimizationRunData.maxGenerations!!,
        optimizationRunData.currentGeneration,
        optimizationRunData.status,
        optimizationRunData.objectiveFunctionEvaluationTimeoutSeconds,
        optimizationRunData.chromosomeElementDetails!!
            .map { ChromosomeElementDetails(it) }
            .toMutableList(),
    )

    val bestSoFarChromosomeId: Int? = null

    fun createInitialPopulation(): Population {
        val populationMembers = mutableListOf<Chromosome>()
        val chromosomeSize = chromosomeElementsDetails.size
        for (iChromosome in 1..populationSize) {
            val chromosomeElements = mutableListOf<Double>()
            for(chromosomeElementDetails in chromosomeElementsDetails) {
                val chromosomeElementValue = chromosomeElementDetails.lowerBoundary +
                        Math.random() * (chromosomeElementDetails.upperBoundary - chromosomeElementDetails.lowerBoundary)
                chromosomeElements.add(chromosomeElementValue)
            }
            val chromosome = Chromosome(
                type = TARGET,
                size = chromosomeSize,
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