package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.CreateOptimizationRunRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.CreateOptimizationRunResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.OptimizationRunChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeElementData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.EVALUATING
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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

    val logger = LoggerFactory.getLogger(this::class.java)

    @PostMapping
    fun createOptimizationRun(
        @RequestBody
        createOptimizationRunRequest: CreateOptimizationRunRequest
    ): ResponseEntity<CreateOptimizationRunResponse> {
        var optimizationRunData = createOptimizationRunRequest.toOptimizationRunData()
        optimizationRunData = optimizationRunRepository.save(optimizationRunData)
        val createOptimizationRunResponse = CreateOptimizationRunResponse(optimizationRunData)

        val optimizationRun = OptimizationRun(optimizationRunData)
        val initialPopulation = optimizationRun.createInitialPopulation()
        var initialPopulationData = initialPopulation.toPopulationData(
            optimizationRunData.id!!
        )
        initialPopulationData = populationRepository.save(initialPopulationData)

        initialPopulationData.populationMembers = initialPopulation.populationMembers.map {
            val chromosomeData = ChromosomeData(
                populationId = initialPopulationData.id,
                optimizationRunId = optimizationRunData.id,
                objectiveFunctionId = optimizationRunData.objectiveFunctionId,
                size = it.size,
                elements = it.elements?.mapIndexed { position, value ->
                    ChromosomeElementData(
                        position = position,
                        value = value
                    )
                }?.toMutableList()
            )
            chromosomeRepository.save(chromosomeData)
        }.toMutableList()

        return ResponseEntity.status(HttpStatus.CREATED).body(createOptimizationRunResponse)
    }

    @GetMapping("/{optimizationRunId}/chromosome/notEvaluated")
    fun getNotEvaluatedChromosome(
        @PathVariable("optimizationRunId") optimizationRunId: Int
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