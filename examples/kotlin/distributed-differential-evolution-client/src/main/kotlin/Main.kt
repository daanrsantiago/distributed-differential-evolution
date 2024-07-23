import client.response.OptimizationStatus.FINISHED
import util.getNotEvaluatedChromosome
import util.getOptimizationRunIdFromFile
import util.publishEvaluationResult
import util.rastrigin


fun main() {
    print("Start app")
    while (true) {
        val optimizationRunId = getOptimizationRunIdFromFile()
        try {
            val (optimizationRunResponse, shouldWait) = getNotEvaluatedChromosome(optimizationRunId)

            if (shouldWait) {
                Thread.sleep(100)
                continue
            } else if (optimizationRunResponse!!.optimizationStatus != FINISHED) {
                val fitness = rastrigin(optimizationRunResponse.chromosome!!.elements)
                publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}



