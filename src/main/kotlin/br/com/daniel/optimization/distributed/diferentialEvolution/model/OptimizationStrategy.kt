package br.com.daniel.optimization.distributed.diferentialEvolution.model

import br.com.daniel.optimization.distributed.diferentialEvolution.util.bestChromosome
import br.com.daniel.optimization.distributed.diferentialEvolution.util.minus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.plus
import br.com.daniel.optimization.distributed.diferentialEvolution.util.times

enum class OptimizationStrategy {
    DE_RAND_1_BIN {
        override fun createDonorChromosomeElements(populationMembers: List<Chromosome>, perturbationFactor: Double): MutableList<Double> {
            val populationMembersToUse = populationMembers.shuffled().subList(0,3)
            val alphaChromosome = populationMembersToUse[0]
            val betaChromosome = populationMembersToUse[1]
            val gamaChromosome = populationMembersToUse[2]
            return alphaChromosome.elements + (betaChromosome.elements - gamaChromosome.elements) * perturbationFactor
        }
    },
    DE_BEST_1_BIN {
        override fun createDonorChromosomeElements(populationMembers: List<Chromosome>, perturbationFactor: Double): MutableList<Double> {
            val populationMembersToUse = populationMembers.shuffled().subList(0,2)
            val bestChromosome = populationMembers.bestChromosome()
            val betaChromosome = populationMembersToUse[0]
            val gamaChromosome = populationMembersToUse[1]
            return bestChromosome.elements + (betaChromosome.elements - gamaChromosome.elements) * perturbationFactor
        }
    },
    DE_RAND_2_BIN {
        override fun createDonorChromosomeElements(populationMembers: List<Chromosome>, perturbationFactor: Double): MutableList<Double> {
            val populationMembersToUse = populationMembers.shuffled().subList(0,5)
            val alphaChromosome = populationMembersToUse[0]
            val betaChromosome = populationMembersToUse[1]
            val gamaChromosome = populationMembersToUse[2]
            val rhoChromosome = populationMembersToUse[3]
            val deltaChromosome = populationMembersToUse[4]
            return alphaChromosome.elements + (betaChromosome.elements - gamaChromosome.elements) * perturbationFactor + (rhoChromosome.elements - deltaChromosome.elements) * perturbationFactor
        }
    },
    DE_BEST_2_BIN {
        override fun createDonorChromosomeElements(populationMembers: List<Chromosome>, perturbationFactor: Double): MutableList<Double> {
            val populationMembersToUse = populationMembers.shuffled().subList(0,5)
            val bestChromosome = populationMembers.bestChromosome()
            val betaChromosome = populationMembersToUse[1]
            val gamaChromosome = populationMembersToUse[2]
            val rhoChromosome = populationMembersToUse[3]
            val deltaChromosome = populationMembersToUse[4]
            return bestChromosome.elements + (betaChromosome.elements - gamaChromosome.elements) * perturbationFactor + (rhoChromosome.elements - deltaChromosome.elements) * perturbationFactor
        }
    };

    abstract fun createDonorChromosomeElements(populationMembers: List<Chromosome>, perturbationFactor: Double): MutableList<Double>
}