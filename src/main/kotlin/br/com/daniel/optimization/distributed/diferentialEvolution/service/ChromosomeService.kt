package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.ERROR
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATED
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

@Service
class ChromosomeService(
    val chromosomeRepository: ChromosomeRepository,
    val populationService: PopulationService
) {
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
        val chromosomeData = chromosomeRepository
            .getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId)
            .ifEmpty {
                logger.info("No chromosome found for evaluation on optimizationRun with id $optimizationRunId yet")
                throw RestHandledException(
                    ErrorResponse(
                        HttpStatus.PROCESSING.value(),
                        "No chromosome found for evaluation on optimizationRun with id $optimizationRunId yet, comeback later."
                    )
                )
            }.first()
        chromosomeData.evaluationStatus = EvaluationStatus.EVALUATING
        chromosomeData.evaluationId = UUID.randomUUID().toString()
        chromosomeData.evaluationBeginAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        chromosomeRepository.save(chromosomeData)
        logger.info("Chromosome with id ${chromosomeData.id} from optimizationRun with id ${chromosomeData.optimizationRunId} changed evaluationStatus to EVALUATING")
        return Chromosome(chromosomeData)
    }

    fun saveEvaluatedChromosome(chromosome: Chromosome, fitness: Double): Chromosome {
        logger.info("""Chromosome with id ${chromosome.id} from optimizationRun ${chromosome.optimizationRunId} changedEvaluationStatus to EVALUATED
                elements: ${chromosome.elements}
                fitness $fitness""".trimIndent())
        chromosome.fitness = fitness
        chromosome.evaluationStatus = EvaluationStatus.EVALUATED
        chromosome.evaluatedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        chromosomeRepository.save(chromosome.toChromosomeData())

        val populationId = chromosome.populationId
        val targetPopulationId = chromosome.targetPopulationId
        val populationIdToLook = if (chromosome.type == TARGET) populationId!! else targetPopulationId!!

        val optimizationRun = optimizationRunService.getOptimizationRun(chromosome.optimizationRunId!!)
        optimizationRunService.substituteBestSoFarChromosomeIfNecessary(optimizationRun, chromosome)

        val population = populationService.getPopulation(populationIdToLook)

        optimizationRunService.advanceGenerationOrStopOptimizationIfNeeded(optimizationRun, population)

        return chromosome
    }

    fun saveChromosome(chromosome: Chromosome): Chromosome {
        val savedChromosomeData = chromosomeRepository.save(chromosome.toChromosomeData())
        return Chromosome(savedChromosomeData)
    }

    fun saveChromosomes(chromosomes: List<Chromosome>): List<Chromosome> {
        val savedChromosomesData = chromosomeRepository.saveAll(chromosomes.map { it.toChromosomeData() })
        return savedChromosomesData.map { Chromosome(it) }
    }

    fun registerChromosomeError(chromosome: Chromosome) {
        logger.info("Chromosome with id ${chromosome.id} from optimizationRun with id ${chromosome.optimizationRunId} changed evaluationStatus to ERROR")
        chromosome.evaluationStatus = ERROR
        chromosomeRepository.save(chromosome.toChromosomeData())

        if(chromosome.type == EXPERIMENTAL) {
            val targetChromosome = getChromosome(chromosome.targetChromosomeId!!)

            if(targetChromosome.evaluationStatus == ERROR) {
                createAndSaveExperimentalChromosome(targetChromosome)
            } else if (targetChromosome.evaluationStatus == EVALUATED){
                advanceGenerationOrStopOptimizationIfNeeded(targetChromosome)
            }
        } else if(chromosome.type == TARGET) {
            val experimentalChromosomes = getExperimentalChromosomesByTargetChromosomeId(chromosome.id!!)

            if (experimentalChromosomes.all { it.evaluationStatus == ERROR }) {
                createAndSaveExperimentalChromosome(chromosome)
            } else if (experimentalChromosomes.any { it.evaluationStatus == EVALUATED }) {
                advanceGenerationOrStopOptimizationIfNeeded(chromosome)
            }
        }

    }

    private fun advanceGenerationOrStopOptimizationIfNeeded(targetChromosome: Chromosome) {
        val optimizationRun = optimizationRunService.getOptimizationRun(targetChromosome.optimizationRunId!!)
        val population = populationService.getPopulation(targetChromosome.populationId!!)
        optimizationRunService.advanceGenerationOrStopOptimizationIfNeeded(optimizationRun, population)
    }

    private fun createAndSaveExperimentalChromosome(targetChromosome: Chromosome) {
        logger.info("Creating new experimental chromosome for target chromosome with id ${targetChromosome.id}")
        val optimizationRun = optimizationRunService.getOptimizationRun(targetChromosome.optimizationRunId!!)
        val population = populationService.getPopulation(targetChromosome.populationId!!)
        val newExperimentalChromosome =
            optimizationRunService.createExperimentalChromosome(targetChromosome, population.members, optimizationRun)
        this.saveChromosome(newExperimentalChromosome)
    }

    fun areAllChromosomesInFinalStatus(populationId: Long): Boolean {
        return chromosomeRepository.areAllChromosomesStatusEvaluatedOrError(populationId)
    }

}