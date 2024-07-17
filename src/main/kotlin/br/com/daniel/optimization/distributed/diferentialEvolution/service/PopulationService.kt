package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.ChromosomeRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.PopulationRepository
import org.springframework.stereotype.Service

@Service
class PopulationService(
    val populationRepository: PopulationRepository,
    val chromosomeRepository: ChromosomeRepository
) {
}