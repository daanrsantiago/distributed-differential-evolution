package client.request

data class PublishEvaluationResultRequest(
    val fitness: Double,
    val evaluationId: String
)
