import org.junit.jupiter.api.Test
import java.lang.Math.random
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.toDuration


internal class ObjectiveFunctionTimeMeasureTest {

    @OptIn(ExperimentalTime::class)
    @Test
    fun `Measure mean objective function execution time`() {
        val numberOfDesignVariables = 22
        val numberOfMeasures = 40
        val measuredTimes = MutableList<Duration>(numberOfMeasures) { Duration.ZERO }
        val listDesignVariables = (0 until numberOfMeasures).map { createRandomDesignVariables(numberOfDesignVariables) }

        for (iMeasures in 0 until numberOfMeasures) {
            measuredTimes[iMeasures] = measureTime {
                objectiveFunction(designVariables = listDesignVariables[iMeasures])
            }
        }

        val mean = measuredTimes.reduce { acc, value -> acc + value } / measuredTimes.size
        val std = sqrt(measuredTimes.map {
            val deviationFromMean = it.inWholeNanoseconds - mean.inWholeNanoseconds
            return@map deviationFromMean * deviationFromMean
        }.average()).toDuration(DurationUnit.NANOSECONDS)

        println("mean time: ${mean.toString(DurationUnit.SECONDS, 8)}")
        println("standard mean deviation: ${std.toString(DurationUnit.SECONDS, 8)} ")
    }

    private fun createRandomDesignVariables(size: Int): MutableList<Double> {
        val designVariables = MutableList(size) { 0.0 }

        designVariables[0] = 40.0 + random() * 50.0
        designVariables[1] = 0.005 + random() * 0.005

        for (iDesignVariable in 2 until size - 2) {
            if( (iDesignVariable -2) % 3 == 0) {
                designVariables[iDesignVariable] = -10.0 + random() * 10.0
            } else if( (iDesignVariable - 2) % 3 == 1 ) {
                designVariables[iDesignVariable] = 0.01 + random() * 0.02
            } else  {
                designVariables[iDesignVariable] = 0.01 + random() * 0.02
            }
        }

        designVariables.add(-30.0 + random() * 40.0)
        designVariables.add(0.01 + random() * 0.02)

        return designVariables
    }

}