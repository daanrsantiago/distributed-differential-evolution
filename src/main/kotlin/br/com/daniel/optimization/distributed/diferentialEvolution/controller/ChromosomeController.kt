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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
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
        val chromosomeData = chromosomeRepository
            .findById(chromosomeId)
            .orElseThrow { RestHandledException(ErrorResponse(NOT_FOUND.value(), "Chromosome with id $chromosomeId not found")) }

        checkIfChromosomeIsEvaluatingOrTimeout(chromosomeData)
        checkIfEvaluationIdIsTheSame(chromosomeData, changeEvaluationResultRequest.evaluationId)
        fillAndSaveChromosomeData(chromosomeData, changeEvaluationResultRequest)

        val optimizationRunData = optimizationRunRepository
            .findById(chromosomeData.optimizationRunId!!)
            .orElseThrow { RestHandledException(ErrorResponse(NOT_FOUND.value(), "OptimizationRun with id ${chromosomeData.optimizationRunId} not found")) }
        substituteBestSoFarChromosomeIfNecessary(optimizationRunData, chromosomeData)

        val populationIdToLook = if (chromosomeData.type == TARGET) chromosomeData.populationId!! else chromosomeData.targetPopulationId!!

        val populationData = populationRepository
            .findById(populationIdToLook)
            .orElseThrow { RestHandledException(ErrorResponse(NOT_FOUND.value(), "Population with id ${chromosomeData.populationId} not found")) }

        val allPopulationChromosomesEvaluated = chromosomeRepository.areAllChromosomesEvaluated(chromosomeData.optimizationRunId)
        val shouldStopOptimizationRun = checkForStopCriteria(optimizationRunData, allPopulationChromosomesEvaluated)

        if (!shouldStopOptimizationRun && allPopulationChromosomesEvaluated) {
            logger.info("Performing selection on population with id $populationIdToLook and optimizationRun with id ${chromosomeData.optimizationRunId}")
            performSelection(optimizationRunData, populationData)
        }

        return ResponseEntity.ok(ChromosomeResponse(chromosomeData))
    }

    private fun fillAndSaveChromosomeData(
        chromosomeData: ChromosomeData,
        changeEvaluationResultRequest: ChangeEvaluationResultRequest
    ) {
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

    private fun substituteBestSoFarChromosomeIfNecessary(optimizationRunData: OptimizationRunData, chromosome: ChromosomeData) {
        if (optimizationRunData.bestSoFarChromosome == null || ( optimizationRunData.bestSoFarChromosome!!.fitness!! < chromosome.fitness!! )) {
            optimizationRunData.bestSoFarChromosome = chromosome
            optimizationRunRepository.save(optimizationRunData)
        }
    }

    private fun checkForStopCriteria(optimizationRunData: OptimizationRunData, allPopulationChromosomesEvaluated: Boolean): Boolean {
        if (optimizationRunData.maxGenerations == optimizationRunData.currentGeneration && allPopulationChromosomesEvaluated) {
            optimizationRunData.status = OptimizationStatus.FINISHED
            optimizationRunRepository.save(optimizationRunData)
            return true
        }
        return false
    }

    private fun performSelection(optimizationRunData: OptimizationRunData, populationData: PopulationData): PopulationData {
        val newPopulationData = populationRepository.save(PopulationData(
            optimizationRunId = optimizationRunData.id,
            generation = optimizationRunData.currentGeneration + 1,
            size = populationData.size
        ))
        logger.info("Saving new population with id ${newPopulationData.id} and optimizationRun with id ${optimizationRunData.id}")
        val currentPopulationChromosomesMap = mapOf(*populationData.populationMembers!!.map { Pair(it.id!!, it) }.toTypedArray())
        val experimentalChromosomes = chromosomeRepository.findAllByTargetPopulationIdAndType(populationData.id, EXPERIMENTAL)

        val selectedChromosomes = experimentalChromosomes.map { experimentalChromosome ->
            var selectedChromosomeData: ChromosomeData
            val targetChromosome = currentPopulationChromosomesMap[experimentalChromosome.targetChromosomeId]
            selectedChromosomeData = if (targetChromosome!!.fitness!! > experimentalChromosome.fitness!!) {
                targetChromosome.copy(
                    id = null,
                    populationId = newPopulationData.id,
                    type = TARGET
                )
            } else {
                experimentalChromosome.copy(
                    id = null,
                    populationId = newPopulationData.id,
                    type = TARGET
                )
            }
            selectedChromosomeData.elements = selectedChromosomeData.elements?.map { it.copy() }?.toMutableList()
            selectedChromosomeData = chromosomeRepository.save(selectedChromosomeData)
            return@map selectedChromosomeData
        }.toMutableList()

        return PopulationData(
            optimizationRunId = optimizationRunData.id,
            generation = optimizationRunData.currentGeneration + 1,
            size = populationData.size,
            populationMembers = selectedChromosomes
        )
   }
}