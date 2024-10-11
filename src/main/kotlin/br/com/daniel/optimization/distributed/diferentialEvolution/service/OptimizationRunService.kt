@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.ERROR
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATED
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ObjectiveFunctionRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZoneId
import java.time.ZonedDateTime

@Service
class OptimizationRunService(
    val optimizationRunRepository: OptimizationRunRepository,
    val populationService: PopulationService,
    val objectiveFunctionRepository: ObjectiveFunctionRepository
) {
    @set: Autowired
    lateinit var chromosomeService: ChromosomeService

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getOptimizationRunsPage(pageable: Pageable): Page<OptimizationRun> {
        return optimizationRunRepository.findAll(pageable)
            .map { OptimizationRun(it) }
    }

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

    fun createOptimizationRun(optimizationRun: OptimizationRun): OptimizationRun {
        checksIfObjectiveFunctionExists(optimizationRun.objectiveFunctionId)
        val savedOptimizationRun = saveOptimizationRun(optimizationRun)

        var initialPopulation = createInitialPopulation(savedOptimizationRun)
        initialPopulation = populationService.savePopulation(initialPopulation)

        val experimentalChromosomes = createExperimentalChromosomes(savedOptimizationRun, initialPopulation)
        chromosomeService.saveChromosomes(experimentalChromosomes)
        populationService.addChromosomesRemainingToBeEvaluatedBy(initialPopulation.id!!, experimentalChromosomes.size)

        return savedOptimizationRun
    }

    private fun checksIfObjectiveFunctionExists(objectiveFunctionId: Long) {
        if (!objectiveFunctionRepository.existsById(objectiveFunctionId)) {
            throw RestHandledException(
                ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Objective function with id $objectiveFunctionId not found"
                )
            )
        }
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
            chromosomesRemainingToBeEvaluated = optimizationRun.populationSize,
            members = populationMembers
        )
    }

    fun createRandomTargetChromosome(optimizationRun: OptimizationRun, generation: Int = 1): Chromosome {
        val chromosomeElements = optimizationRun.createRandomChromosomeElementsValues()
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
        basedPopulation: Population
    ): List<Chromosome> {
        return basedPopulation.members.map { targetChromosome ->
            createExperimentalChromosome(
                targetChromosome,
                basedPopulation,
                optimizationRun
            )
        }
    }

    fun createExperimentalChromosome(
        targetChromosome: Chromosome,
        basedPopulation: Population,
        optimizationRun: OptimizationRun
    ): Chromosome {
        val donorChromosome = createDonorChromosome(
            basedPopulation.members,
            optimizationRun.perturbationFactor,
            optimizationRun
        )
        val experimentalChromosomeElements = targetChromosome.elements
            .mapIndexed { targetChromosomeElementIndex, targetChromosomeElement ->
                if (Math.random() < optimizationRun.crossoverProbability) {
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

    fun advanceGenerationOrStopOptimizationIfNecessary(
        optimizationRun: OptimizationRun,
        population: Population,
        allPopulationChromosomesInFinalStatus: Boolean
    ) {
        if (allPopulationChromosomesInFinalStatus) {
            logger.info("All chromosomes from population with id ${population.id} and generation ${population.generation} are in final status")
            val shouldStopOptimizationRun = checkForStopCriteria(optimizationRun)
            if (!shouldStopOptimizationRun) {
                advanceGeneration(optimizationRun, population)
            }
        }
    }

    fun advanceGeneration(optimizationRun: OptimizationRun, population: Population) {
        incrementOptimizationRunCurrentGeneration(optimizationRun.id!!)
        val nextGeneration = optimizationRun.currentGeneration + 1
        logger.info("Advancing generation to $nextGeneration on optimizationRun with id ${optimizationRun.id}")
        var newPopulation = Population(
            optimizationRunId = optimizationRun.id,
            generation = nextGeneration,
            chromosomesRemainingToBeEvaluated = optimizationRun.populationSize,
            members = performSelection(optimizationRun, population, nextGeneration).toMutableList()
        )
        newPopulation = populationService.savePopulation(newPopulation)
        val newExperimentalChromosomes = createExperimentalChromosomes(optimizationRun, newPopulation)
        chromosomeService.saveChromosomes(newExperimentalChromosomes)
    }

    fun performSelection(
        optimizationRun: OptimizationRun,
        population: Population,
        newGeneration: Int,
    ): List<Chromosome> {
        logger.info("Performing selection on population with id ${population.id} and optimizationRun with id ${optimizationRun.id}")
        val experimentalChromosomes = chromosomeService.getExperimentalChromosomesByTargetPopulationId(population.id!!)
        val targetChromosomes = population.members
        val selectedChromosomes = targetChromosomes.map { targetChromosome ->
            val targetChromosomeExperimentalChromosomes = experimentalChromosomes.filter { it.targetChromosomeId == targetChromosome.id }
            if (targetChromosome.evaluationStatus == ERROR || targetChromosomeExperimentalChromosomes.all { it.evaluationStatus == ERROR }) {
                return@map selectChromosomeWithNoError(targetChromosome, targetChromosomeExperimentalChromosomes, newGeneration)
            } else {
                val evaluatedExperimentalChromosome = targetChromosomeExperimentalChromosomes.first { it.evaluationStatus == EVALUATED }
                return@map selectChromosomeWithLeastFitness(targetChromosome, evaluatedExperimentalChromosome, newGeneration)
            }
        }
        return selectedChromosomes
    }
    
    private fun selectChromosomeWithNoError(
        targetChromosome: Chromosome,
        targetChromosomeExperimentalChromosomes: List<Chromosome>,
        newGeneration: Int
    ): Chromosome {
        if (targetChromosome.evaluationStatus == ERROR) {
            return targetChromosomeExperimentalChromosomes.first { it.evaluationStatus == EVALUATED }
                .copy(
                    id = null,
                    populationId = null,
                    generation = newGeneration,
                    targetChromosomeId = null,
                    targetPopulationId = null,
                    type = TARGET
                )
        } else {
            return targetChromosome.copy(
                id = null,
                populationId = null,
                generation = newGeneration,
                type = TARGET
            )
        }
    }

    private fun selectChromosomeWithLeastFitness(
        targetChromosome: Chromosome,
        evaluatedExperimentalChromosome: Chromosome,
        newGeneration: Int
    ): Chromosome {
        if (targetChromosome.fitness!! < evaluatedExperimentalChromosome.fitness!!) {
            return targetChromosome.copy(
                id = null,
                populationId = null,
                generation = newGeneration,
                type = TARGET
            )
        } else {
            return evaluatedExperimentalChromosome.copy(
                id = null,
                populationId = null,
                generation = newGeneration,
                targetChromosomeId = null,
                targetPopulationId = null,
                type = TARGET
            )
        }
    }


    fun substituteBestSoFarChromosomeIfNecessary(optimizationRun: OptimizationRun, chromosome: Chromosome) {
        if (optimizationRun.bestSoFarChromosome == null) {
            optimizationRunRepository.setOptimizationRunBestSoFarChromosome(optimizationRun.id!!, chromosome.toChromosomeData())
        } else {
            val rowsAffected = optimizationRunRepository.updateOptimizationRunBestSoFarChromosome(
                optimizationRun.id!!,
                chromosome.toChromosomeData(),
                chromosome.fitness!!
            )
            if (rowsAffected == 1) {
                logger.info(
                    """Chromosome with id ${chromosome.id} is the new best so far for optimizationRun with id ${optimizationRun.id}
                elements: ${chromosome.elements}
                fitness: ${chromosome.fitness}"""
                )
            }
        }
    }

    fun checkForStopCriteria(optimizationRun: OptimizationRun): Boolean {
        if (optimizationRun.maxGenerations == optimizationRun.currentGeneration) {
            optimizationRun.status = OptimizationStatus.FINISHED
            optimizationRun.finishedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            optimizationRun.timeToFinishInSeconds =
                optimizationRun.finishedAt!!.toEpochSecond() - optimizationRun.createdAt!!.toEpochSecond()
            logger.info("Changing status of optimizationRun with id ${optimizationRun.id} to FINISHED. " +
                    "Time to finish ${optimizationRun.timeToFinishInSeconds} seconds")
            optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
            return true
        }
        return false
    }

    fun saveOptimizationRun(optimizationRun: OptimizationRun): OptimizationRun {
        val savedOptimizationRun = optimizationRunRepository.save(optimizationRun.toOptimizationRunData())
        return OptimizationRun(savedOptimizationRun)
    }

    fun incrementOptimizationRunCurrentGeneration(optimizationRunId: Long) {
        optimizationRunRepository.incrementOptimizationRunCurrentGeneration(optimizationRunId);
    }

    @Transactional
    fun deleteOptimizationRun(optimizationRunId: Long) {
        optimizationRunRepository.deleteById(optimizationRunId)
        populationService.deleteAllByOptimizationRunId(optimizationRunId)
        chromosomeService.deleteAllByOptimizationRunId(optimizationRunId)
    }

}