package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param

interface ChromosomeRepository: PagingAndSortingRepository<ChromosomeData, Long>, CrudRepository<ChromosomeData, Long> {

    @Query("select c from ChromosomeData c where c.optimizationRunId = :optimizationRunId and (c.evaluationStatus = NOT_EVALUATED OR c.evaluationStatus = TIMEOUT)")
    fun getNotEvaluatedChromosomeByOptimizationRunId(@Param("optimizationRunId") optimizationRunId: Long): List<ChromosomeData>

    fun findAllByOptimizationRunIdAndEvaluationStatus(optimizationRunId: Long, evaluationStatus: EvaluationStatus): List<ChromosomeData>

    @Query(
        """SELECT CASE WHEN COUNT(c) = SUM(CASE WHEN c.evaluationStatus = EVALUATED THEN 1 ELSE 0 END) THEN TRUE ELSE FALSE END
           FROM ChromosomeData c 
           WHERE (c.populationId = :populationId OR c.targetPopulationId = :populationId)"""
    )
    fun areAllChromosomesEvaluated(@Param("populationId") populationId: Long?): Boolean

    fun findAllByPopulationId(populationId: Long?): List<ChromosomeData>

    fun findAllByTargetPopulationIdAndType(targetPopulationId: Long?, type: ChromosomeType, pageable: Pageable): Page<ChromosomeData>

    fun findAllByTargetPopulationIdAndType(targetPopulationId: Long?, type: ChromosomeType): List<ChromosomeData>
}