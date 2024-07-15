package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATING
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
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
    val populationRepository: PopulationRepository,
    val chromosomeRepository: ChromosomeRepository
) {

    val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/{optimizationRunId}")
    fun getOptimizationRunById(@PathVariable optimizationRunId: Long): ResponseEntity<GetOptimizationRunResponse> {
        val optimizationRunData = optimizationRunRepository.findById(optimizationRunId).orElseThrow {
            RestHandledException(ErrorResponse(NOT_FOUND.value(), "OptimizationRun with id $optimizationRunId not found"))
        }
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
        var optimizationRunData = createOptimizationRunRequest.toOptimizationRunData()
        optimizationRunData = optimizationRunRepository.save(optimizationRunData)
        val optimizationRun = OptimizationRun(optimizationRunData)

        val initialPopulation = optimizationRun.createInitialPopulation()
        var initialPopulationData = initialPopulation.toPopulationData(optimizationRunData.id!!, optimizationRunData.objectiveFunctionId!!)
        initialPopulationData = populationRepository.save(initialPopulationData)
        initialPopulationData.populationMembers!!.forEachIndexed { index, chromosomeData ->
            val initialPopulationMember = initialPopulation.populationMembers[index]
            initialPopulationMember.populationId = initialPopulationData.id
            initialPopulationMember.id = chromosomeData.id

            chromosomeData.populationId = initialPopulationData.id
            chromosomeRepository.save(chromosomeData)
        }

        val experimentalChromosomes = initialPopulation
            .createExperimentalChromosomes(optimizationRun.perturbationFactor, optimizationRun.crossOverProbability)
        experimentalChromosomes.forEach {
            chromosomeRepository.save(it.toChromosomeData(optimizationRunData.id!!, optimizationRunData.objectiveFunctionId!!))
        }

        val createOptimizationRunResponse = CreateOptimizationRunResponse(optimizationRunData)
        return ResponseEntity.status(HttpStatus.CREATED).body(createOptimizationRunResponse)
    }

    @GetMapping("/{optimizationRunId}/chromosome/notEvaluated")
    fun getNotEvaluatedChromosome(
        @PathVariable("optimizationRunId") optimizationRunId: Long
    ): ResponseEntity<OptimizationRunChromosomeResponse> {
        val optimizationRunOptional = optimizationRunRepository.findById(optimizationRunId)
        if(optimizationRunOptional.isEmpty) return ResponseEntity.notFound().build()
        val optimizationRun = optimizationRunOptional.get()

        val notEvaluatedChromosomesData = chromosomeRepository
            .getNotEvaluatedChromosomeByOptimizationRunId(optimizationRunId)
        if (notEvaluatedChromosomesData.isEmpty()) return ResponseEntity.notFound().build()

        var chromosomeData = notEvaluatedChromosomesData.first()
        chromosomeData.evaluationStatus = EVALUATING
        chromosomeData.evaluationId = UUID.randomUUID().toString()
        chromosomeData.evaluationBeginAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
        chromosomeData = chromosomeRepository.save(chromosomeData)
        logger.info("Chromosome with id ${chromosomeData.id} from optimizationRun with id $optimizationRunId changed evaluationStatus to EVALUATING")
        val chromosomeResponse = ChromosomeResponse(chromosomeData)

        val optimizationRunChromosomeResponse = OptimizationRunChromosomeResponse(
            optimizationRunId = optimizationRun.id,
            optimizationStatus = optimizationRun.status,
            chromosome = chromosomeResponse
        )
        return ResponseEntity.ok(optimizationRunChromosomeResponse)
    }

}