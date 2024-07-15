package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface PopulationRepository: PagingAndSortingRepository<PopulationData, Long>, CrudRepository<PopulationData, Long> {

    fun findAllByOptimizationRunId(optimizationRunId: Long, pageable: Pageable): Page<PopulationData>

}