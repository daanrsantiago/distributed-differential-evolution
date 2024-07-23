package client.response

data class ChromosomeResponse(
    val id: Long,
    val populationId: Long?,
    val optimizationRunId: Long,
    val objectiveFunctionId: Long,
    val type: ChromosomeType,
    val targetChromosomeId: Long?,
    val targetPopulationId: Long?,
    val evaluationStatus: EvaluationStatus,
    val evaluationId: String?,
    val evaluationBeginAt: String?,
    val evaluatedAt: String?,
    val fitness: Double?,
    val elements: List<Double>,
    val createdAt: String
)
