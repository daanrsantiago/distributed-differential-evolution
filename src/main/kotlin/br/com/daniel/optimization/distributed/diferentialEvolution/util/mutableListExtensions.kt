package br.com.daniel.optimization.distributed.diferentialEvolution.util

operator fun MutableList<Double>.minus(other: MutableList<Double>): MutableList<Double> {
    if (this.size != other.size) throw IllegalArgumentException("To subtract a vector to other vector they must be of the same size")
    val newList = mutableListOf<Double>()
    forEachIndexed { index, element ->
        newList.add(element - other[index])
    }
    return newList
}

operator fun MutableList<Double>.plus(other: MutableList<Double>): MutableList<Double> {
    if (this.size != other.size) throw IllegalArgumentException("To sum a vector to other vector they must be of the same size")
    val newList = mutableListOf<Double>()
    forEachIndexed { index, element ->
        newList.add(element + other[index])
    }
    return newList
}

operator fun MutableList<Double>.times(scalar: Double): MutableList<Double> {
    val newList = mutableListOf<Double>()
    forEach { element ->
        newList.add(element * scalar)
    }
    return newList
}