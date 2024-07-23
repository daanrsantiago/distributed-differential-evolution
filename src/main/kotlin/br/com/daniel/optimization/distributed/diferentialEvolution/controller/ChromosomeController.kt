package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.ChangeEvaluationResultRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.*
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.service.ChromosomeService
import br.com.daniel.optimization.distributed.diferentialEvolution.service.OptimizationRunService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chromosome")
class ChromosomeController(
    val chromosomeService: ChromosomeService,
    val optimizationRunService: OptimizationRunService,
) {

    @GetMapping("/{chromosomeId}")
    fun getChromosomeById(@PathVariable chromosomeId: Long): ResponseEntity<ChromosomeResponse> {
        val chromosome = chromosomeService.getChromosome(chromosomeId)
        return ResponseEntity.ok(ChromosomeResponse(chromosome))
    }

    @GetMapping("/targetPopulation/{targetPopulationId}")
    fun getExperimentalChromosomesByTargetPopulationId(@PathVariable targetPopulationId: Long, pageable: Pageable): ResponseEntity<Page<ChromosomeResponse>> {
        val experimentalChromosomePage = chromosomeService.getExperimentalChromosomesPageByTargetPopulationId(targetPopulationId, pageable)
        return ResponseEntity.ok(experimentalChromosomePage.map { ChromosomeResponse(it) })
    }

    @PostMapping("/{chromosomeId}/evaluationResult")
    fun changeEvaluationResult(
        @PathVariable
        chromosomeId: Long,
        @RequestBody
        changeEvaluationResultRequest: ChangeEvaluationResultRequest
    ): ResponseEntity<ChromosomeResponse> {
        var chromosome = chromosomeService.getChromosome(chromosomeId)

        checkIfChromosomeIsEvaluatingOrTimeout(chromosome)
        checkIfEvaluationIdIsTheSame(chromosome, changeEvaluationResultRequest.evaluationId)
        chromosome = chromosomeService.saveEvaluatedChromosome(chromosome, changeEvaluationResultRequest.fitness)

        return ResponseEntity.ok(ChromosomeResponse(chromosome))
    }

    private fun checkIfChromosomeIsEvaluatingOrTimeout(chromosome: Chromosome) {
        if (chromosome.evaluationStatus != EVALUATING && chromosome.evaluationStatus != TIMEOUT) {
            throw RestHandledException(ErrorResponse(NOT_FOUND.value(), "Chromosome with id ${chromosome.id} is not being evaluating"))
        }
    }

    private fun checkIfEvaluationIdIsTheSame(chromosome: Chromosome, requestEvaluationId: String) {
        if (chromosome.evaluationId != requestEvaluationId) {
            throw RestHandledException(ErrorResponse(BAD_REQUEST.value(), "Provided evaluationId is not the same as current chromosome evaluationId"))
        }
    }

}