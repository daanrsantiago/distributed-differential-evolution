package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import br.com.daniel.optimization.distributed.diferentialEvolution.database.model.EvaluationStatus.NOT_EVALUATED
import jakarta.persistence.*
import jakarta.persistence.EnumType.STRING
import org.hibernate.Hibernate
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "chromosomes", indexes = [
    Index(columnList = "chromosome_id", name = "chromosomes_chromosomeId_idx", unique = true),
    Index(columnList = "population_id", name = "chromosomes_populationId_idx"),
    Index(columnList = "optimization_run_id", name = "chromosomes_optimizationRunId_idx"),
    Index(columnList = "optimization_run_id, evaluation_status", name = "chromosomes_optimizationRunId_evaluation_status_idx"),
    Index(columnList = "targetPopulationId", name = "chromosomes_targetPopulationId_idx")
])
data class ChromosomeData (
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "chromosome_id")
    var id: Long? = null,
    @Column(name = "population_id")
    var populationId: Long? = null,
    @Column(name = "optimization_run_id")
    val optimizationRunId: Long? = null,
    val objectiveFunctionId: Long? = null,
    val generation: Int? = null,
    var fitness: Double? = null,
    @Enumerated(STRING)
    val type: ChromosomeType,
    val targetChromosomeId: Long? = null,
    val targetPopulationId: Long? = null,
    @Column(name = "evaluation_status")
    @Enumerated(STRING)
    var evaluationStatus: EvaluationStatus = NOT_EVALUATED,
    val evaluationErrorReason: String? = null,
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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
        other as ChromosomeData

        return id != null && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()

    @Override
    override fun toString(): String {
        return this::class.simpleName + "(id = $id , populationId = $populationId , optimizationRunId = $optimizationRunId , objectiveFunctionId = $objectiveFunctionId , generation = $generation , fitness = $fitness , type = $type , targetChromosomeId = $targetChromosomeId , targetPopulationId = $targetPopulationId , evaluationStatus = $evaluationStatus , evaluationErrorReason = $evaluationErrorReason , evaluationRetries = $evaluationRetries , evaluationId = $evaluationId , evaluationBeginAt = $evaluationBeginAt , evaluatedAt = $evaluatedAt , elements = $elements , createdAt = $createdAt )"
    }
}