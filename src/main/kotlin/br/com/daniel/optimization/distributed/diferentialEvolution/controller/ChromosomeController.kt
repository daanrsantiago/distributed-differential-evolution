package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ChangeEvaluationResultRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.EXPERIMENTAL
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.TARGET
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.ZoneId
import java.time.ZonedDateTime

@RestController
@RequestMapping("/chromosome")
class ChromosomeController(
    val chromosomeRepository: ChromosomeRepository,
    val optimizationRunRepository: OptimizationRunRepository,
    val populationRepository: PopulationRepository
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{chromosomeId}")
    fun getChromosomeById(@PathVariable chromosomeId: Long): ResponseEntity<ChromosomeResponse> {
        val chromosome = chromosomeRepository.findById(chromosomeId)
        if (chromosome.isEmpty) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ChromosomeResponse(chromosome.get()))
    }

    @GetMapping("/targetPopulation/{targetPopulationId}")
    fun getExperimentalChromosomesByTargetPopulationId(@PathVariable targetPopulationId: Long, pageable: Pageable): ResponseEntity<Page<ChromosomeResponse>> {
        val experimentalChromosomePage = chromosomeRepository.findAllByTargetPopulationIdAndType(targetPopulationId, EXPERIMENTAL, pageable)
        return ResponseEntity.ok(experimentalChromosomePage.map { ChromosomeResponse(it) })
    }

    @PostMapping("/{chromosomeId}/evaluationResult")
    fun changeEvaluationResult(
        @PathVariable
        chromosomeId: Long,
        @RequestBody
        changeEvaluationResultRequest: ChangeEvaluationResultRequest
    ): ResponseEntity<ChromosomeResponse> {
        val chromosomeData = getChromosomeData(chromosomeId)
        val optimizationRunId = chromosomeData.optimizationRunId!!
        val populationId = chromosomeData.populationId
        val targetPopulationId = chromosomeData.targetPopulationId
        val populationIdToLook = if (chromosomeData.type == TARGET) populationId!! else targetPopulationId!!

        checkIfChromosomeIsEvaluatingOrTimeout(chromosomeData)
        checkIfEvaluationIdIsTheSame(chromosomeData, changeEvaluationResultRequest.evaluationId)
        fillAndSaveChromosomeData(chromosomeData, changeEvaluationResultRequest)

        val optimizationRunData = getOptimizationRunData(optimizationRunId)
        val optimizationRun = OptimizationRun(optimizationRunData)
        substituteBestSoFarChromosomeIfNecessary(optimizationRunData, chromosomeData)

        val populationData = getPopulationData(populationIdToLook)

        val allPopulationChromosomesEvaluated = chromosomeRepository.areAllChromosomesEvaluated(populationIdToLook)
        if (allPopulationChromosomesEvaluated) {
            val shouldStopOptimizationRun = checkForStopCriteria(optimizationRunData)
            if (!shouldStopOptimizationRun) {
                logger.info("Performing selection on population with id $populationIdToLook and optimizationRun with id $optimizationRunId")
                createNewPopulation(populationData, optimizationRun)
                optimizationRunData.currentGeneration += 1
                optimizationRunRepository.save(optimizationRunData)
            }
        }

        return ResponseEntity.ok(ChromosomeResponse(chromosomeData))
    }

    private fun createNewPopulation(
        populationData: PopulationData,
        optimizationRun: OptimizationRun,
    ) {
        val newPopulationData = performSelection(optimizationRun, populationData)
        val newPopulation = Population(newPopulationData)
        val newExperimentalChromosomes = newPopulation.createExperimentalChromosomes(optimizationRun)
        val newExperimentalChromosomesData = newExperimentalChromosomes.map {
            it.toChromosomeData(
                optimizationRun.id,
                optimizationRun.objectiveFunctionId
            )
        }
        chromosomeRepository.saveAll(newExperimentalChromosomesData)
    }

    private fun getChromosomeData(chromosomeId: Long): ChromosomeData =
        chromosomeRepository
            .findById(chromosomeId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        NOT_FOUND.value(),
                        "Chromosome with id $chromosomeId not found"
                    )
                )
            }!!

    private fun getPopulationData(
        populationIdToLook: Long
    ): PopulationData = populationRepository
        .findById(populationIdToLook)
        .orElseThrow {
            RestHandledException(
                ErrorResponse(
                    NOT_FOUND.value(),
                    "Population with id $populationIdToLook not found"
                )
            )
        }!!

    private fun getOptimizationRunData(optimizationRunId: Long): OptimizationRunData =
        optimizationRunRepository
            .findById(optimizationRunId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        NOT_FOUND.value(),
                        "OptimizationRun with id $optimizationRunId not found"
                    )
                )
            }!!

    private fun fillAndSaveChromosomeData(
        chromosomeData: ChromosomeData,
        changeEvaluationResultRequest: ChangeEvaluationResultRequest
    ) {
        logger.info("""Chromosome with id ${chromosomeData.id} from optimizationRun ${chromosomeData.optimizationRunId} changedEvaluationStatus to EVALUATED
                elements: ${chromosomeData.elements?.map { it.value }}
                fitness ${changeEvaluationResultRequest.fitness}""".trimIndent())
        chromosomeData.fitness = changeEvaluationResultRequest.fitness
        chromosomeData.evaluationStatus = EVALUATED
        chromosomeData.evaluatedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        chromosomeRepository.save(chromosomeData)
    }

    private fun checkIfChromosomeIsEvaluatingOrTimeout(chromosomeData: ChromosomeData) {
        if (chromosomeData.evaluationStatus != EVALUATING && chromosomeData.evaluationStatus != TIMEOUT) {
            throw RestHandledException(ErrorResponse(NOT_FOUND.value(), "Chromosome with id ${chromosomeData.id} is not being evaluating"))
        }
    }

    private fun checkIfEvaluationIdIsTheSame(chromosomeData: ChromosomeData, requestEvaluationId: String) {
        if (chromosomeData.evaluationId != requestEvaluationId) {
            throw RestHandledException(ErrorResponse(BAD_REQUEST.value(), "Provided evaluationId is not the same as current chromosome evaluationId"))
        }
    }

    private fun substituteBestSoFarChromosomeIfNecessary(optimizationRunData: OptimizationRunData, chromosomeData: ChromosomeData) {
        if (optimizationRunData.bestSoFarChromosome == null || ( chromosomeData.fitness!! < optimizationRunData.bestSoFarChromosome!!.fitness!! )) {
            logger.info("""Chromosome with id ${chromosomeData.id} is the new best so far for optimizationRun with id ${optimizationRunData.id}
                elements: ${chromosomeData.elements?.map { it.value }}
                fitness: ${chromosomeData.fitness}""")
            optimizationRunData.bestSoFarChromosome = chromosomeData
            optimizationRunRepository.save(optimizationRunData)
        }
    }

    private fun checkForStopCriteria(optimizationRunData: OptimizationRunData): Boolean {
        if (optimizationRunData.maxGenerations == optimizationRunData.currentGeneration) {
            logger.info("Changing status of optimizationRun with id ${optimizationRunData.id} to FINISHED ")
            optimizationRunData.status = OptimizationStatus.FINISHED
            optimizationRunData.finishedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
            optimizationRunData.timeToFinishInSeconds = optimizationRunData.finishedAt!!.toEpochSecond() - optimizationRunData.createdAt.toEpochSecond()
            optimizationRunRepository.save(optimizationRunData)
            return true
        }
        return false
    }

    private fun performSelection(optimizationRun: OptimizationRun, populationData: PopulationData): PopulationData {
        val newPopulationData = populationRepository.save(PopulationData(
            optimizationRunId = optimizationRun.id,
            generation = optimizationRun.currentGeneration + 1,
            size = populationData.size
        ))
        logger.info("Saving new population with id ${newPopulationData.id} and optimizationRun with id ${optimizationRun.id}")
        val experimentalChromosomes = chromosomeRepository.findAllByTargetPopulationIdAndType(populationData.id, EXPERIMENTAL)

        val selectedChromosomes = experimentalChromosomes.mapIndexed { index, experimentalChromosome ->
            var selectedChromosomeData: ChromosomeData
            val targetChromosome = populationData.populationMembers!![index]
            selectedChromosomeData = if (targetChromosome.fitness!! < experimentalChromosome.fitness!!) {
                targetChromosome.copy(
                    id = null,
                    populationId = newPopulationData.id,
                    type = TARGET
                )
            } else {
                experimentalChromosome.copy(
                    id = null,
                    populationId = newPopulationData.id,
                    targetChromosomeId = null,
                    targetPopulationId = null,
                    type = TARGET
                )
            }
            selectedChromosomeData.elements = selectedChromosomeData.elements?.map { it.copy() }?.toMutableList()
            selectedChromosomeData = chromosomeRepository.save(selectedChromosomeData)
            return@mapIndexed selectedChromosomeData
        }.toMutableList()

        return PopulationData(
            optimizationRunId = optimizationRun.id,
            generation = optimizationRun.currentGeneration + 1,
            size = populationData.size,
            populationMembers = selectedChromosomes
        )
   }
}