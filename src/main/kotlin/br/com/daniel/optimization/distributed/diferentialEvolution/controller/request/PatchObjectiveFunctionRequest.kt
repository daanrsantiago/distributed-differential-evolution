package br.com.daniel.optimization.distributed.diferentialEvolution.controller.request

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ObjectiveFunctionData
import java.util.Optional

data class PatchObjectiveFunctionRequest(
    val name: Optional<String> = Optional.empty(),
    val description: Optional<String> = Optional.empty(),
    val hash: Optional<String> = Optional.empty<String>()
) {
    fun patchObjectiveFunction(objectiveFunctionData: ObjectiveFunctionData): ObjectiveFunctionData {
        name.ifPresent { objectiveFunctionData.name = it }
        description.ifPresent { objectiveFunctionData.description = it}
        hash.ifPresent { objectiveFunctionData.hash = it }
        return objectiveFunctionData
    }
}
