package client.request

data class PublishEvaluationErrorRequest(
    val evaluationId: String,
    val reason: String? = ""
)
