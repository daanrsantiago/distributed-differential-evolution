package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ObjectiveFunctionData

data class CreateObjectiveFunctionRequest(
    val name: String,
    val description: String?,
    val hash: String
) {

    fun toObjectiveFunctionData(): ObjectiveFunctionData {
        return ObjectiveFunctionData(
            name = name,
            description = description,
            hash = hash
        )
    }

}
