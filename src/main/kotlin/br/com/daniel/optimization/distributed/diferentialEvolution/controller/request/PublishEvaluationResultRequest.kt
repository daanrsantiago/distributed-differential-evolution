package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

data class PublishEvaluationResultRequest(
    val fitness: Double,
    val evaluationId: String
) {
}