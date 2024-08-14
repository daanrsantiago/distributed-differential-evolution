package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.NOT_EVALUATED
import jakarta.persistence.*
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "chromosomes", indexes = [
    Index(columnList = "chromosomeId", name = "chromosomes_chromosomeId_idx"),
    Index(columnList = "populationId", name = "chromosomes_populationId_idx"),
    Index(columnList = "optimizationRunId", name = "chromosomes_optimizationRunId_idx"),
    Index(columnList = "targetPopulationId", name = "chromosomes_targetPopulationId_idx")
])
data class ChromosomeData (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "chromosomeId")
    var id: Long? = null,
    @Column(name = "populationId")
    var populationId: Long? = null,
    @Column(name = "optimizationRunId")
    val optimizationRunId: Long? = null,
    val objectiveFunctionId: Long? = null,
    var fitness: Double? = null,
    val type: ChromosomeType,
    val targetChromosomeId: Long? = null,
    val targetPopulationId: Long? = null,
    var evaluationStatus: EvaluationStatus = NOT_EVALUATED,
    var evaluationRetries: Int = 0,
    var evaluationId: String? = null,
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name="chromosomeElement",
        joinColumns= [JoinColumn(name = "chromosomeId")]
    )
    var elements: MutableList<ChromosomeElementData>? = null,
    var createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")),
)