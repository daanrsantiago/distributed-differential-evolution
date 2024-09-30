package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy

data class CreateOptimizationRunRequest (
    val objectiveFunctionId: Long,
    val populationSize: Int,
    val strategy: OptimizationStrategy = OptimizationStrategy.DE_RAND_1_BIN,
    val crossoverProbability: Double,
    val perturbationFactor: Double,
    val valueToReach: Double? = null,
    val maxGenerations: Int,
    val objectiveFunctionEvaluationTimeoutSeconds: Long,
    val maxObjectiveFunctionReEvaluations: Int,
    val chromosomeElementDetails: MutableList<ChromosomeElementDetails>,
) {
    fun toOptimizationRun(): OptimizationRun {
        return OptimizationRun(
            objectiveFunctionId = this.objectiveFunctionId,
            populationSize = this.populationSize,
            strategy = this.strategy,
            crossoverProbability = this.crossoverProbability,
            perturbationFactor = this.perturbationFactor,
            valueToReach = this.valueToReach,
            maxGenerations = this.maxGenerations,
            currentGeneration = 1,
            status = OptimizationStatus.RUNNING,
            objectiveFunctionEvaluationTimeoutSeconds = this.objectiveFunctionEvaluationTimeoutSeconds,
            maxObjectiveFunctionReEvaluations = this.maxObjectiveFunctionReEvaluations,
            chromosomeElementsDetails = this.chromosomeElementDetails
        )
    }
}
