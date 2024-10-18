import client.getNotEvaluatedChromosome
import client.publishEvaluationError
import client.publishEvaluationResult
import client.response.GetChromosomeForEvaluationResponse
import client.response.OptimizationStatus
import utils.getOptimizationRunIdFromFile
import java.io.File
import kotlin.time.measureTime

val containerId: Int = System.getenv("CONTAINER_ID")?.toIntOrNull() ?: 0
lateinit var getChromosomeSuccessfullyMeasuredTimesFile: File
lateinit var getChromosomeFailMeasuredTimesFile: File
lateinit var objectiveFunctionMeasuredTimesFile: File
lateinit var publishEvaluationResultMeasuredTimesFile: File
var isFilesWritten: Boolean = false


fun main() {
    val getChromosomeSuccessfullyMeasuredTimes = mutableListOf<Long>()
    val getChromosomeFailMeasuredTimes = mutableListOf<Long>()
    val objectiveFunctionMeasuredTimes = mutableListOf<Long>()
    val publishEvaluationResultMeasuredTimes = mutableListOf<Long>()
    var currentOptimizationRunId = 0
    var optimizationRunIdChanged: Boolean

    println("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        optimizationRunIdChanged = optimizationRunId != currentOptimizationRunId
        if (optimizationRunIdChanged) {
            println("OptimizationRunId changed to $optimizationRunId")
            currentOptimizationRunId = optimizationRunId
            getChromosomeSuccessfullyMeasuredTimes.clear()
            getChromosomeFailMeasuredTimes.clear()
            objectiveFunctionMeasuredTimes.clear()
            publishEvaluationResultMeasuredTimes.clear()
            isFilesWritten = false
            createFiles(optimizationRunId)
        }
        try {
            val optimizationRunResponse: GetChromosomeForEvaluationResponse?
            val shouldWait: Boolean
            val getChromosomeMeasuredTime = measureTime {
                val response = getNotEvaluatedChromosome(optimizationRunId)
                optimizationRunResponse = response.first
                shouldWait = response.second
            }

            if (shouldWait) {
                getChromosomeFailMeasuredTimes.add(getChromosomeMeasuredTime.inWholeMicroseconds)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != OptimizationStatus.FINISHED) {
                getChromosomeSuccessfullyMeasuredTimes.add(getChromosomeMeasuredTime.inWholeMicroseconds)
                if (optimizationRunResponse.chromosome == null) throw java.lang.RuntimeException("null chromosome")

                val chromosome = optimizationRunResponse.chromosome

                try {
                    val fitness: Double
                    val objectiveFunctionMeasuredTime = measureTime {
                        fitness = objectiveFunction(chromosome.elements)
                    }
                    objectiveFunctionMeasuredTimes.add(objectiveFunctionMeasuredTime.inWholeMicroseconds)

                    val publishEvaluationResultMeasuredTime = measureTime {
                        publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
                    }
                    publishEvaluationResultMeasuredTimes.add(publishEvaluationResultMeasuredTime.inWholeMicroseconds)
                } catch (e: Exception) {
                    publishEvaluationError(chromosome.id, chromosome.evaluationId!!, "error while calculating objective function")
                }
            } else if (optimizationRunResponse.optimizationStatus == OptimizationStatus.FINISHED && !isFilesWritten) {
                getChromosomeSuccessfullyMeasuredTimesFile.writeText(getChromosomeSuccessfullyMeasuredTimes.joinToString(separator = "\n"))
                getChromosomeFailMeasuredTimesFile.writeText(getChromosomeFailMeasuredTimes.joinToString(separator = "\n"))
                objectiveFunctionMeasuredTimesFile.writeText(objectiveFunctionMeasuredTimes.joinToString(separator = "\n"))
                publishEvaluationResultMeasuredTimesFile.writeText(publishEvaluationResultMeasuredTimes.joinToString(separator = "\n"))
                isFilesWritten = true
                println("Calculated ${publishEvaluationResultMeasuredTimes.size} chromosomes and waited ${getChromosomeFailMeasuredTimes.size} times for run with id $optimizationRunId")
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

private fun createFiles(
    optimizationRunId: Int
) {
    val resultsDirectory = File("./results")
    if (!resultsDirectory.exists()) resultsDirectory.mkdirs()
    val runIdDirectory = File("./results/runId$optimizationRunId")
    if (!runIdDirectory.exists()) runIdDirectory.mkdirs()
    val containerDirectory = File("./results/runId$optimizationRunId/container$containerId")
    if (!containerDirectory.exists()) containerDirectory.mkdirs()
    val basePath = "./results/runId$optimizationRunId/container$containerId"
    getChromosomeSuccessfullyMeasuredTimesFile = File("$basePath/getChromosomeSuccessfullyMeasuredTimes.txt")
    getChromosomeFailMeasuredTimesFile = File("$basePath/getChromosomeFailMeasuredTimes.txt")
    objectiveFunctionMeasuredTimesFile = File("$basePath/objectiveFunctionMeasuredTimes.txt")
    publishEvaluationResultMeasuredTimesFile = File("$basePath/publishEvaluationResultMeasuredTimes.txt")
}

