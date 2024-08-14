package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

data class PublishEvaluationErrorRequest(
    val evaluationId: String,
    val reason: String = ""
)
