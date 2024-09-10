package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import jakarta.persistence.LockModeType.PESSIMISTIC_WRITE
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime

interface ChromosomeRepository: PagingAndSortingRepository<ChromosomeData, Long>, CrudRepository<ChromosomeData, Long> {

    @Transactional
    @Lock(PESSIMISTIC_WRITE)
    @Query("SELECT c FROM ChromosomeData c WHERE c.optimizationRunId = :optimizationRunId AND (c.evaluationStatus = NOT_EVALUATED OR c.evaluationStatus = TIMEOUT)")
    fun getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId: Long): List<ChromosomeData>

    fun findAllByOptimizationRunIdAndEvaluationStatus(optimizationRunId: Long, evaluationStatus: EvaluationStatus): List<ChromosomeData>

    fun findAllByPopulationId(populationId: Long?): List<ChromosomeData>

    fun findAllByTargetPopulationIdAndType(targetPopulationId: Long?, type: ChromosomeType, pageable: Pageable): Page<ChromosomeData>

    fun findAllByTargetPopulationIdAndType(targetPopulationId: Long?, type: ChromosomeType): List<ChromosomeData>

    fun findAllByTargetChromosomeId(targetChromosomeId: Long): List<ChromosomeData>

    @Transactional
    @Query("UPDATE ChromosomeData c " +
            "SET c.evaluationId = :evaluationId, c.evaluationStatus = EVALUATING, c.evaluationBeginAt = :evaluationBeginAt " +
            "WHERE c.id = :chromosomeId")
    @Modifying
    fun updateBeginEvaluationData(chromosomeId: Long, evaluationId: String, evaluationBeginAt: ZonedDateTime)

    @Transactional
    @Query("UPDATE ChromosomeData c " +
            "SET c.fitness = :fitness, c.evaluationStatus = EVALUATED, c.evaluatedAt = :evaluatedAt " +
            "WHERE c.id = :chromosomeId")
    @Modifying
    fun updateEndEvaluationData(chromosomeId: Long, fitness: Double, evaluatedAt: ZonedDateTime)

    @Transactional
    @Query("UPDATE ChromosomeData c " +
            "SET c.evaluationStatus = ERROR, c.evaluationErrorReason = :evaluationErrorReason " +
            "WHERE c.id = :chromosomeId")
    @Modifying
    fun updateEvaluationErrorData(chromosomeId: Long, evaluationErrorReason: String)

    fun deleteAllByOptimizationRunId(optimizationRunId: Long)
}