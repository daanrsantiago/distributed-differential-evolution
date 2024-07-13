package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.NOT_EVALUATED
import jakarta.persistence.*
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "chromosomes")
class ChromosomeData (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val id: Long? = null,
    val populationId: Long? = null,
    val optimizationRunId: Long? = null,
    val objectiveFunctionId: Long? = null,
    var fitness: Double? = null,
    val size: Int,
    var evaluationStatus: EvaluationStatus = NOT_EVALUATED,
    var evaluationId: String? = null,
    @ElementCollection
    @CollectionTable(
        name="chromosomeElement",
        joinColumns= [JoinColumn(name = "chromosomeId")]
    )
    val elements: MutableList<ChromosomeElementData>? = null,
    var createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo")),
    var evaluationBeginAt: ZonedDateTime? = null,
    var evaluatedAt: ZonedDateTime? = null,
)