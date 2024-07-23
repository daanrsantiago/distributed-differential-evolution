package util

import java.io.File

val optimizationRunIdFilePath = System.getenv("OPTIMIZATION_RUN_ID_FILE_PATH") ?: "src/main/resources/optimizationRunId.txt"

var currentIterationCount = 0
var optimizationRunId = 1
val iterationsBeforeReadFileAgain = 10

fun getOptimizationRunIdFromFile(): Int {
    currentIterationCount += 1
    if (currentIterationCount % iterationsBeforeReadFileAgain == 0) {
        currentIterationCount = 0
        try {
            optimizationRunId = File(optimizationRunIdFilePath).readText().toInt()
        } catch (e: Exception) {
            print(e.message)
        }
    }

    return optimizationRunId
}