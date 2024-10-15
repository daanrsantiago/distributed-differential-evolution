import client.getNotEvaluatedChromosome
import client.publishEvaluationError
import client.publishEvaluationResult
import client.response.GetChromosomeForEvaluationResponse
import client.response.OptimizationStatus
import utils.getOptimizationRunIdFromFile
import java.io.File
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

val containerId: Int = System.getenv("CONTAINER_ID")?.toIntOrNull() ?: 0
val recordTimings: Boolean = containerId == 0 || containerId == 1
var getChromosomeMeasuredTimesFile: File? = null
var objectiveFunctionMeasuredTimesFile: File? = null
var publishEvaluationResultMeasuredTimesFile: File? = null
lateinit var waitCountFile: File
lateinit var chromosomeCountFile: File
var countFilesWritten: Boolean = false


@OptIn(ExperimentalTime::class)
fun main() {
    val chromosomeCount = HashMap<Int, Int>()
    val waitCount = HashMap<Int,Int>()
    var currentOptimizationRunId = 0
    var optimizationRunIdChanged: Boolean

    println("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        optimizationRunIdChanged = optimizationRunId != currentOptimizationRunId
        if (optimizationRunIdChanged) {
            println("OptimizationRunId changed to $optimizationRunId")
            currentOptimizationRunId = optimizationRunId
            waitCount[optimizationRunId] = 0
            chromosomeCount[optimizationRunId] = 0
            countFilesWritten = false
        }
        createFilesIfOptimizationRunChanged(optimizationRunIdChanged, optimizationRunId)
        try {
            val optimizationRunResponse: GetChromosomeForEvaluationResponse?
            val shouldWait: Boolean
            val getChromosomeMeasuredTime = measureTime {
                val response = getNotEvaluatedChromosome(optimizationRunId)
                optimizationRunResponse = response.first
                shouldWait = response.second
            }

            if (shouldWait) {
                increaseCount(waitCount, optimizationRunId)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != OptimizationStatus.FINISHED) {
                increaseCount(chromosomeCount, optimizationRunId)
                if (optimizationRunResponse.chromosome == null) throw java.lang.RuntimeException("null chromosome")

                recordTiming(getChromosomeMeasuredTimesFile, getChromosomeMeasuredTime)
                val chromosome = optimizationRunResponse.chromosome

                try {
                    val fitness: Double

                    val objectiveFunctionMeasuredTime = measureTime {
                        fitness = objectiveFunction(chromosome.elements)
                    }

                    recordTiming(objectiveFunctionMeasuredTimesFile, objectiveFunctionMeasuredTime)

                    val publishEvaluationResultMeasuredTime = measureTime {
                        publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
                    }

                    recordTiming(publishEvaluationResultMeasuredTimesFile, publishEvaluationResultMeasuredTime)
                } catch (e: Exception) {
                    publishEvaluationError(chromosome.id, chromosome.evaluationId!!, "error while calculating objective function")
                }
            } else if (optimizationRunResponse.optimizationStatus == OptimizationStatus.FINISHED && !countFilesWritten) {
                waitCountFile.writeText(waitCount[optimizationRunId].toString())
                chromosomeCountFile.writeText(chromosomeCount[optimizationRunId].toString())
                countFilesWritten = true
                println("Calculated ${chromosomeCount[optimizationRunId]} chromosomes and waited ${waitCount[optimizationRunId]} times for run with id $optimizationRunId")
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

private fun createFilesIfOptimizationRunChanged(
    optimizationRunIdChanged: Boolean,
    optimizationRunId: Int
) {
    if (optimizationRunIdChanged) {
        val runIdDirectory = File("./resultados/runId$optimizationRunId")
        if (!runIdDirectory.exists()) runIdDirectory.mkdirs()
        val containerDirectory = File("./resultados/runId$optimizationRunId/container$containerId")
        if (!containerDirectory.exists()) containerDirectory.mkdirs()
        waitCountFile = File("./resultados/runId$optimizationRunId/container$containerId/waitCount.txt")
        chromosomeCountFile = File("./resultados/runId$optimizationRunId/container$containerId/chromosomeCount.txt")
        if (recordTimings) {
            getChromosomeMeasuredTimesFile =
                File("./resultados/runId$optimizationRunId/container$containerId/getChromosomeMeasuredTimes.txt")
            objectiveFunctionMeasuredTimesFile =
                File("./resultados/runId$optimizationRunId/container$containerId/objectiveFunctionMeasuredTimes.txt")
            publishEvaluationResultMeasuredTimesFile =
                File("./resultados/runId$optimizationRunId/container$containerId/publishEvaluationResultMeasuredTimes.txt")
        }
    }
}

private fun recordTiming(measuredTimesFile: File?, measuredTime: Duration) {
    if (recordTimings) {
        measuredTimesFile!!.appendText("${measuredTime.inWholeMicroseconds}\n")
        println("${measuredTimesFile.name} add duration: ${measuredTime.toString(DurationUnit.MILLISECONDS, 8)}")
    }
}

private fun increaseCount(hashMapCount: HashMap<Int, Int>, optimizationRunId: Int) {
    if (!hashMapCount.contains(optimizationRunId)) {
        hashMapCount[optimizationRunId] = 1
    } else {
        hashMapCount[optimizationRunId] = hashMapCount[optimizationRunId] as Int + 1
    }
}

