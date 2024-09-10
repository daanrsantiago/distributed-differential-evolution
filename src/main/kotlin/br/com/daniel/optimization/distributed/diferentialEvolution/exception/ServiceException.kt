package br.com.daniel.optimization.distributed.diferentialEvolution.exception

sealed class ServiceException (
    message: String
): RuntimeException(message) {

    class OptimizationRunFinishedException(optimizationRunId: Long): ServiceException("OptimizationRun with id $optimizationRunId already with status FINISHED")
    class NoChromosomeFoundForEvaluation(optimizationRunId: Long): ServiceException("No chromosome found for evaluation on optimizationRun with id $optimizationRunId")

}