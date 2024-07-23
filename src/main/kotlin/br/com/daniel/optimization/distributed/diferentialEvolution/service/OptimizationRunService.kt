package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class OptimizationRunService(
    val optimizationRunRepository: OptimizationRunRepository,
    val populationService: PopulationService,
) {
    @set: Autowired
    lateinit var chromosomeService: ChromosomeService

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getOptimizationRun(optimizationRunId: Long): OptimizationRun {
        val optimizationRunData = optimizationRunRepository
            .findById(optimizationRunId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "OptimizationRun with id $optimizationRunId not found"
                    )
                )
            }!!
        return OptimizationRun(optimizationRunData)
    }

    fun createInitialPopulation(optimizationRun: OptimizationRun): Population {
        val populationMembers = mutableListOf<Chromosome>()
        for (iChromosome in 0 until optimizationRun.populationSize) {
            val chromosomeElements = mutableListOf<Double>()
            for(chromosomeElementDetails in optimizationRun.chromosomeElementsDetails) {
                val chromosomeElementValue = chromosomeElementDetails.lowerBoundary +
                        Math.random() * (chromosomeElementDetails.upperBoundary - chromosomeElementDetails.lowerBoundary)
                chromosomeElements.add(chromosomeElementValue)
            }
            val chromosome = Chromosome(
                optimizationRunId = optimizationRun.id,
                objectiveFunctionId = optimizationRun.objectiveFunctionId,
                type = ChromosomeType.TARGET,
                elements = chromosomeElements
            )
            populationMembers.add(chromosome)
        }

        return Population(
            optimizationRunId = optimizationRun.id,
            generation = 1,
            populationMembers = populationMembers
        )
    }

    fun createExperimentalChromosomes(optimizationRun: OptimizationRun, populationMembers: List<Chromosome>): List<Chromosome> {
        return populationMembers.map { targetChromosome ->
            createExperimentalChromosome(
                targetChromosome,
                populationMembers,
                optimizationRun
            )
        }
    }

    private fun createExperimentalChromosome(
        targetChromosome: Chromosome,
        populationMembers: List<Chromosome>,
        optimizationRun: OptimizationRun
    ): Chromosome {
        val donorChromosome = createDonorChromosome(populationMembers, optimizationRun.perturbationFactor, optimizationRun)
        val experimentalChromosomeElements = targetChromosome.elements.mapIndexed { targetChromosomeElementIndex, targetChromosomeElement ->
            if (Math.random() < optimizationRun.crossOverProbability) {
                return@mapIndexed donorChromosome.elements[targetChromosomeElementIndex]
            }
            return@mapIndexed targetChromosomeElement
        }.toMutableList()
        return Chromosome(
            optimizationRunId = optimizationRun.id,
            objectiveFunctionId = optimizationRun.objectiveFunctionId,
            type = EXPERIMENTAL,
            targetChromosomeId = targetChromosome.id,
            targetPopulationId = targetChromosome.populationId,
            elements = experimentalChromosomeElements,
        )
    }

    private fun createDonorChromosome(
        populationMembers: List<Chromosome>,
        perturbationFactor: Double,
        optimizationRun: OptimizationRun
    ): Chromosome {
        val donorChromosomeElements = optimizationRun.strategy.createDonorChromosomeElements(populationMembers, perturbationFactor)
        limitChromosomeElementsToBoundaries(optimizationRun.chromosomeElementsDetails, donorChromosomeElements)
        return Chromosome(
            type = ChromosomeType.DONOR,
            elements = donorChromosomeElements,
        )
    }

    private fun limitChromosomeElementsToBoundaries(
        chromosomeElementDetails: List<ChromosomeElementDetails>,
        donorChromosomeElements: MutableList<Double>
    ) {
        chromosomeElementDetails.forEach {
            if (donorChromosomeElements[it.position] > it.upperBoundary) {
                donorChromosomeElements[it.position] = it.upperBoundary
            } else if (donorChromosomeElements[it.position] < it.lowerBoundary) {
                donorChromosomeElements[it.position] = it.lowerBoundary
            }
        }
    }

    fun advanceGeneration(optimizationRun: OptimizationRun, population: Population) {
        optimizationRun.currentGeneration += 1
        saveOptimizationRun(optimizationRun)
        logger.info("Advancing generation to ${optimizationRun.currentGeneration} on optimizationRun with id ${optimizationRun.id}")
        var newPopulation = Population(
            optimizationRunId = optimizationRun.id,
            generation = optimizationRun.currentGeneration,
            populationMembers = performSelection(optimizationRun, population).toMutableList()
        )
        newPopulation = populationService.savePopulation(newPopulation)
        val newExperimentalChromosomes = createExperimentalChromosomes(optimizationRun, newPopulation.populationMembers)
        chromosomeService.saveChromosomes(newExperimentalChromosomes)
    }

    fun performSelection(
        optimizationRun: OptimizationRun,
        population: Population,
    ): List<Chromosome> {
        logger.info("Performing selection on population with id ${population.id} and optimizationRun with id ${optimizationRun.id}")
        val experimentalChromosomes = chromosomeService.getExperimentalChromosomesByTargetPopulationId(population.id!!)
        val targetChromosomeMap = population.populationMembers.associateBy { it.id!! }
        return experimentalChromosomes.map { experimentalChromosome ->
            val targetChromosome = targetChromosomeMap[experimentalChromosome.targetChromosomeId]
            if (targetChromosome!!.fitness!! < experimentalChromosome.fitness!!) {
                return@map targetChromosome.copy(
                    id = null,
                    populationId = null,
                    type = ChromosomeType.TARGET
                )
            } else {
                return@map experimentalChromosome.copy(
                    id = null,
                    populationId = null,
                    targetChromosomeId = null,
                    targetPopulationId = null,
                    type = ChromosomeType.TARGET
                )
            }
        }
    }

    fun substituteBestSoFarChromosomeIfNecessary(optimizationRun: OptimizationRun, chromosome: Chromosome) {
        if (optimizationRun.bestSoFarChromosome == null || ( chromosome.fitness!! < optimizationRun.bestSoFarChromosome!!.fitness!! )) {
            logger.info("""Chromosome with id ${chromosome.id} is the new best so far for optimizationRun with id ${optimizationRun.id}
                elements: ${chromosome.elements}
                fitness: ${chromosome.fitness}""")
            optimizationRun.bestSoFarChromosome = chromosome
            optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
        }
    }

    fun checkForStopCriteria(optimizationRun: OptimizationRun): Boolean {
        if (optimizationRun.maxGenerations == optimizationRun.currentGeneration) {
            logger.info("Changing status of optimizationRun with id ${optimizationRun.id} to FINISHED ")
            optimizationRun.status = OptimizationStatus.FINISHED
            optimizationRun.finishedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            optimizationRun.timeToFinishInSeconds = optimizationRun.finishedAt!!.toEpochSecond() - optimizationRun.createdAt!!.toEpochSecond()
            optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
            return true
        }
        return false
    }

    fun saveOptimizationRun(optimizationRun: OptimizationRun): OptimizationRun {
        val savedOptimizationRun = optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
        return OptimizationRun(savedOptimizationRun)
    }

}