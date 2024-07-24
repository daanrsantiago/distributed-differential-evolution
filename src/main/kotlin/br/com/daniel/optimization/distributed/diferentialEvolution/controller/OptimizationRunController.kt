package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.CreateOptimizationRunRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus.FINISHED
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ObjectiveFunctionRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.service.ChromosomeService
import br.com.daniel.optimization.distributed.diferentialEvolution.service.OptimizationRunService
import br.com.daniel.optimization.distributed.diferentialEvolution.service.PopulationService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(path = ["/optimizationRun"])
class OptimizationRunController(
    val populationService: PopulationService,
    val optimizationRunService: OptimizationRunService,
    val objectiveFunctionRepository: ObjectiveFunctionRepository,
    val populationRepository: PopulationRepository,
    val chromosomeService: ChromosomeService
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{optimizationRunId}")
    fun getOptimizationRunById(@PathVariable optimizationRunId: Long): ResponseEntity<GetOptimizationRunResponse> {
        val optimizationRun = optimizationRunService.getOptimizationRun(optimizationRunId)
        return ResponseEntity.ok(GetOptimizationRunResponse(optimizationRun))
    }

    @GetMapping("/{optimizationRunId}/populations")
    fun getOptimizationRunPopulationsById(@PathVariable optimizationRunId: Long, pageable: Pageable): ResponseEntity<Page<GetOptimizationRunPopulationsResponse>> {
        val optimizationRunPopulationsPage = populationService.getPopulationPageByOptimizationRunId(optimizationRunId, pageable)
        return ResponseEntity.ok(optimizationRunPopulationsPage.map { GetOptimizationRunPopulationsResponse(it) })
    }

    @PostMapping
    fun createOptimizationRun(
        @RequestBody
        createOptimizationRunRequest: CreateOptimizationRunRequest
    ): ResponseEntity<CreateOptimizationRunResponse> {
        checksIfObjectiveFunctionExists(createOptimizationRunRequest.objectiveFunctionId)
        var optimizationRun = createOptimizationRunRequest.toOptimizationRun()
        optimizationRun = optimizationRunService.saveOptimizationRun(optimizationRun)

        var initialPopulation = optimizationRunService.createInitialPopulation(optimizationRun)
        initialPopulation = populationService.savePopulation(initialPopulation)

        val experimentalChromosomes = optimizationRunService
            .createExperimentalChromosomes(optimizationRun, initialPopulation.populationMembers)
        chromosomeService.saveChromosomes(experimentalChromosomes)

        return ResponseEntity
            .status(CREATED)
            .body(CreateOptimizationRunResponse(optimizationRun))
    }

    private fun checksIfObjectiveFunctionExists(objectiveFunctionId: Long) {
        if (!objectiveFunctionRepository.existsById(objectiveFunctionId)) {
            throw RestHandledException(
                ErrorResponse(
                    NOT_FOUND.value(),
                    "Objective function with id $objectiveFunctionId not found"
                )
            )
        }
    }

    @GetMapping("/{optimizationRunId}/chromosome/notEvaluated")
    fun getNotEvaluatedChromosome(
        @PathVariable("optimizationRunId") optimizationRunId: Long
    ): ResponseEntity<OptimizationRunChromosomeResponse> {
        val optimizationRun = optimizationRunService.getOptimizationRun(optimizationRunId)
        if (optimizationRun.status == FINISHED) {
            return ResponseEntity.ok(
                OptimizationRunChromosomeResponse(
                    optimizationRunId = optimizationRun.id!!,
                    optimizationStatus = optimizationRun.status
                )
            )
        }

        val notEvaluatedChromosomeData = chromosomeService.getChromosomeForEvaluation(optimizationRunId)
        val chromosomeResponse = ChromosomeResponse(notEvaluatedChromosomeData)

        val optimizationRunChromosomeResponse = OptimizationRunChromosomeResponse(
            optimizationRunId = optimizationRun.id!!,
            optimizationStatus = optimizationRun.status,
            chromosome = chromosomeResponse
        )
        return ResponseEntity.ok(optimizationRunChromosomeResponse)
    }

}