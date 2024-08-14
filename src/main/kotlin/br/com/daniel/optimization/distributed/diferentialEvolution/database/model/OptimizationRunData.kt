package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy
import br.com.daniel.optimization.distributed.diferentialEvolution.model.OptimizationStrategy.DE_RAND_1_BIN
import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "optimizationRuns")
class OptimizationRunData(
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    val id: Long? = null,
    val objectiveFunctionId: Long? = null,
    val populationSize: Int? = null,
    val strategy: OptimizationStrategy = DE_RAND_1_BIN,
    val crossOverProbability: Double? = null,
    val perturbationFactor: Double? = null,
    val valueToReach: Double? = null,
    val maxGenerations: Int? = null,
    var currentGeneration: Int,
    @ManyToOne(optional = true)
    @JoinColumn(name = "chromosomeId", unique = true, nullable = true)
    var bestSoFarChromosome: ChromosomeData? = null,
    val objectiveFunctionEvaluationTimeoutSeconds: Long? = null,
    val maxObjectiveFunctionReEvaluations: Int = 3,
    var status: OptimizationStatus = OptimizationStatus.RUNNING,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name="chromosomeElementDetails",
        joinColumns= [JoinColumn(name = "optimizationRunId")]
    )
    val chromosomeElementsDetails: MutableList<ChromosomeElementDetailsData>? = null,
    var timeToFinishInSeconds: Long? = null,
    var finishedAt: ZonedDateTime? = null,
    val createdAt: ZonedDateTime? = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {
    init {
        chromosomeElementsDetails?.sortBy { it.position }
    }
}