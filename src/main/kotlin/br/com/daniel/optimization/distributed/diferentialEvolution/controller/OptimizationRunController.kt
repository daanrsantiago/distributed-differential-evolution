package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.CreateOptimizationRunRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.request.PatchOptimizationRunRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus.FINISHED
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus.RUNNING
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.ServiceException
import br.com.daniel.optimization.distributed.diferentialEvolution.service.ChromosomeService
import br.com.daniel.optimization.distributed.diferentialEvolution.service.OptimizationRunService
import br.com.daniel.optimization.distributed.diferentialEvolution.service.PopulationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path = ["/optimizationRun"])
class OptimizationRunController(
    val populationService: PopulationService,
    val optimizationRunService: OptimizationRunService,
    val chromosomeService: ChromosomeService
) {

    @GetMapping("/")
    fun getOptimizationRunsPage(pageable: Pageable): ResponseEntity<Page<GetOptimizationRunsPageContentResponse>> {
        val optimizationRunsPage = optimizationRunService.getOptimizationRunsPage(pageable)
        val optimizationRunsContentsPage = optimizationRunsPage.map { GetOptimizationRunsPageContentResponse(it) }
        return ResponseEntity.ok(optimizationRunsContentsPage)
    }

    @GetMapping("/{optimizationRunId}")
    fun getOptimizationRunById(@PathVariable optimizationRunId: Long): ResponseEntity<GetOptimizationRunResponse> {
        val optimizationRun = optimizationRunService.getOptimizationRun(optimizationRunId)
        return ResponseEntity.ok(GetOptimizationRunResponse(optimizationRun))
    }

    @GetMapping("/{optimizationRunId}/populations")
    fun getOptimizationRunPopulationsById(@PathVariable optimizationRunId: Long, pageable: Pageable): ResponseEntity<Page<GetOptimizationRunPopulationsResponse>> {
        val optimizationRunPopulationsPage = populationService.getPopulationsPageByOptimizationRunId(optimizationRunId, pageable)
        return ResponseEntity.ok(optimizationRunPopulationsPage.map { GetOptimizationRunPopulationsResponse(it) })
    }

    @GetMapping("/{optimizationRunId}/chromosome/notEvaluated")
    fun getChromosomeForEvaluation(
        @PathVariable("optimizationRunId") optimizationRunId: Long
    ): ResponseEntity<GetChromosomeForEvaluationResponse> {
        return try {
            val notEvaluatedChromosomeData = chromosomeService.getChromosomeForEvaluation(optimizationRunId)
            val getChromosomeForEvaluationResponse = GetChromosomeForEvaluationResponse(
                optimizationRunId = optimizationRunId,
                optimizationStatus = RUNNING,
                chromosome = ChromosomeResponse(notEvaluatedChromosomeData)
            )
            ResponseEntity.ok(getChromosomeForEvaluationResponse)
        } catch (optimizationRunFinishedException: ServiceException.OptimizationRunFinishedException) {
            val getChromosomeForEvaluationResponse = GetChromosomeForEvaluationResponse(
                optimizationRunId = optimizationRunId,
                optimizationStatus = FINISHED
            )
            ResponseEntity.ok(getChromosomeForEvaluationResponse)
        } catch (noChromosomeFoundForEvaluation: ServiceException.NoChromosomeFoundForEvaluation) {
            throw RestHandledException(
                ErrorResponse(
                    404,
                    "No chromosome found for evaluation on optimizationRun with id $optimizationRunId yet, comeback later."
                )
            )
        }
    }

    @PostMapping
    fun createOptimizationRun(
        @RequestBody
        createOptimizationRunRequest: CreateOptimizationRunRequest
    ): ResponseEntity<CreateOptimizationRunResponse> {
        val optimizationRun = optimizationRunService.createOptimizationRun(createOptimizationRunRequest.toOptimizationRun())
        return ResponseEntity
            .status(CREATED)
            .body(CreateOptimizationRunResponse(optimizationRun))
    }

    @PatchMapping("/{optimizationRunId}")
    fun patchOptimizationRun(
        @PathVariable("optimizationRunId") optimizationRunId: Long,
        @RequestBody patchOptimizationRunRequest: PatchOptimizationRunRequest
    ): ResponseEntity<GetOptimizationRunResponse> {
        val optimizationRun = optimizationRunService.getOptimizationRun(optimizationRunId)
        val patchedOptimizationRun = patchOptimizationRunRequest.patchOptimizationRun(optimizationRun)
        optimizationRunService.saveOptimizationRun(patchedOptimizationRun)
        return ResponseEntity.ok(GetOptimizationRunResponse(patchedOptimizationRun))
    }

    @DeleteMapping("/{optimizationRunId}")
    fun deleteOptimizationRun(@PathVariable("optimizationRunId") optimizationRunId: Long): ResponseEntity<Any> {
        optimizationRunService.deleteOptimizationRun(optimizationRunId)
        return ResponseEntity.ok().build()
    }

}