package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

data class ChangeEvaluationResultRequest(
    val fitness: Double,
    val evaluationId: String
) {
}