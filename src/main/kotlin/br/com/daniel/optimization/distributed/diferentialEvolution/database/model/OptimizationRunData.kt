package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

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
    val crossOverProbability: Double? = null,
    val perturbationFactor: Double? = null,
    val valueToReach: Double? = null,
    val maxGenerations: Int? = null,
    val bestSoFarChromosomeId: Int? = null,
    val objectiveFunctionEvaluationTimeoutSeconds: Long? = null,
    var status: OptimizationStatus = OptimizationStatus.RUNNING,
    @ElementCollection
    @CollectionTable(
        name="chromosomeElementDetails",
        joinColumns= [JoinColumn(name = "optimizationRunId")]
    )
    val chromosomeElementDetails: MutableList<ChromosomeElementDetailsData>? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
)