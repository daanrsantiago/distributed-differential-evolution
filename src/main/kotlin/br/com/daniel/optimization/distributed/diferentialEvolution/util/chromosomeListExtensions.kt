package br.com.daniel.optimization.distributed.diferentialEvolution.util

import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome

fun List<Chromosome>.bestChromosome(): Chromosome {
    return this.minBy { it.fitness ?: Double.MAX_VALUE }
}