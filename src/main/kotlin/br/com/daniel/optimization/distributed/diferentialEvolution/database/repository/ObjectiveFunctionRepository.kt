package br.com.daniel.optimization.distributed.diferentialEvolution.database.repository

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ObjectiveFunctionData
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository

interface ObjectiveFunctionRepository: PagingAndSortingRepository<ObjectiveFunctionData, Long>, CrudRepository<ObjectiveFunctionData, Long> {
}