package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.ERROR
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATED
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
            val chromosome = createRandomTargetChromosome(optimizationRun, generation = 1)
            populationMembers.add(chromosome)
        }

        return Population(
            optimizationRunId = optimizationRun.id,
            generation = 1,
            members = populationMembers
        )
    }

    fun createRandomTargetChromosome(optimizationRun: OptimizationRun, generation: Int = 1): Chromosome {
        val chromosomeElements = mutableListOf<Double>()
        for (chromosomeElementDetails in optimizationRun.chromosomeElementsDetails) {
            val chromosomeElementValue = chromosomeElementDetails.lowerBoundary +
                    Math.random() * (chromosomeElementDetails.upperBoundary - chromosomeElementDetails.lowerBoundary)
            chromosomeElements.add(chromosomeElementValue)
        }
        return Chromosome(
            optimizationRunId = optimizationRun.id,
            objectiveFunctionId = optimizationRun.objectiveFunctionId,
            type = TARGET,
            elements = chromosomeElements,
            generation = generation
        )
    }

    fun createExperimentalChromosomes(
        optimizationRun: OptimizationRun,
        populationMembers: List<Chromosome>
    ): List<Chromosome> {
        return populationMembers.map { targetChromosome ->
            createExperimentalChromosome(
                targetChromosome,
                populationMembers,
                optimizationRun
            )
        }
    }

    fun createExperimentalChromosome(
        targetChromosome: Chromosome,
        basedPopulation: Population,
        optimizationRun: OptimizationRun
    ): Chromosome {
        val donorChromosome =
            createDonorChromosome(basedPopulation.members, optimizationRun.perturbationFactor, optimizationRun)
        val experimentalChromosomeElements =
            targetChromosome.elements.mapIndexed { targetChromosomeElementIndex, targetChromosomeElement ->
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
            generation = basedPopulation.generation
        )
    }

    private fun createDonorChromosome(
        populationMembers: List<Chromosome>,
        perturbationFactor: Double,
        optimizationRun: OptimizationRun
    ): Chromosome {
        val donorChromosomeElements =
            optimizationRun.strategy.createDonorChromosomeElements(populationMembers, perturbationFactor)
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

    fun advanceGenerationOrStopOptimizationIfNeeded(
        optimizationRun: OptimizationRun,
        population: Population
    ) {
        val allPopulationChromosomesInFinalStatus = chromosomeService.areAllChromosomesInFinalStatus(population.id!!)
        if (allPopulationChromosomesInFinalStatus) {
            val shouldStopOptimizationRun = checkForStopCriteria(optimizationRun)
            if (!shouldStopOptimizationRun) {
                advanceGeneration(optimizationRun, population)
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
            members = performSelection(optimizationRun, population).toMutableList()
        )
        newPopulation = populationService.savePopulation(newPopulation)
        val newExperimentalChromosomes = createExperimentalChromosomes(optimizationRun, newPopulation.members)
        chromosomeService.saveChromosomes(newExperimentalChromosomes)
    }

    fun performSelection(
        optimizationRun: OptimizationRun,
        population: Population,
    ): List<Chromosome> {
        logger.info("Performing selection on population with id ${population.id} and optimizationRun with id ${optimizationRun.id}")
        val newGeneration = optimizationRun.currentGeneration
        val experimentalChromosomes = chromosomeService.getExperimentalChromosomesByTargetPopulationId(population.id!!)
        val targetChromosomes = population.members
        val selectedChromosomes = targetChromosomes.map { targetChromosome ->
            val targetChromosomeExperimentalChromosomes =
                experimentalChromosomes.filter { it.targetChromosomeId == targetChromosome.id }
            if (targetChromosome.evaluationStatus == ERROR) {
                return@map targetChromosomeExperimentalChromosomes.first { it.evaluationStatus == EVALUATED }
                    .copy(
                        id = null,
                        populationId = null,
                        generation = newGeneration,
                        targetChromosomeId = null,
                        targetPopulationId = null,
                        type = TARGET
                    )
            } else if (targetChromosomeExperimentalChromosomes.all { it.evaluationStatus == ERROR }) {
                return@map targetChromosome.copy(
                    id = null,
                    populationId = null,
                    generation = newGeneration,
                    type = TARGET
                )
            } else {
                val evaluatedExperimentalChromosome =
                    targetChromosomeExperimentalChromosomes.first { it.evaluationStatus == EVALUATED }
                if (targetChromosome.fitness!! < evaluatedExperimentalChromosome.fitness!!) {
                    return@map targetChromosome.copy(
                        id = null,
                        populationId = null,
                        generation = newGeneration,
                        type = TARGET
                    )
                } else {
                    return@map evaluatedExperimentalChromosome.copy(
                        id = null,
                        populationId = null,
                        generation = newGeneration,
                        targetChromosomeId = null,
                        targetPopulationId = null,
                        type = TARGET
                    )
                }
            }
        }
        return selectedChromosomes
    }


    fun substituteBestSoFarChromosomeIfNecessary(optimizationRun: OptimizationRun, chromosome: Chromosome) {
        if (optimizationRun.bestSoFarChromosome == null || (chromosome.fitness!! < optimizationRun.bestSoFarChromosome!!.fitness!!)) {
            logger.info(
                """Chromosome with id ${chromosome.id} is the new best so far for optimizationRun with id ${optimizationRun.id}
                elements: ${chromosome.elements}
                fitness: ${chromosome.fitness}"""
            )
            optimizationRun.bestSoFarChromosome = chromosome
            optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
        }
    }

    fun checkForStopCriteria(optimizationRun: OptimizationRun): Boolean {
        if (optimizationRun.maxGenerations == optimizationRun.currentGeneration) {
            logger.info("Changing status of optimizationRun with id ${optimizationRun.id} to FINISHED ")
            optimizationRun.status = OptimizationStatus.FINISHED
            optimizationRun.finishedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            optimizationRun.timeToFinishInSeconds =
                optimizationRun.finishedAt!!.toEpochSecond() - optimizationRun.createdAt!!.toEpochSecond()
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