import client.getNotEvaluatedChromosome
import client.publishEvaluationError
import client.publishEvaluationResult
import client.response.GetChromosomeForEvaluationResponse
import client.response.OptimizationStatus
import utils.getOptimizationRunIdFromFile
import java.io.File
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

@OptIn(ExperimentalTime::class)
fun main(args: Array<String>) {
    val getChromosomeMeasuredTimesFile = File("getChromosomeMeasuredTimes.txt")
    val objectiveFunctionMeasuredTimesFile = File("objectiveFunctionMeasuredTimes.txt")
    val publishEvaluationResultMeasuredTimesFile = File("publishEvaluationResultMeasuredTimes.txt")

    println("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        try {
            val optimizationRunResponse: GetChromosomeForEvaluationResponse?
            val shouldWait: Boolean
            val getChromosomeMeasuredTime = measureTime {
                val response = getNotEvaluatedChromosome(optimizationRunId)
                optimizationRunResponse = response.first
                shouldWait = response.second
            }

            if (shouldWait) {
                Thread.sleep(500)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != OptimizationStatus.FINISHED) {
                if (optimizationRunResponse.chromosome == null) throw java.lang.RuntimeException("null chromosome")

                getChromosomeMeasuredTimesFile.appendText("${getChromosomeMeasuredTime.inWholeMicroseconds}\n")
                println("get chromosome duration: ${getChromosomeMeasuredTime.toString(DurationUnit.MILLISECONDS, 8)}")
                val chromosome = optimizationRunResponse.chromosome

                try {
                    val fitness: Double

                    val objectiveFunctionMeasuredTime = measureTime {
                        fitness = objectiveFunction(chromosome.elements)
                    }
                    println("objective function duration: ${objectiveFunctionMeasuredTime.toString(DurationUnit.MILLISECONDS, 8)}")
                    objectiveFunctionMeasuredTimesFile.appendText("${objectiveFunctionMeasuredTime.inWholeMicroseconds}\n")

                    val publishEvaluationResultMeasuredTime = measureTime {
                        publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
                    }
                    println("publish evaluation duration: ${publishEvaluationResultMeasuredTime.toString(DurationUnit.MILLISECONDS, 8)}")
                    publishEvaluationResultMeasuredTimesFile.appendText("${publishEvaluationResultMeasuredTime.inWholeMicroseconds}\n")
                } catch (e: Exception) {
                    publishEvaluationError(chromosome.id, chromosome.evaluationId!!, "error while calculating objective function")
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

