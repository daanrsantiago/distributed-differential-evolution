package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import java.util.*

data class PatchOptimizationRunRequest(
    val crossoverProbability: Optional<Double> = Optional.empty(),
    val perturbationFactor: Optional<Double> = Optional.empty(),
    val valueToReach: Optional<Double> = Optional.empty(),
    val maxGenerations: Optional<Int> = Optional.empty(),
    val objectiveFunctionEvaluationTimeoutSeconds: Optional<Long> = Optional.empty(),
    val maxObjectiveFunctionReEvaluations: Optional<Int> = Optional.empty(),
) {

    fun patchOptimizationRun(optimizationRun: OptimizationRun): OptimizationRun {
        return optimizationRun.copy(
            crossoverProbability = crossoverProbability.orElse(optimizationRun.crossoverProbability),
            perturbationFactor = perturbationFactor.orElse(optimizationRun.perturbationFactor),
            valueToReach = valueToReach.orElse(optimizationRun.valueToReach),
            maxGenerations = maxGenerations.orElse(optimizationRun.maxGenerations),
            objectiveFunctionEvaluationTimeoutSeconds = objectiveFunctionEvaluationTimeoutSeconds.orElse(optimizationRun.objectiveFunctionEvaluationTimeoutSeconds),
            maxObjectiveFunctionReEvaluations = maxObjectiveFunctionReEvaluations.orElse(optimizationRun.maxObjectiveFunctionReEvaluations)
        )
    }
}
