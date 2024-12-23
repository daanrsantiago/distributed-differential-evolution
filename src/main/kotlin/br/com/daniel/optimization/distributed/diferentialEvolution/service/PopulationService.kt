package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class PopulationService(
    val populationRepository: PopulationRepository,
    val chromosomeRepository: ChromosomeRepository
) {

    fun getPopulation(populationId: Long): Population {
        val populationData = populationRepository
            .findById(populationId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Population with id $populationId not found"
                    )
                )
            }!!
        return Population(populationData)
    }

    fun getPopulationsPage(pageable: Pageable): Page<Population> {
        return populationRepository.findAll(pageable).map { Population(it) }
    }

    fun getPopulationsPageByOptimizationRunId(optimizationRunId: Long, pageable: Pageable): Page<Population> {
        return populationRepository.findAllByOptimizationRunId(optimizationRunId, pageable).map { Population(it) }
    }

    fun savePopulation(population: Population): Population {
        val savedPopulationData = populationRepository.save(population.toPopulationData())
        savedPopulationData.members?.forEach {
            it.populationId = savedPopulationData.id
            chromosomeRepository.save(it)
        }
        return Population(savedPopulationData)
    }

    fun addChromosomesRemainingToBeEvaluatedBy(populationId: Long, value: Int): Int {
        return populationRepository.addChromosomesRemainingToBeEvaluatedBy(populationId, value)
    }

    fun removeChromosomesRemainingToBeEvaluatedBy(populationId: Long, value: Int): Int {
        return populationRepository.removeChromosomesRemainingToBeEvaluatedBy(populationId, value)
    }

    fun getChromosomesRemainingToBeEvaluated(populationId: Long): Int {
        return populationRepository.getChromosomesRemainingToBeEvaluated(populationId)
    }

    fun allChromosomesAreEvaluated(populationId: Long): Boolean {
        return populationRepository.allChromosomesAreEvaluated(populationId)
    }

    fun deleteAllByOptimizationRunId(optimizationRunId: Long) {
        chromosomeRepository.deleteAllByOptimizationRunId(optimizationRunId)
    }
}