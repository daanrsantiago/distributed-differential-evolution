package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementDetailsData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData

data class CreateOptimizationRunRequest (
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val crossOverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    val objectiveFunctionEvaluationTimeoutSeconds: Long,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetailsData>,
) {
    fun toOptimizationRunData(): OptimizationRunData {
        return OptimizationRunData(
            objectiveFunctionId = this.objectiveFunctionId,
            populationSize = this.populationSize,
            crossOverProbability = this.crossOverProbability,
            perturbationFactor = this.perturbationFactor,
            valueToReach = this.valueToReach,
            currentGeneration = 1,
            maxGenerations = this.maxGenerations,
            objectiveFunctionEvaluationTimeoutSeconds = this.objectiveFunctionEvaluationTimeoutSeconds,
            chromosomeElementDetails = this.chromosomeElementDetails
        )
    }
}
