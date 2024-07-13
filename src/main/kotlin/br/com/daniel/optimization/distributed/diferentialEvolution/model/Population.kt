package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData

class Population(
    val generation: Int,
    val populationMembers: MutableList<Chromosome>,
) {

    fun toPopulationData(optimizationRunId: Int): PopulationData {
        return PopulationData(
            optimizationRunId = optimizationRunId,
            generation = generation,
            size = populationMembers.size,
        )
    }

}
