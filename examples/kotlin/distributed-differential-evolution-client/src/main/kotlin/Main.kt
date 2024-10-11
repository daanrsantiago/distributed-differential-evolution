import client.response.OptimizationStatus.FINISHED
import util.*


fun main() {
    print("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        try {
            val (optimizationRunResponse, shouldWait) = getNotEvaluatedChromosome(optimizationRunId)

            if (shouldWait) {
                Thread.sleep(10)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != FINISHED) {
                val fitness = rastrigin(optimizationRunResponse.chromosome!!.elements)
                publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}



