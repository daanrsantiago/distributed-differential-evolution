package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

data class ChangeEvaluationResultRequest(
    val fitness: Double,
    val evaluationId: String
) {
}