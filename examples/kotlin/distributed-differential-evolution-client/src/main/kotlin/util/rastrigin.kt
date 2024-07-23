package util

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

fun rastrigin(X: List<Double>): Double {

    return 20 + X[0].pow(2) + X[1].pow(2) - 10 * ( cos(2 * PI * X[0]) + cos(2 * PI * X[1]) )
}