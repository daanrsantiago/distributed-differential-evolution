package util

import client.request.PublishEvaluationErrorRequest
import client.request.PublishEvaluationResultRequest
import client.response.OptimizationRunResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

val objectMapper = jacksonObjectMapper()
val client = HttpClient.newBuilder().build()
val baseUrl = System.getenv("SERVER_HOST") ?: "localhost"

fun getNotEvaluatedChromosome(optimizationRunId: Int): Pair<OptimizationRunResponse?, Boolean> {
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://$baseUrl:8080/optimizationRun/$optimizationRunId/chromosome/notEvaluated"))
        .GET()
        .timeout(Duration.ofMillis(100))
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString());

    return if (response.statusCode() == 200) {
        val optimizationRunResponse: OptimizationRunResponse = objectMapper.readValue(response.body())
        println("Chromosome with id ${optimizationRunResponse.chromosome!!.id} obtained with success")
        Pair(optimizationRunResponse, false)
    } else {
        println("Could not obtain chromosome from optimizationId $optimizationRunId with success")
        Pair(null, true)
    }
}

fun publishEvaluationResult(result: Double, chromosomeId: Long, evaluationId: String): HttpResponse<String>? {
    val requestBody = objectMapper.writeValueAsString(
        PublishEvaluationResultRequest(
            result,
            evaluationId
        )
    )
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://$baseUrl:8080/chromosome/$chromosomeId/evaluationResult"))
        .POST( HttpRequest.BodyPublishers.ofString(requestBody) )
        .setHeader("Content-Type", "application/json")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response
}

fun publishEvaluationError(chromosomeId: Long, evaluationId: String, reason: String = ""): HttpResponse<String> {
    val requestBody = objectMapper.writeValueAsString(
        PublishEvaluationErrorRequest(
            evaluationId,
            reason
        )
    )
    val request = HttpRequest.newBuilder()
        .uri(URI.create("http://$baseUrl:8080/chromosome/$chromosomeId/evaluationError"))
        .POST( HttpRequest.BodyPublishers.ofString(requestBody) )
        .setHeader("Content-Type", "application/json")
        .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    return response
}