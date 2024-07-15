package br.com.daniel.optimization.distributed.diferentialEvolution.database.model

import jakarta.persistence.*
import jakarta.persistence.GenerationType.SEQUENCE
import java.time.ZoneId
import java.time.ZonedDateTime

@Entity
@Table(name = "populations")
class PopulationData(
    @Id
    @GeneratedValue(strategy = SEQUENCE)
    val id: Long? = null,
    val optimizationRunId: Long? = null,
    val generation: Int? = null,
    val size: Int? = null,
    @OneToMany(cascade = [CascadeType.ALL])
    @JoinColumn(name = "populationId")
    var populationMembers: MutableList<ChromosomeData>? = null,
    val createdAt: ZonedDateTime = ZonedDateTime.now(ZoneId.of("America/Sao_Paulo"))
) {
}