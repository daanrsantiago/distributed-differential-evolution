package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ObjectiveFunctionData
import org.springframework.data.repository.CrudRepository

interface ObjectiveFunctionRepository: CrudRepository<ObjectiveFunctionData, Long> {
}