package client.request

data class ChangeEvaluationResultRequest(
    val fitness: Double,
    val evaluationId: String
)
