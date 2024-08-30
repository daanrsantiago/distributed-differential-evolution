package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.PublishEvaluationErrorRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.PublishEvaluationResultRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.ChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.*
import br.com.daniel.optimization.distributed.diferentialEvolution.service.ChromosomeService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/chromosome")
class ChromosomeController(
    val chromosomeService: ChromosomeService
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

    @PostMapping("/{chromosomeId}/evaluationError")
    fun publishEvaluationError(
        @PathVariable chromosomeId: Long,
        @RequestBody publishEvaluationErrorRequest: PublishEvaluationErrorRequest
    ) {
        val chromosome = chromosomeService.getChromosome(chromosomeId)
        val evaluationId = publishEvaluationErrorRequest.evaluationId
        chromosomeService.publishEvaluationError(chromosome, publishEvaluationErrorRequest.reason, evaluationId)
    }

    @PostMapping("/{chromosomeId}/evaluationResult")
    fun publishEvaluationResult(
        @PathVariable chromosomeId: Long,
        @RequestBody publishEvaluationResultRequest: PublishEvaluationResultRequest
    ): ResponseEntity<ChromosomeResponse> {
        var chromosome = chromosomeService.getChromosome(chromosomeId)
        val evaluationId = publishEvaluationResultRequest.evaluationId
        chromosome = chromosomeService.saveEvaluatedChromosome(chromosome, publishEvaluationResultRequest.fitness, evaluationId)
        return ResponseEntity.ok(ChromosomeResponse(chromosome))
    }

}