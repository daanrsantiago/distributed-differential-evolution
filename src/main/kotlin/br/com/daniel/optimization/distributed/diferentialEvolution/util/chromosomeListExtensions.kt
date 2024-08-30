package br.com.daniel.optimization.distributed.diferentialEvolution.util

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.ERROR
import br.com.daniel.optimization.distributed.diferentialEvolution.model.Chromosome

fun List<Chromosome>.bestChromosome(): Chromosome {
    return this.minBy { it.fitness ?: Double.MAX_VALUE }
}

fun List<Chromosome>.worstChromosome(): Chromosome {
    return this.maxBy { it.fitness ?: Double.MIN_VALUE }
}

fun List<Chromosome>.meanFitness(): Double {
    val chromosomesWithoutError = this.filter { it.evaluationStatus != ERROR }
    return chromosomesWithoutError.sumOf { it.fitness ?: 0.0 } / chromosomesWithoutError.size
}

