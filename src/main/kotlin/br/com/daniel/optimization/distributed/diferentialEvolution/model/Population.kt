package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType.*
import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.PopulationData
import br.com.daniel.optimization.distributed.diferentialEvolution.util.minus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.plus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.times

class Population(
    val generation: Int,
    val populationMembers: MutableList<Chromosome>,
) {

    constructor(populationData: PopulationData): this(
        generation = populationData.generation!!,
        populationMembers = populationData.populationMembers!!.map { Chromosome(it) }.toMutableList()
    )

    fun toPopulationData(optimizationRunId: Long, objectiveFunctionId: Long): PopulationData {
        return PopulationData(
            optimizationRunId = optimizationRunId,
            generation = generation,
            size = populationMembers.size,
            populationMembers = populationMembers.map { it.toChromosomeData(optimizationRunId, objectiveFunctionId) }.toMutableList()
        )
    }

    fun createExperimentalChromosomes(optimizationRun: OptimizationRun): MutableList<Chromosome> {
        return populationMembers.map {
            createExperimentalChromosome(
                it,
                optimizationRun.perturbationFactor,
                optimizationRun.crossOverProbability,
                optimizationRun.chromosomeElementsDetails
            )
        }.toMutableList()
    }

    private fun createExperimentalChromosome(
        targetChromosome: Chromosome,
        perturbationFactor: Double,
        crossoverProbability: Double,
        chromosomeElementDetails: List<ChromosomeElementDetails>
    ): Chromosome {
        val donorChromosome = createDonorChromosome(perturbationFactor, chromosomeElementDetails)
        val experimentalChromosomeElements = targetChromosome.elements.mapIndexed { targetChromosomeElementIndex, targetChromosomeElement ->
            if (Math.random() < crossoverProbability) {
                return@mapIndexed donorChromosome.elements[targetChromosomeElementIndex]
            }
            return@mapIndexed targetChromosomeElement
        }.toMutableList()
        return Chromosome(
            type = EXPERIMENTAL,
            targetChromosomeId = targetChromosome.id,
            targetPopulationId = targetChromosome.populationId,
            elements = experimentalChromosomeElements,
            size = populationMembers.size
        )
    }
    private fun createDonorChromosome(
        perturbationFactor: Double,
        chromosomeElementDetails: List<ChromosomeElementDetails>
    ): Chromosome {
        val differenceChromosome = createDifferenceChromosome()
        val alphaChromosome = populationMembers.random()
        val donorChromosomeElements = alphaChromosome.elements + (differenceChromosome.elements * perturbationFactor)
        limitChromosomeElementsToBoundaries(chromosomeElementDetails, donorChromosomeElements)
        return Chromosome(
            type = DONOR,
            elements = donorChromosomeElements,
            size = populationMembers.size
        )
    }

    private fun limitChromosomeElementsToBoundaries(
        chromosomeElementDetails: List<ChromosomeElementDetails>,
        donorChromosomeElements: MutableList<Double>
    ) {
        chromosomeElementDetails.forEach {
            if (donorChromosomeElements[it.position] > it.upperBoundary) {
                donorChromosomeElements[it.position] = it.upperBoundary
            } else if (donorChromosomeElements[it.position] < it.lowerBoundary) {
                donorChromosomeElements[it.position] = it.lowerBoundary
            }
        }
    }

    private fun createDifferenceChromosome(): Chromosome {
        return Chromosome(
            type = DIFFERENTIAL,
            elements = getRandomMember().elements - getRandomMember().elements,
            size = populationMembers.size
        )
    }

    private fun getRandomMember(): Chromosome {
        return populationMembers.random()
    }

}
