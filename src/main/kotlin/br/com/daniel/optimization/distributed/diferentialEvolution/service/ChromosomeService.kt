@file:Suppress("SpringJavaInjectionPointsAutowiringInspection")

package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.ServiceException.NoChromosomeFoundForEvaluation
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.ServiceException.OptimizationRunFinishedException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
class ChromosomeService(
    val chromosomeRepository: ChromosomeRepository,
    val populationService: PopulationService,
    platformTransactionManager: PlatformTransactionManager
) {
    private val transactionTemplate = TransactionTemplate(platformTransactionManager)

    @set: Autowired
    lateinit var optimizationRunService: OptimizationRunService

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun getChromosome(chromosomeId: Long): Chromosome {
        val chromosomeData = chromosomeRepository
            .findById(chromosomeId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Chromosome with id $chromosomeId not found"
                    )
                )
            }!!
        return Chromosome(chromosomeData)
    }

    fun getExperimentalChromosomesByTargetChromosomeId(targetChromosomeId: Long): List<Chromosome> {
        return chromosomeRepository.findAllByTargetChromosomeId(targetChromosomeId).map { Chromosome(it) }
    }

    fun getExperimentalChromosomesByTargetPopulationId(targetPopulationId: Long): List<Chromosome> {
        val targetChromosomesData = chromosomeRepository
            .findAllByTargetPopulationIdAndType(targetPopulationId, EXPERIMENTAL)
        return targetChromosomesData.map { Chromosome(it) }
    }

    fun getExperimentalChromosomesPageByTargetPopulationId(targetPopulationId: Long, pageable: Pageable): Page<Chromosome> {
        val experimentalChromosomePage = chromosomeRepository.findAllByTargetPopulationIdAndType(targetPopulationId, EXPERIMENTAL, pageable)
        return experimentalChromosomePage.map { Chromosome(it) }
    }

    fun getChromosomeForEvaluation(optimizationRunId: Long): Chromosome {
        val optimizationRun = optimizationRunService.getOptimizationRun(optimizationRunId)
        checkIfOptimizationHasFinished(optimizationRun, optimizationRunId)
        val chromosome = transactionTemplate.execute {
            val chromosomeData = chromosomeRepository.getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId)
                .ifEmpty { throw NoChromosomeFoundForEvaluation(optimizationRunId) }
                .first()
            val chromosome = Chromosome(chromosomeData)
            checkIfChromosomeIsFromCurrentGeneration(chromosome)
            chromosome.evaluationId = UUID.randomUUID().toString()
            chromosome.evaluationStatus = EVALUATING
            chromosome.evaluationBeginAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            updateBeginEvaluationData(
                chromosomeData.id!!,
                chromosome.evaluationId!!,
                chromosome.evaluationBeginAt!!
            )
            return@execute chromosome
        }!!
        logger.info("Chromosome with id ${chromosome.id}, populationId ${chromosome.populationId}, targetPopulationId ${chromosome.targetPopulationId}, generation ${chromosome.generation} and optimizationRunId ${chromosome.optimizationRunId} changed evaluationStatus to EVALUATING")
        return chromosome
    }

    private fun checkIfOptimizationHasFinished(
        optimizationRun: OptimizationRun,
        optimizationRunId: Long
    ) {
        if (optimizationRun.status == OptimizationStatus.FINISHED) {
            throw OptimizationRunFinishedException(optimizationRunId)
        }
    }

    private fun checkIfChromosomeIsFromCurrentGeneration(chromosome: Chromosome) {
        val optimizationRun = optimizationRunService.getOptimizationRun(chromosome.optimizationRunId!!)
        if (optimizationRun.currentGeneration != chromosome.generation) {
            logger.error("Chromosome with id ${chromosome.id} and status ${chromosome.evaluationStatus} " +
                    "from population with id ${chromosome.populationId}, targetPopulation with id ${chromosome.targetPopulationId} and generation ${chromosome.generation} " +
                    "is trying to change status to EVALUATED but is not from current generation ${optimizationRun.currentGeneration}")
        }
    }

    fun saveEvaluatedChromosome(chromosome: Chromosome, fitness: Double, evaluationId: String): Chromosome {
        checkIfEvaluationIdIsTheSame(chromosome, evaluationId)
        checkIfChromosomeIsFromCurrentGeneration(chromosome)
        checkIfChromosomeAlreadyEvaluated(chromosome)
        checkIfChromosomeIsEvaluatingOrTimeout(chromosome)
        logger.info("""Chromosome with id ${chromosome.id}, populationId ${chromosome.populationId}, targetPopulationId ${chromosome.targetPopulationId}, generation ${chromosome.generation} and optimizationRunId ${chromosome.optimizationRunId} changedEvaluationStatus to EVALUATED
                elements: ${chromosome.elements}
                fitness $fitness""".trimIndent())
        chromosome.fitness = fitness
        chromosome.evaluationStatus = EVALUATED
        chromosome.evaluatedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))

        val allPopulationChromosomesInFinalStatus = transactionTemplate.execute {
            updateEndEvaluationData(chromosome.id!!, fitness, chromosome.evaluatedAt!!)
            populationService.removeChromosomesRemainingToBeEvaluatedBy(chromosome.populationIdAssociated, 1)
            val chromosomesRemainingToBeEvaluated = populationService.getChromosomesRemainingToBeEvaluated(chromosome.populationIdAssociated)
            logger.info("$chromosomesRemainingToBeEvaluated chromosomes remaining to be evaluated for population with id ${chromosome.populationIdAssociated}")
            return@execute chromosomesRemainingToBeEvaluated == 0
        }
        val population = populationService.getPopulation(chromosome.populationIdAssociated)
        val optimizationRun = optimizationRunService.getOptimizationRun(chromosome.optimizationRunId!!)
        optimizationRunService.advanceGenerationOrStopOptimizationIfNecessary(optimizationRun, population, allPopulationChromosomesInFinalStatus!!)
        optimizationRunService.substituteBestSoFarChromosomeIfNecessary(optimizationRun, chromosome)

        return chromosome
    }

    private fun checkIfChromosomeAlreadyEvaluated(chromosome: Chromosome) {
        if(chromosome.evaluationStatus == EVALUATED) {
            logger.error("Chromosome with id ${chromosome.id} already evaluated")
            throw RestHandledException(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Chromosome with id ${chromosome.id} already evaluated"))
        }
    }

    private fun checkIfChromosomeIsEvaluatingOrTimeout(chromosome: Chromosome) {
        if (chromosome.evaluationStatus != EVALUATING && chromosome.evaluationStatus != EvaluationStatus.TIMEOUT) {
            logger.error("Chromosome with id ${chromosome.id} is not being evaluating")
            throw RestHandledException(ErrorResponse(HttpStatus.NOT_FOUND.value(), "Chromosome with id ${chromosome.id} is not being evaluating"))
        }
    }

    private fun checkIfEvaluationIdIsTheSame(chromosome: Chromosome, requestEvaluationId: String) {
        if (chromosome.evaluationId != requestEvaluationId) {
            throw RestHandledException(ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Provided evaluationId is not the same as current chromosome evaluationId"))
        }
    }

    fun saveChromosome(chromosome: Chromosome): Chromosome {
        val savedChromosomeData = chromosomeRepository.save(chromosome.toChromosomeData())
        return Chromosome(savedChromosomeData)
    }

    fun saveChromosomes(chromosomes: List<Chromosome>): List<Chromosome> {
        val savedChromosomesData = chromosomeRepository.saveAll(chromosomes.map { it.toChromosomeData() })
        return savedChromosomesData.map { Chromosome(it) }
    }

    fun publishEvaluationError(chromosome: Chromosome, reason: String, evaluationId: String) {
        checkIfEvaluationIdIsTheSame(chromosome, evaluationId)
        logger.info("Chromosome with id ${chromosome.id} from optimizationRun with id ${chromosome.optimizationRunId} changed evaluationStatus to ERROR with " +
                "reason: $reason")
        chromosome.evaluationStatus = ERROR
        chromosome.evaluationErrorReason = reason
        updateEvaluationError(chromosome.id!!, reason)

        val targetChromosome = if (chromosome.type == TARGET) chromosome else getChromosome(chromosome.targetChromosomeId!!)
        val experimentalChromosomes = if (chromosome.type == EXPERIMENTAL) listOf(chromosome) else getExperimentalChromosomesByTargetChromosomeId(chromosome.id!!)

        if (targetChromosome.evaluationStatus == ERROR && experimentalChromosomes.all { it.evaluationStatus == ERROR }) {
            createAndSaveExperimentalChromosome(targetChromosome)
        } else {
            val allPopulationChromosomesInFinalStatus = transactionTemplate.execute {
                populationService.removeChromosomesRemainingToBeEvaluatedBy(chromosome.populationIdAssociated, 1)
                val chromosomesRemainingToBeEvaluated = populationService.getChromosomesRemainingToBeEvaluated(chromosome.populationIdAssociated)
                logger.info("$chromosomesRemainingToBeEvaluated chromosomes remaining to be evaluated for population with id ${chromosome.populationIdAssociated}")
                return@execute chromosomesRemainingToBeEvaluated == 0
            }
            advanceGenerationOrStopOptimizationIfNeeded(targetChromosome, allPopulationChromosomesInFinalStatus!!)
        }
    }

    private fun advanceGenerationOrStopOptimizationIfNeeded(targetChromosome: Chromosome, allPopulationChromosomesInFinalStatus: Boolean) {
        val optimizationRun = optimizationRunService.getOptimizationRun(targetChromosome.optimizationRunId!!)
        val population = populationService.getPopulation(targetChromosome.populationId!!)
        optimizationRunService.advanceGenerationOrStopOptimizationIfNecessary(optimizationRun, population, allPopulationChromosomesInFinalStatus)
    }

    private fun createAndSaveExperimentalChromosome(targetChromosome: Chromosome) {
        val optimizationRun = optimizationRunService.getOptimizationRun(targetChromosome.optimizationRunId!!)
        val population = populationService.getPopulation(targetChromosome.populationId!!)
        var newExperimentalChromosome = optimizationRunService.createExperimentalChromosome(targetChromosome, population, optimizationRun)
        newExperimentalChromosome = this.saveChromosome(newExperimentalChromosome)
        logger.info("Created new experimental chromosome with id ${newExperimentalChromosome.id}, " +
                "targetChromosomeId ${newExperimentalChromosome.targetChromosomeId},  " +
                "targetPopulationId ${newExperimentalChromosome.targetPopulationId} and " +
                "optimizationRunId ${newExperimentalChromosome.optimizationRunId}")

    }

    fun updateBeginEvaluationData(chromosomeId: Long, evaluationId: String, evaluationBeginAt: ZonedDateTime) {
        chromosomeRepository.updateBeginEvaluationData(chromosomeId, evaluationId, evaluationBeginAt)
    }

    fun updateEndEvaluationData(chromosomeId: Long, fitness: Double, evaluatedAt: ZonedDateTime) {
        chromosomeRepository.updateEndEvaluationData(chromosomeId, fitness, evaluatedAt)
    }

    fun updateEvaluationError(chromosomeId: Long, evaluationErrorReason: String) {
        chromosomeRepository.updateEvaluationErrorData(chromosomeId, evaluationErrorReason)
    }

    fun deleteAllByOptimizationRunId(optimizationRunId: Long) {
        chromosomeRepository.deleteAllByOptimizationRunId(optimizationRunId)
    }

}