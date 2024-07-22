package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy

data class CreateOptimizationRunRequest (
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val strategy: OptimizationStrategy = OptimizationStrategy.DE_RAND_1_BIN,
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
            strategy = this.strategy,
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
