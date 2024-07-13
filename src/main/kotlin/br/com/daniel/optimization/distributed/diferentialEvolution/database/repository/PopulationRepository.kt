package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import org.springframework.data.repository.CrudRepository

interface PopulationRepository: CrudRepository<PopulationData, Long> {
}