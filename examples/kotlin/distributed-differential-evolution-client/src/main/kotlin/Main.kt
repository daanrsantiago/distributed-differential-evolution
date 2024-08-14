import client.response.OptimizationStatus.FINISHED
import util.*
import java.util.Random


fun main() {
    print("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        try {
            val (optimizationRunResponse, shouldWait) = getNotEvaluatedChromosome(optimizationRunId)

            if (shouldWait) {
                Thread.sleep(500)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != FINISHED) {
                val randval = Math.random()
                if (randval <= 0.90) {
                    val fitness = rastrigin(optimizationRunResponse.chromosome!!.elements)
                    publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
                } else {
                    publishEvaluationError(optimizationRunResponse.chromosome!!.id, optimizationRunResponse.chromosome.evaluationId!!, "random error")
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}



