package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.transaction.annotation.Transactional

interface PopulationRepository: PagingAndSortingRepository<PopulationData, Long>, CrudRepository<PopulationData, Long> {

    fun findAllByOptimizationRunId(optimizationRunId: Long, pageable: Pageable): Page<PopulationData>

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PopulationData p SET p.chromosomesRemainingToBeEvaluated = p.chromosomesRemainingToBeEvaluated + :value WHERE p.id = :populationId")
    fun addChromosomesRemainingToBeEvaluatedBy(populationId: Long, value: Int): Int

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PopulationData p SET p.chromosomesRemainingToBeEvaluated = p.chromosomesRemainingToBeEvaluated - :value WHERE p.id = :populationId")
    fun removeChromosomesRemainingToBeEvaluatedBy(populationId: Long, value: Int): Int

    @Query("SELECT p.chromosomesRemainingToBeEvaluated FROM PopulationData p WHERE p.id = :populationId")
    fun getChromosomesRemainingToBeEvaluated(populationId: Long): Int

    @Query("SELECT CASE WHEN p.chromosomesRemainingToBeEvaluated = 0 THEN true ELSE false END FROM PopulationData p WHERE p.id = :populationId")
    fun allChromosomesAreEvaluated(populationId: Long): Boolean

    fun deleteAllByOptimizationRunId(optimizationRunId: Long)
}