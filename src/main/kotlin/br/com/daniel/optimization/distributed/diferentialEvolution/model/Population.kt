package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeData
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import br.com.daniel.optimization.distributed.diferentialEvolution.util.*

class Population(
    val generation: Int,
    val populationMembers: MutableList<Chromosome>,
) {

    fun toPopulationData(optimizationRunId: Long, objectiveFunctionId: Long): PopulationData {
        return PopulationData(
            optimizationRunId = optimizationRunId,
            generation = generation,
            size = populationMembers.size,
            populationMembers = populationMembers.map { it.toChromosomeData(optimizationRunId, objectiveFunctionId) }.toMutableList()
        )
    }

    fun createExperimentalChromosomes(perturbationFactor: Double, crossoverProbability: Double): MutableList<Chromosome> {
        return populationMembers.map { createExperimentalChromosome(it, perturbationFactor, crossoverProbability) }.toMutableList()
    }

    private fun createExperimentalChromosome(targetChromosome: Chromosome, perturbationFactor: Double, crossoverProbability: Double): Chromosome {
        val donorChromosome = createDonorChromosome(targetChromosome, perturbationFactor)
        val experimentalChromosomeElements = targetChromosome.elements.mapIndexed { targetChromosomeElementIndex, targetChromosomeElement ->
            if (Math.random() < crossoverProbability) donorChromosome.elements[targetChromosomeElementIndex]
            targetChromosomeElement
        }.toMutableList()
        return Chromosome(
            type = EXPERIMENTAL,
            targetChromosomeId = targetChromosome.id,
            targetPopulationId = targetChromosome.populationId,
            elements = experimentalChromosomeElements,
            size = populationMembers.size
        )
    }

    private fun createDonorChromosome(targetChromosome: Chromosome, perturbationFactor: Double): Chromosome {
        val differenceChromosome = createDifferenceChromosome()
        return Chromosome(
            type = DONOR,
            targetChromosomeId = targetChromosome.id,
            targetPopulationId = targetChromosome.targetPopulationId,
            elements = targetChromosome.elements + (differenceChromosome.elements * perturbationFactor),
            size = populationMembers.size
        )
    }

    private fun createDifferenceChromosome(): Chromosome {
        return Chromosome(
            type = DIFFERENCE,
            elements = getRandomMember().elements - getRandomMember().elements,
            size = populationMembers.size
        )
    }

    private fun getRandomMember(): Chromosome {
        return populationMembers.random()
    }

}
