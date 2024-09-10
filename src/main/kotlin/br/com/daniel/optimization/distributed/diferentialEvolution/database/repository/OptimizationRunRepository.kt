package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface OptimizationRunRepository: CrudRepository<OptimizationRunData, Long> {

    fun findAllByStatus(status: OptimizationStatus): List<OptimizationRunData>

    @Transactional
    @Query("UPDATE OptimizationRunData o SET o.currentGeneration = o.currentGeneration + 1 WHERE o.id = :optimizationRunId")
    @Modifying
    fun incrementOptimizationRunCurrentGeneration(optimizationRunId: Long)

    @Transactional
    @Query( "UPDATE OptimizationRunData o SET o.bestSoFarChromosome = :chromosomeData WHERE o.id = :optimizationRunId")
    @Modifying
    fun setOptimizationRunBestSoFarChromosome(optimizationRunId: Long, chromosomeData: ChromosomeData)

    @Transactional
    @Query( "UPDATE OptimizationRunData o SET o.bestSoFarChromosome = :chromosomeData WHERE o.id = :optimizationRunId AND o.bestSoFarChromosome.fitness > :chromosomeDataFitness")
    @Modifying
    fun updateOptimizationRunBestSoFarChromosome(optimizationRunId: Long, chromosomeData: ChromosomeData, chromosomeDataFitness: Double): Int
}
