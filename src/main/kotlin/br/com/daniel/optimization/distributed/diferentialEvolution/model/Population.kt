package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import java.time.ZoneId
import java.time.ZonedDateTime

class Population(
    val id: Long? = null,
    val optimizationRunId: Long? = null,
    val generation: Int,
    val members: MutableList<Chromosome>,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {

    constructor(populationData: PopulationData): this(
        id = populationData.id,
        optimizationRunId = populationData.optimizationRunId,
        generation = populationData.generation!!,
        members = populationData.members!!.map { Chromosome(it) }.toMutableList(),
        createdAt = populationData.createdAt
    )

    fun toPopulationData(): PopulationData {
        return PopulationData(
            id = id,
            optimizationRunId = optimizationRunId,
            generation = generation,
            size = members.size,
            members = members.map { it.toChromosomeData() }.toMutableList(),
            createdAt = createdAt
        )
    }

}
