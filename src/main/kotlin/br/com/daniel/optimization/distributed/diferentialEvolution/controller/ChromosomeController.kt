package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ChangeEvaluationResultRequest
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ChromosomeResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
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

    @GetMapping("/{chromosomeId}")
    fun getChromosomeById(@PathVariable chromosomeId: Long): ResponseEntity<ChromosomeResponse> {
        val chromosome = chromosomeRepository.findById(chromosomeId)
        if (chromosome.isEmpty) return ResponseEntity.notFound().build()
        return ResponseEntity.ok(ChromosomeResponse(chromosome.get()))
    }

    @PostMapping("/{chromosomeId}/evaluationResult")
    fun changeEvaluationResult(
        @PathVariable
        chromosomeId: Long,
        @RequestBody
        changeEvaluationResultRequest: ChangeEvaluationResultRequest
    ): ResponseEntity<ChromosomeResponse> {
        val chromosomeOptional = chromosomeRepository.findById(chromosomeId)
        if (chromosomeOptional.isEmpty) return ResponseEntity.notFound().build()
        val chromosomeData = chromosomeOptional.get()

        if (chromosomeData.evaluationStatus != EVALUATING && chromosomeData.evaluationStatus != TIMEOUT) return ResponseEntity.badRequest().build()
        if (chromosomeData.evaluationId != changeEvaluationResultRequest.evaluationId) return ResponseEntity.badRequest().build()

        val populationOptional = populationRepository.findById(chromosomeData.populationId!!)
        if (populationOptional.isEmpty) return  ResponseEntity.internalServerError().build()
        val populationData = populationOptional.get()

        chromosomeData.fitness = changeEvaluationResultRequest.fitness
        chromosomeData.evaluationStatus = EVALUATED
        chromosomeData.evaluatedAt = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))

        chromosomeRepository.save(chromosomeData)

        val allPopulationChromosomesEvaluated = chromosomeRepository.areAllChromosomesEvaluated(chromosomeData.populationId)
        if (allPopulationChromosomesEvaluated) {
            // Preciso gerar novos chromosomos
        }

        return ResponseEntity.ok(ChromosomeResponse(chromosomeData))
    }
}