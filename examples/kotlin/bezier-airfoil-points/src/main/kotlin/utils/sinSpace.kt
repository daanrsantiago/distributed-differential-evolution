package utils

import kotlin.math.PI
import kotlin.math.sin

fun sinSpace(min: Double = 0.0, max: Double = PI/2, nPoints: Int = 10): List<Double> {
    val linearSpacedPoints = linspace(min,max,nPoints)
    return linearSpacedPoints.map { sin(it) }
}