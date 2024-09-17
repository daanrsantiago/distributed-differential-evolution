package client.response

data class GetChromosomeForEvaluationResponse(
    val optimizationRunId: Long,
    val optimizationStatus: OptimizationStatus,
    val chromosome: ChromosomeResponse? = null
)