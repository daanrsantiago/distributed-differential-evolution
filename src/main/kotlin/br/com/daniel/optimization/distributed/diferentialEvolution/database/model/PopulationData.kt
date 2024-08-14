package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.GenerationType.SEQUENCE
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "populations", indexes = [
    Index(columnList = "id", name = "populations_id_idx"),
    Index(columnList = "optimizationRunId", name = "populations_optimizationRunId_idx")
])
class PopulationData(
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    val id: Long? = null,
    val optimizationRunId: Long? = null,
    val generation: Int? = null,
    val size: Int? = null,
    @OneToMany(cascade = [ALL], fetch = EAGER)
    @JoinColumn(name = "populationId")
    var members: MutableList<ChromosomeData>? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {
}