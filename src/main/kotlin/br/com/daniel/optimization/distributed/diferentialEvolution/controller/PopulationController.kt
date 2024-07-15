package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.ErrorResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.model.GetPopulationResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.exception.RestHandledException
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/population")
class PopulationController(
    val populationRepository: PopulationRepository
) {

    @GetMapping("/{populationId}")
    fun getPopulationById(@PathVariable populationId: Long): ResponseEntity<GetPopulationResponse> {
        val populationData = populationRepository.findById(populationId).orElseThrow {
            RestHandledException(ErrorResponse(NOT_FOUND.value(), "Population with id $populationId not found"))
        }
        return ResponseEntity.ok(GetPopulationResponse(populationData))
    }

}