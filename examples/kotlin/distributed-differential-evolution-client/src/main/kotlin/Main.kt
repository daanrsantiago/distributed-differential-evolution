import client.request.ChangeEvaluationResultRequest
import client.response.OptimizationRunResponse
import client.response.OptimizationStatus.FINISHED
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow

val objectMapper = jacksonObjectMapper()
val client = HttpClient.newBuilder().build()

fun main(args: Array<String>) {

    while (true) {
        val (optimizationRunResponse, shouldWait) = getNotEvaluatedChromosome(1604)

        if (shouldWait) {
            Thread.sleep(1000)
            continue
        } else if (optimizationRunResponse!!.optimizationStatus == FINISHED) {
            break
        }

        val fitness = rastrigin(optimizationRunResponse.chromosome!!.elements)
        publishEvaluationResult(fitness, optimizationRunResponse.chromosome.id, optimizationRunResponse.chromosome.evaluationId!!)
    }
}

fun rastrigin(X: List<Double>): Double {

    return 20 + X[0].pow(2) + X[1].pow(2) - 10 * ( cos(2 * PI * X[0]) + cos(2 * PI * X[1]) )
}

fun getNotEvaluatedChromosome(optimizationRunId: Int): Pair<OptimizationRunResponse?, Boolean> {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/optimizationRun/$optimizationRunId/chromosome/notEvaluated"))
        .GET()
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());

    return if (response.statusCode() == 200) {
        val optimizationRunResponse: OptimizationRunResponse = objectMapper.readValue(response.body())
        Pair(optimizationRunResponse, false)
    } else {
        Pair(null, true)
    }
}

fun publishEvaluationResult(result: Double, chromosomeId: Long, evaluationId: String): HttpResponse<String>? {
    val requestBody = objectMapper.writeValueAsString(
        ChangeEvaluationResultRequest(
            result,
            evaluationId
        )
    )
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://localhost:8080/chromosome/$chromosomeId/evaluationResult"))
        .POST( BodyPublishers.ofString(requestBody) )
        .setHeader("Content-Type", "application/json")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response
}