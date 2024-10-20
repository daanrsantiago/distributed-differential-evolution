package br.com.daniel.optimization.distributed.diferentialEvolution.controller

import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.GetPopulationResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.controller.response.GetPopulationStatisticsResponse
import br.com.daniel.optimization.distributed.diferentialEvolution.service.PopulationService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/population")
class PopulationController(
    val populationService: PopulationService
) {
    @GetMapping
    fun getPopulationsPage(pageable: Pageable): Page<GetPopulationResponse> {
        val populationsPage = populationService.getPopulationsPage(pageable)
        val populationsResponsePage = populationsPage.map { GetPopulationResponse(it) }
        return populationsResponsePage
    }

    @GetMapping("/{populationId}")
    fun getPopulationById(@PathVariable populationId: Long): ResponseEntity<GetPopulationResponse> {
        val population = populationService.getPopulation(populationId)
        return ResponseEntity.ok(GetPopulationResponse(population))
    }

    @GetMapping("/{populationId}/statistics")
    fun getPopulationStatisticsById(@PathVariable populationId: Long): ResponseEntity<GetPopulationStatisticsResponse> {
        val population = populationService.getPopulation(populationId)
        return ResponseEntity.ok(GetPopulationStatisticsResponse(population))
    }

}