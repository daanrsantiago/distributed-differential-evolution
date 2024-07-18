package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun

data class CreateOptimizationRunRequest (
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val crossOverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    val objectiveFunctionEvaluationTimeoutSeconds: Long,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetails>,
) {
    fun toOptimizationRun(): OptimizationRun {
        return OptimizationRun(
            objectiveFunctionId = this.objectiveFunctionId,
            populationSize = this.populationSize,
            crossOverProbability = this.crossOverProbability,
            perturbationFactor = this.perturbationFactor,
            valueToReach = this.valueToReach,
            maxGenerations = this.maxGenerations,
            currentGeneration = 1,
            status = OptimizationStatus.RUNNING,
            objectiveFunctionEvaluationTimeoutSeconds = this.objectiveFunctionEvaluationTimeoutSeconds,
            chromosomeElementsDetails = this.chromosomeElementDetails
        )
    }
}
