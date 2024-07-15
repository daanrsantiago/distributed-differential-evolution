package br.com.daniel.optimization.distributed.diferentialEvolution.controller.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus

data class OptimizationRunChromosomeResponse(
    val optimizationRunId: Long?,
    val optimizationStatus: OptimizationStatus,
    val chromosome: ChromosomeResponse
)
