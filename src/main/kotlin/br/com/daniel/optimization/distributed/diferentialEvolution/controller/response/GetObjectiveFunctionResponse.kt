package br.com.daniel.optimization.distributed.diferentialEvolution.controller.response

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ObjectiveFunctionData
import java.time.ZonedDateTime

data class GetObjectiveFunctionResponse (
    val id: Long,
    val name: String,
    val description: String?,
    val hash: String,
    val createAt: ZonedDateTime
) {

    constructor(objectiveFunctionData: ObjectiveFunctionData): this(
        id = objectiveFunctionData.id!!,
        name = objectiveFunctionData.name!!,
        description = objectiveFunctionData.description,
        hash = objectiveFunctionData.hash!!,
        createAt = objectiveFunctionData.createdAt
    )

}
