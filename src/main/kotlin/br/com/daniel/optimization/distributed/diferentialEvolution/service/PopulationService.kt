package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
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

    fun savePopulation(population: Population): Population {
        val savedPopulationData = populationRepository.save(population.toPopulationData())
        savedPopulationData.populationMembers?.forEach {
            it.populationId = savedPopulationData.id
            chromosomeRepository.save(it)
        }
        return Population(savedPopulationData)
    }

}