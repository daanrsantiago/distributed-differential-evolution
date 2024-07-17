package br.com.daniel.optimization.distributed.diferentialEvolution.service

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.ChromosomeType
import br.com.daniel.optimization.distributed.diferentialEvolution.database.repository.OptimizationRunRepository
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.model.ChromosomeElementDetails
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationRun
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Population
import br.com.daniel.optimization.distributed.diferentialEvolution.util.minus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.plus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.times
import org.springframework.stereotype.Service

@Service
class OptimizationRunService(
    val optimizationRunRepository: OptimizationRunRepository
) {

    fun createInitialPopulation(optimizationRun: OptimizationRun): Population {
        val populationMembers = mutableListOf<Chromosome>()
        for (iChromosome in 0 until optimizationRun.populationSize) {
            val chromosomeElements = mutableListOf<Double>()
            for(chromosomeElementDetails in optimizationRun.chromosomeElementsDetails) {
                val chromosomeElementValue = chromosomeElementDetails.lowerBoundary +
                        Math.random() * (chromosomeElementDetails.upperBoundary - chromosomeElementDetails.lowerBoundary)
                chromosomeElements.add(chromosomeElementValue)
            }
            val chromosome = Chromosome(
                type = ChromosomeType.TARGET,
                elements = chromosomeElements
            )
            populationMembers.add(chromosome)
        }

        return Population(
            generation = 1,
            populationMembers = populationMembers
        )
    }

    fun createExperimentalChromosomes(optimizationRun: OptimizationRun, populationMembers: List<Chromosome>): MutableList<Chromosome> {
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
            type = ChromosomeType.EXPERIMENTAL,
            targetChromosomeId = targetChromosome.id,
            targetPopulationId = targetChromosome.populationId,
            elements = experimentalChromosomeElements,
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
            type = ChromosomeType.DONOR,
            elements = donorChromosomeElements,
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
            type = ChromosomeType.DIFFERENTIAL,
            elements = getRandomMember().elements - getRandomMember().elements,
        )
    }

}