package client.response

class OptimizationRunResponse(
    val optimizationRunId: Long,
    val optimizationStatus: OptimizationStatus,
    val chromosome: ChromosomeResponse? = null
) {
}