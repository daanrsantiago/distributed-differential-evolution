package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface ChromosomeRepository: CrudRepository<ChromosomeData, Long> {

    @Query("select c from ChromosomeData c where c.optimizationRunId = ?1 and c.evaluationStatus = NOT_EVALUATED")
    fun getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId: Long): List<ChromosomeData>

    fun findAllByOptimizationRunIdAndEvaluationStatus(optimizationRunId: Long, evaluationStatus: EvaluationStatus): List<ChromosomeData>

    @Query(
        """SELECT CASE WHEN COUNT(c) = SUM(CASE WHEN c.evaluationStatus = 'EVALUATED' THEN 1 ELSE 0 END) THEN TRUE ELSE FALSE END
           FROM Chromosome c 
           WHERE c.populationId = :populationId"""
    )
    fun areAllChromosomesEvaluated(@Param("populationId") populationId: Long?): Boolean
}