package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATING
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationRunData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.OptimizationStatus.FINISHED
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ObjectiveFunctionRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.CREATED
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
import java.util.UUID

@RestController
@RequestMapping(path = ["/optimizationRun"])
class OptimizationRunController(
    val optimizationRunRepository: OptimizationRunRepository,
    val objectiveFunctionRepository: ObjectiveFunctionRepository,
    val populationRepository: PopulationRepository,
    val chromosomeRepository: ChromosomeRepository
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{optimizationRunId}")
    fun getOptimizationRunById(@PathVariable optimizationRunId: Long): ResponseEntity<GetOptimizationRunResponse> {
        val optimizationRunData = getOptimizationRunData(optimizationRunId)
        return ResponseEntity.ok(GetOptimizationRunResponse(optimizationRunData))
    }

    @GetMapping("/{optimizationRunId}/populations")
    fun getOptimizationRunPopulationsById(@PathVariable optimizationRunId: Long, pageable: Pageable): ResponseEntity<Page<GetOptimizationRunPopulations>> {
        val optimizationRunPopulationsPage = populationRepository.findAllByOptimizationRunId(optimizationRunId, pageable)
        return ResponseEntity.ok(optimizationRunPopulationsPage.map { GetOptimizationRunPopulations(it) })
    }

    @PostMapping
    fun createOptimizationRun(
        @RequestBody
        createOptimizationRunRequest: CreateOptimizationRunRequest
    ): ResponseEntity<CreateOptimizationRunResponse> {
        checksIfObjectiveFunctionExists(createOptimizationRunRequest.objectiveFunctionId)
        var optimizationRunData = createOptimizationRunRequest.toOptimizationRunData()
        optimizationRunData = optimizationRunRepository.save(optimizationRunData)
        val optimizationRun = OptimizationRun(optimizationRunData)

        val initialPopulation = optimizationRun.createInitialPopulation()
        var initialPopulationData = initialPopulation.toPopulationData(optimizationRunData.id!!, optimizationRunData.objectiveFunctionId!!)
        initialPopulationData = populationRepository.save(initialPopulationData)
        fillAndSavePopulationMembersPopulationIds(initialPopulationData, initialPopulation)

        val experimentalChromosomesData = initialPopulation
            .createExperimentalChromosomes(optimizationRun)
            .map { it.toChromosomeData(optimizationRunData.id!!, optimizationRunData.objectiveFunctionId!!) }
        chromosomeRepository.saveAll(experimentalChromosomesData)

        return ResponseEntity
            .status(CREATED)
            .body(CreateOptimizationRunResponse(optimizationRunData))
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

    private fun fillAndSavePopulationMembersPopulationIds(
        initialPopulationData: PopulationData,
        initialPopulation: Population
    ) {
        initialPopulationData.populationMembers!!.forEachIndexed { index, chromosomeData ->
            val initialPopulationMember = initialPopulation.populationMembers[index]
            initialPopulationMember.populationId = initialPopulationData.id
            initialPopulationMember.id = chromosomeData.id

            chromosomeData.populationId = initialPopulationData.id
            chromosomeRepository.save(chromosomeData)
        }
    }

    @GetMapping("/{optimizationRunId}/chromosome/notEvaluated")
    fun getNotEvaluatedChromosome(
        @PathVariable("optimizationRunId") optimizationRunId: Long
    ): ResponseEntity<OptimizationRunChromosomeResponse> {
        val optimizationRunData = getOptimizationRunData(optimizationRunId)
        if (optimizationRunData.status == FINISHED) {
            return ResponseEntity.ok(
                OptimizationRunChromosomeResponse(
                    optimizationRunId = optimizationRunId,
                    optimizationStatus = optimizationRunData.status
                )
            )
        }

        var notEvaluatedChromosomeData = getNotEvaluatedChromosomeData(optimizationRunId)
        notEvaluatedChromosomeData = fillEvaluationDetails(notEvaluatedChromosomeData)
        val chromosomeResponse = ChromosomeResponse(notEvaluatedChromosomeData)

        val optimizationRunChromosomeResponse = OptimizationRunChromosomeResponse(
            optimizationRunId = optimizationRunData.id!!,
            optimizationStatus = optimizationRunData.status,
            chromosome = chromosomeResponse
        )
        return ResponseEntity.ok(optimizationRunChromosomeResponse)
    }

    private fun fillEvaluationDetails(
        notEvaluatedChromosomeData: ChromosomeData
    ): ChromosomeData {
        var notEvaluatedChromosomeData1 = notEvaluatedChromosomeData
        notEvaluatedChromosomeData1.evaluationStatus = EVALUATING
        notEvaluatedChromosomeData1.evaluationId = UUID.randomUUID().toString()
        notEvaluatedChromosomeData1.evaluationBeginAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        notEvaluatedChromosomeData1 = chromosomeRepository.save(notEvaluatedChromosomeData1)
        logger.info("Chromosome with id ${notEvaluatedChromosomeData1.id} from optimizationRun with id ${notEvaluatedChromosomeData.optimizationRunId} changed evaluationStatus to EVALUATING")
        return notEvaluatedChromosomeData1
    }

    private fun getOptimizationRunData(optimizationRunId: Long): OptimizationRunData =
        optimizationRunRepository.findById(optimizationRunId)
            .orElseThrow {
                RestHandledException(
                    ErrorResponse(
                        NOT_FOUND.value(),
                        "OptimizationRun with id $optimizationRunId not found"
                    )
                )
            }

    private fun getNotEvaluatedChromosomeData(optimizationRunId: Long): ChromosomeData {
        val notEvaluatedChromosomesData = chromosomeRepository
            .getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId)
        if (notEvaluatedChromosomesData.isEmpty()) {
            throw RestHandledException(
                ErrorResponse(
                    NOT_FOUND.value(),
                    "No chromosome found for evaluation on optimizationRun with id $optimizationRunId"
                )
            )
        }
        return notEvaluatedChromosomesData.first()
    }
}