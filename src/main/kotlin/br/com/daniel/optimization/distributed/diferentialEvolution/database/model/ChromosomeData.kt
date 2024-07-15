package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.NOT_EVALUATED
import jakarta.persistence.*
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "chromosomes")
data class ChromosomeData (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "chromosomeId")
    var id: Long? = null,
    @Column(name = "populationId")
    var populationId: Long? = null,
    val optimizationRunId: Long? = null,
    val objectiveFunctionId: Long? = null,
    var fitness: Double? = null,
    val size: Int,
    val type: ChromosomeType,
    val targetChromosomeId: Long? = null,
    val targetPopulationId: Long? = null,
    var evaluationStatus: EvaluationStatus = NOT_EVALUATED,
    var evaluationId: String? = null,
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
    @ElementCollection
    @CollectionTable(
        name="chromosomeElement",
        joinColumns= [JoinColumn(name = "chromosomeId")]
    )
    var elements: MutableList<ChromosomeElementData>? = null,
    var createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")),
)