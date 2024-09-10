package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus

data class GetChromosomeForEvaluationResponse(
    val optimizationRunId: Long,
    val optimizationStatus: OptimizationStatus,
    val chromosome: ChromosomeResponse? = null
)
