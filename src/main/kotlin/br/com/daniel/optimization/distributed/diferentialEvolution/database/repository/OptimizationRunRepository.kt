package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import org.springframework.data.repository.CrudRepository

interface OptimizationRunRepository: CrudRepository<OptimizationRunData, Long> {

    fun findAllByStatus(status: OptimizationStatus): List<OptimizationRunData>

}